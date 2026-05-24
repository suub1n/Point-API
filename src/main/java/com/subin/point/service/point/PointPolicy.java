package com.subin.point.service.point;

import com.subin.point.entity.Member;
import com.subin.point.exception.PointServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.subin.point.dto.reponse.Code.EXPIRES_OUT_OF_RANGE;
import static com.subin.point.dto.reponse.Code.MAX_EARN_POINT_OVER;
import static com.subin.point.dto.reponse.Code.MAX_POINTS_EXCEEDED;

@Component
public class PointPolicy {

    private static final int MIN_EXPIRE_DAYS = 1;
    private static final int MAX_EXPIRE_DAYS_EXCLUSIVE = 365 * 5;

    private final int defaultExpireDays;

    public PointPolicy(@Value("${point.default-expire-days}") int defaultExpireDays) {
        this.defaultExpireDays = defaultExpireDays;
    }

    // 1회 적립 가능 금액 범위를 검증한다.
    public void validateEarnAmount(Member member, long amount) {
        if (amount < 1 || amount > member.getMaxEarnPoint()) {
            throw new PointServiceException(MAX_EARN_POINT_OVER);
        }
    }

    // 적립 후 최대 보유 포인트를 초과하지 않는지 검증한다.
    public void validateHoldLimit(Member member, long currentAvailableAmount, long earningAmount) {
        if (currentAvailableAmount + earningAmount > member.getMaxHoldPoint()) {
            throw new PointServiceException(MAX_POINTS_EXCEEDED);
        }
    }

    // 요청 만료일이 없으면 기본값을 사용하고 허용 범위를 검증한다.
    public int resolveExpireDays(Integer expireDays) {
        int daysToExpire = expireDays != null ? expireDays : defaultExpireDays;
        if (daysToExpire < MIN_EXPIRE_DAYS || daysToExpire >= MAX_EXPIRE_DAYS_EXCLUSIVE) {
            throw new PointServiceException(EXPIRES_OUT_OF_RANGE);
        }
        return daysToExpire;
    }

    // 만료 포인트 재적립 시 사용할 기본 만료일을 반환한다.
    public int defaultExpireDays() {
        return defaultExpireDays;
    }
}
