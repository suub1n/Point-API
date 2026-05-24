package com.subin.point.service.point;

import com.subin.point.entity.Point;
import com.subin.point.entity.PointTransaction;
import com.subin.point.entity.TransactionType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderPointUsage {

    private final List<PointUsage> cancelableUsages;

    private OrderPointUsage(List<PointUsage> cancelableUsages) {
        this.cancelableUsages = cancelableUsages;
    }

    // 주문 거래 이력을 포인트별 순사용 금액으로 집계한다.
    public static OrderPointUsage from(List<PointTransaction> transactions) {
        Map<Long, Point> pointById = new HashMap<>();
        Map<Long, Long> netUsedByPointId = new HashMap<>();

        transactions.stream()
                .sorted(Comparator.comparing(PointTransaction::getCreatedAt).thenComparing(PointTransaction::getId))
                .forEach(transaction -> {
                    Long pointId = transaction.getPoint().getId();
                    pointById.putIfAbsent(pointId, transaction.getPoint());
                    netUsedByPointId.merge(pointId, signedAmount(transaction), Long::sum);
                });

        List<PointUsage> cancelableUsages = netUsedByPointId.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new PointUsage(pointById.get(entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparing((PointUsage usage) -> usage.point().getExpireAt())
                        .thenComparing(usage -> usage.point().getId()))
                .toList();

        return new OrderPointUsage(cancelableUsages);
    }

    // 아직 취소 가능한 주문 사용 포인트 총액을 반환한다.
    public long totalCancelableAmount() {
        return cancelableUsages.stream()
                .mapToLong(PointUsage::amount)
                .sum();
    }

    // 취소 가능한 포인트별 사용 금액 목록을 반환한다.
    public List<PointUsage> cancelableUsages() {
        return cancelableUsages;
    }

    // 사용 거래는 더하고 사용 취소 거래는 차감한다.
    private static long signedAmount(PointTransaction transaction) {
        if (transaction.getType() == TransactionType.USE) {
            return transaction.getAmount();
        }
        return -transaction.getAmount();
    }
}
