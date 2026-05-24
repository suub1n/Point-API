package com.subin.point.service;

import com.subin.point.entity.Member;
import com.subin.point.entity.Point;
import com.subin.point.entity.PointTransaction;
import com.subin.point.entity.TransactionType;
import com.subin.point.exception.PointServiceException;
import com.subin.point.repository.PointRepository;
import com.subin.point.repository.PointTransactionRepository;
import com.subin.point.service.member.MemberReader;
import com.subin.point.service.point.EarnCancelPointSelector;
import com.subin.point.service.point.OrderPointUsage;
import com.subin.point.service.point.PointAllocation;
import com.subin.point.service.point.PointPolicy;
import com.subin.point.service.point.PointUsage;
import com.subin.point.service.point.PointUseAllocator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.subin.point.dto.reponse.Code.DUPLICATE_ORDER;
import static com.subin.point.dto.reponse.Code.NOT_ENOUGH_CANCEL_POINT;
import static com.subin.point.dto.reponse.Code.NOT_FOUND_USING_POINT;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PointService {

    private final MemberReader memberReader;
    private final PointRepository pointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointPolicy pointPolicy;
    private final PointUseAllocator pointUseAllocator;
    private final EarnCancelPointSelector earnCancelPointSelector;

    // 포인트를 적립하고 적립 거래 이력을 생성한다.
    @Transactional
    public Point earn(Long memberId, Long amount, boolean isManual, Integer expireDays) {
        Member member = memberReader.getById(memberId);

        pointPolicy.validateEarnAmount(member, amount);
        pointPolicy.validateHoldLimit(member, pointRepository.sumAvailableAmountByMember(member), amount);

        Point point = pointRepository.save(
                Point.createPoint(member, amount, isManual, pointPolicy.resolveExpireDays(expireDays))
        );
        pointTransactionRepository.save(PointTransaction.earn(member, point));
        return point;
    }

    // 미사용 적립 포인트 중 요청 금액과 정확히 일치하는 조합을 취소한다.
    @Transactional
    public void cancelEarnedPoint(Long memberId, Long amount) {
        Member member = memberReader.getById(memberId);
        List<Point> pointsToCancel = earnCancelPointSelector.selectCancelablePoints(
                pointRepository.findAvailableByMember(member),
                amount
        );

        LocalDateTime canceledAt = LocalDateTime.now();
        List<PointTransaction> transactions = new ArrayList<>(pointsToCancel.size());
        for (Point point : pointsToCancel) {
            point.cancel(canceledAt);
            transactions.add(PointTransaction.cancelEarn(member, point));
        }

        pointTransactionRepository.saveAll(transactions);
    }

    // 사용 우선순위에 따라 포인트를 차감하고 주문별 사용 이력을 남긴다.
    @Transactional
    public void usePoint(Long memberId, Long amount, String orderId) {
        Member member = memberReader.getById(memberId);

        if (pointTransactionRepository.existsByOrderIdAndType(orderId, TransactionType.USE)) {
            throw new PointServiceException(DUPLICATE_ORDER);
        }

        List<PointAllocation> allocations = pointUseAllocator.allocate(
                pointRepository.findAvailableByMember(member),
                amount
        );

        List<PointTransaction> transactions = new ArrayList<>(allocations.size());
        for (PointAllocation allocation : allocations) {
            Point point = allocation.point();
            point.use(allocation.amount());
            transactions.add(PointTransaction.use(allocation.amount(), orderId, member, point));
        }

        pointTransactionRepository.saveAll(transactions);
    }

    // 주문의 순사용 포인트를 기준으로 사용 취소를 처리한다.
    @Transactional
    public void cancelPointUse(Long memberId, String orderId, Long amount) {
        Member member = memberReader.getById(memberId);
        List<PointTransaction> transactions = pointTransactionRepository.findByMemberAndOrderIdAndTypeIn(
                member,
                orderId,
                List.of(TransactionType.USE, TransactionType.USECANCEL)
        );

        if (transactions.isEmpty()) {
            throw new PointServiceException(NOT_FOUND_USING_POINT);
        }

        OrderPointUsage orderPointUsage = OrderPointUsage.from(transactions);
        if (amount > orderPointUsage.totalCancelableAmount()) {
            throw new PointServiceException(NOT_ENOUGH_CANCEL_POINT);
        }

        List<PointTransaction> transactionsToSave = cancelUsedPoints(member, orderId, amount, orderPointUsage);
        if (!transactionsToSave.isEmpty()) {
            pointTransactionRepository.saveAll(transactionsToSave);
        }
    }

    // 취소 대상 포인트를 순회하며 만료 여부에 따라 복원 또는 재적립한다.
    private List<PointTransaction> cancelUsedPoints(Member member,
                                                    String orderId,
                                                    long amount,
                                                    OrderPointUsage orderPointUsage) {
        long remainingCancelAmount = amount;
        LocalDateTime now = LocalDateTime.now();
        List<PointTransaction> transactionsToSave = new ArrayList<>();

        for (PointUsage usage : orderPointUsage.cancelableUsages()) {
            if (remainingCancelAmount == 0) {
                break;
            }

            Point point = usage.point();
            long cancelableAmount = Math.min(remainingCancelAmount, usage.amount());
            remainingCancelAmount -= cancelableAmount;

            if (point.isExpired(now)) {
                reissueExpiredPoint(member, orderId, point, cancelableAmount, transactionsToSave);
                continue;
            }

            point.restoreUsedAmount(cancelableAmount);
            transactionsToSave.add(PointTransaction.cancelUse(cancelableAmount, orderId, member, point));
            transactionsToSave.add(PointTransaction.restoredEarn(cancelableAmount, orderId, member, point));
        }

        if (remainingCancelAmount > 0) {
            throw new PointServiceException(NOT_ENOUGH_CANCEL_POINT);
        }

        return transactionsToSave;
    }

    // 만료된 포인트의 사용 취소분은 새 포인트로 재적립한다.
    private void reissueExpiredPoint(Member member,
                                     String orderId,
                                     Point expiredPoint,
                                     long amount,
                                     List<PointTransaction> transactionsToSave) {
        Point newPoint = pointRepository.save(
                Point.createPoint(member, amount, expiredPoint.isManual(), pointPolicy.defaultExpireDays())
        );

        transactionsToSave.add(PointTransaction.cancelUse(amount, orderId, member, expiredPoint));
        transactionsToSave.add(PointTransaction.reissue(amount, orderId, member, newPoint));
    }
}
