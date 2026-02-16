package com.subin.point.service;

import com.subin.point.dto.reponse.Code;
import com.subin.point.entity.Member;
import com.subin.point.entity.Point;
import com.subin.point.entity.PointTransaction;
import com.subin.point.entity.TransactionType;
import com.subin.point.exception.MemberServiceException;
import com.subin.point.exception.PointServiceException;
import com.subin.point.repository.MemberRepository;
import com.subin.point.repository.PointRepository;
import com.subin.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.subin.point.dto.reponse.Code.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PointService {

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Value("${point.default-expire-days}")
    private int defaultExpireDays;

    // 포인트 적립
    @Transactional
    public Point earn(Long memberId, Long amount, boolean isManual, Integer expireDays) {

        // 사용자 조회
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberServiceException(NOT_FOUND_MEMBER));

        // 최대 포인트 적립 검증
        if (amount < 1 || amount > member.getMaxEarnPoint()) {
            throw new PointServiceException(Code.MAX_EARN_POINT_OVER);
        }

        // 잔여 포인트 조회
        Long totalPoints = pointRepository.availablePointsByMember(member).stream()
                .mapToLong(Point::getAvailableAmount).sum();

        // 최대보유가능 포인트 검증
        if (totalPoints + amount > member.getMaxHoldPoint()) {
            throw new PointServiceException(MAX_POINTS_EXCEEDED);
        }

        // 적립 포인트 만료일자 설정 (기본 365일)
        int daysToExpire = expireDays != null ? expireDays : defaultExpireDays;
        if (daysToExpire < 1 || daysToExpire >= 365 * 5) {
            throw new PointServiceException(EXPIRES_OUT_OF_RANGE);
        }

        // 포인트 적립 영속화
        Point point = Point.createPoint(member, amount, isManual, daysToExpire);
        pointTransactionRepository.save(PointTransaction.createTransaction(point.getAmount(), null, TransactionType.EARN, member, point));
        return pointRepository.save(point);
    }

    // 포인트 적립 취소
    @Transactional
    public void cancelEarnedPoint(Long memberId, Long amount) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberServiceException(NOT_FOUND_MEMBER));

        // 취소 가능 포인트 조회
        List<Point> points = pointRepository.availablePointsByMember(member).stream()
                .filter(p -> p.getUsedAmount() == 0)
                .sorted(Comparator.comparing(Point::getExpireAt)).toList();

        // 취소 가능 포인트 액수
        Long totalAvailableToCancel = points.stream().mapToLong(Point::getAmount).sum();

        // 포인트 부족 여부 검증
        if (totalAvailableToCancel < amount) {
            throw new PointServiceException(NOT_ENOUGH_CANCEL_POINT);
        }

        // 적립 포인트 단위로 취소 가능 여부 조회
        List<Point> pointsToCancel = findPointsToCancel(points, amount);
        if (pointsToCancel == null) {
            throw new PointServiceException(CANCEL_ONLY_UNUSED_POINTS);
        }

        // Point 적립취소 및 Pont Transaction 영속화
        for (Point point : pointsToCancel) {
            point.setCanceledAt(LocalDateTime.now());

            PointTransaction transaction = PointTransaction.createTransaction(point.getAmount(), null, TransactionType.CANCEL, member, point);
            pointTransactionRepository.save(transaction);
        }
    }

    // 포인트 사용
    @Transactional
    public void usePoint(Long memberId, Long amount, String orderId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberServiceException(NOT_FOUND_MEMBER));

        // 동일 주문번호 존재 여부 검증
        if (pointTransactionRepository.existsByOrderIdAndType(orderId, TransactionType.USE)) {
            throw new PointServiceException(DUPLICATE_ORDER);
        }

        // 포인트 사용 순서 설정
        // 1. 관리자 수기지급 포인트, 2. 만료일이 임박한 포인트
        List<Point> points = pointRepository.availablePointsByMember(member).stream()
                .filter(p -> p.getAvailableAmount() > 0)
                .sorted(Comparator.comparing(Point::isManual).reversed().thenComparing(Point::getExpireAt))
                .toList();

        if (points.stream().mapToLong(Point::getAvailableAmount).sum() < amount) {
            throw new PointServiceException(NOT_ENOUGH_POINT);
        }

        Long remainingAmount = amount;
        for (Point point : points) {
            Long available = point.getAvailableAmount();
            if (available <= 0) continue;

            // 포인트 차감
            Long useAmount = Math.min(available, remainingAmount);
            point.setUsedAmount(point.getUsedAmount() + useAmount);
            pointRepository.save(point);

            // 트랜잭션 차감 트랜잭션 생성
            PointTransaction transaction = PointTransaction.createTransaction(useAmount, orderId, TransactionType.USE, member, point);
            pointTransactionRepository.save(transaction);

            remainingAmount -= useAmount;
            if (remainingAmount == 0) break;
        }
    }

    // 포인트 사용 취소
    @Transactional
    public void cancelPointUse(Long memberId, String orderId, Long amount) {
        // 회원 조회 및 존재 여부 확인
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberServiceException(NOT_FOUND_MEMBER));

        // 포인트 트랜잭션으로 총 사용된 포인트 계산 (USE, USECANCEL)
        List<PointTransaction> transactions = pointTransactionRepository
                .findByMemberAndOrderIdAndTypeIn(member, orderId, Arrays.asList(TransactionType.USE, TransactionType.USECANCEL));

        // 포인트 사용 여부 검즘
        if (transactions.isEmpty()) {
            throw new PointServiceException(NOT_FOUND_USING_POINT);
        }

        // 트랜잭션 생성 순서 기준으로 포인트별 순사용 포인트(USE - USECANCEL) 계산
        Map<Long, Point> pointById = new HashMap<>();
        Map<Long, Long> netUsedByPointId = new HashMap<>();
        transactions.stream()
                .sorted(Comparator.comparing(PointTransaction::getCreatedAt).thenComparing(PointTransaction::getId))
                .forEach(transaction -> {
                    Long pointId = transaction.getPoint().getId();
                    pointById.putIfAbsent(pointId, transaction.getPoint());
                    long delta = transaction.getType() == TransactionType.USE ? transaction.getAmount() : -transaction.getAmount();
                    netUsedByPointId.merge(pointId, delta, Long::sum);
                });

        // 총 사용된 포인트 계산 (포인트별 순사용 포인트의 양수 합)
        Long totalUsed = netUsedByPointId.values().stream()
                .filter(used -> used > 0)
                .mapToLong(Long::longValue)
                .sum();

        // 취소 금액이 사용 금액보다 큰 금액인지 검증
        if (amount > totalUsed) {
            throw new PointServiceException(NOT_ENOUGH_CANCEL_POINT);
        }

        Long remainingCancelAmount = amount;
        LocalDateTime now = LocalDateTime.now();

        // 순사용 포인트가 남은 적립건만 취소 대상으로 사용
        List<Point> pointsToCancel = netUsedByPointId.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> pointById.get(entry.getKey()))
                .sorted(Comparator.comparing(Point::getExpireAt).thenComparing(Point::getId))
                .toList();

        for (Point point : pointsToCancel) {
            if (remainingCancelAmount <= 0) break;

            Long usedByPoint = netUsedByPointId.get(point.getId());
            Long cancelableAmount = Math.min(remainingCancelAmount, usedByPoint);
            remainingCancelAmount -= cancelableAmount;

            // 만료 여부에 따라 처리
            if (point.getExpireAt().isBefore(now)) {
                // 만료된 경우, 신규 포인트로 재적립
                Point newPoint = Point.createPoint(member, cancelableAmount, point.isManual(), defaultExpireDays);
                pointRepository.save(newPoint);
                // 사용취소
                pointTransactionRepository.save(PointTransaction.createTransaction(cancelableAmount, orderId, TransactionType.USECANCEL, member, point));
                // 만료 재적립
                pointTransactionRepository.save(PointTransaction.createTransaction(cancelableAmount, orderId, TransactionType.REISSUE, member, newPoint));
            } else {
                // 만료되지 않은 경우, 사용 포인트 반환
                point.setUsedAmount(point.getUsedAmount() - cancelableAmount);
                pointRepository.save(point);
                // 사용취소
                pointTransactionRepository.save(PointTransaction.createTransaction(cancelableAmount, orderId, TransactionType.USECANCEL, member, point));
                // 적립
                pointTransactionRepository.save(PointTransaction.createTransaction(cancelableAmount, orderId, TransactionType.EARN, member, point));
            }
        }

        // 데이터 불일치 방어
        if (remainingCancelAmount > 0) {
            throw new PointServiceException(NOT_ENOUGH_CANCEL_POINT);
        }
    }

    // 적립 취소 가능한 포인트 조합 계산
    private List<Point> findPointsToCancel(List<Point> points, Long targetAmount) {
        Map<Long, List<Point>> cancelablePoints = new HashMap<>();
        cancelablePoints.put(0L, new ArrayList<>());

        for (Point point : points) {
            Map<Long, List<Point>> temporaryPointMap = new HashMap<>(cancelablePoints);

            // 기존 합계에 현재 포인트를 더해서 취소 가능한 포인트 조합 생성
            for (Map.Entry<Long, List<Point>> entry : cancelablePoints.entrySet()) {
                Long entryKey = entry.getKey();
                List<Point> pointList = entry.getValue();

                Long totalPoints = entryKey + point.getAmount();

                // 목표 금액 초과
                if (totalPoints > targetAmount) continue;

                // 새로운 포인트 조합 생성
                List<Point> newPointList = new ArrayList<>(pointList);
                newPointList.add(point);

                // 목표 금액과 일치하는 합계를 찾으면 해당 포인트 조합 반환
                if (totalPoints.equals(targetAmount)) {
                    return newPointList;
                }
                // 신규 취소 가능 조합 추가
                temporaryPointMap.put(totalPoints, newPointList);
            }
            cancelablePoints = temporaryPointMap;
        }
        return null;
    }
}
