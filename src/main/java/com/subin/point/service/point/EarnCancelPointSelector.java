package com.subin.point.service.point;

import com.subin.point.entity.Point;
import com.subin.point.exception.PointServiceException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.subin.point.dto.reponse.Code.CANCEL_ONLY_UNUSED_POINTS;
import static com.subin.point.dto.reponse.Code.NOT_ENOUGH_CANCEL_POINT;

@Component
public class EarnCancelPointSelector {

    // 미사용 포인트 중 취소 금액과 정확히 일치하는 포인트 조합을 찾는다.
    public List<Point> selectCancelablePoints(List<Point> availablePoints, long amount) {
        List<Point> candidates = availablePoints.stream()
                .filter(Point::isUnused)
                .sorted(Comparator.comparing(Point::getExpireAt).thenComparing(Point::getId))
                .toList();

        long totalCancelableAmount = candidates.stream()
                .mapToLong(Point::getAmount)
                .sum();

        if (totalCancelableAmount < amount) {
            throw new PointServiceException(NOT_ENOUGH_CANCEL_POINT);
        }

        return findExactAmountCombination(candidates, amount)
                .orElseThrow(() -> new PointServiceException(CANCEL_ONLY_UNUSED_POINTS));
    }

    // 부분합을 누적해 목표 금액을 만들 수 있는 포인트 조합을 탐색한다.
    private Optional<List<Point>> findExactAmountCombination(List<Point> points, long targetAmount) {
        Map<Long, Long> previousSumBySum = new HashMap<>();
        Map<Long, Point> selectedPointBySum = new HashMap<>();
        Set<Long> reachableSums = new HashSet<>();
        reachableSums.add(0L);

        for (Point point : points) {
            List<Long> newSums = new ArrayList<>();
            for (Long currentSum : reachableSums) {
                long nextSum = currentSum + point.getAmount();
                if (nextSum > targetAmount || previousSumBySum.containsKey(nextSum)) {
                    continue;
                }

                previousSumBySum.put(nextSum, currentSum);
                selectedPointBySum.put(nextSum, point);

                if (nextSum == targetAmount) {
                    return Optional.of(reconstructPoints(previousSumBySum, selectedPointBySum, targetAmount));
                }
                newSums.add(nextSum);
            }
            reachableSums.addAll(newSums);
        }

        return Optional.empty();
    }

    // 부분합 추적 정보를 역추적해 실제 취소할 포인트 목록을 복원한다.
    private List<Point> reconstructPoints(Map<Long, Long> previousSumBySum,
                                          Map<Long, Point> selectedPointBySum,
                                          long targetAmount) {
        List<Point> result = new ArrayList<>();
        long currentSum = targetAmount;

        while (currentSum > 0) {
            Point point = selectedPointBySum.get(currentSum);
            if (point == null) {
                throw new PointServiceException(CANCEL_ONLY_UNUSED_POINTS);
            }
            result.add(point);
            currentSum = previousSumBySum.get(currentSum);
        }

        return result;
    }
}
