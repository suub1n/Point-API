package com.subin.point.service.point;

import com.subin.point.entity.Point;
import com.subin.point.exception.PointServiceException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.subin.point.dto.reponse.Code.NOT_ENOUGH_POINT;

@Component
public class PointUseAllocator {

    // 관리자 지급 포인트와 빠른 만료일을 우선해 사용 금액을 배분한다.
    public List<PointAllocation> allocate(List<Point> availablePoints, long amount) {
        List<Point> candidates = availablePoints.stream()
                .filter(Point::hasAvailableAmount)
                .sorted(Comparator.comparing(Point::isManual).reversed()
                        .thenComparing(Point::getExpireAt)
                        .thenComparing(Point::getId))
                .toList();

        long totalAvailableAmount = candidates.stream()
                .mapToLong(Point::getAvailableAmount)
                .sum();

        if (totalAvailableAmount < amount) {
            throw new PointServiceException(NOT_ENOUGH_POINT);
        }

        return allocateFrom(candidates, amount);
    }

    // 정렬된 포인트 목록에서 필요한 금액만큼 순차 배분한다.
    private List<PointAllocation> allocateFrom(List<Point> points, long amount) {
        List<PointAllocation> allocations = new ArrayList<>();
        long remainingAmount = amount;

        for (Point point : points) {
            if (remainingAmount == 0) {
                break;
            }

            long useAmount = Math.min(point.getAvailableAmount(), remainingAmount);
            allocations.add(new PointAllocation(point, useAmount));
            remainingAmount -= useAmount;
        }

        return allocations;
    }
}
