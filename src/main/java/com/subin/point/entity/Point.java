package com.subin.point.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "member_id", columnDefinition = "BIGINT COMMENT '회원 ID'")
    private Member member;

    @Column(nullable = false, columnDefinition = "BIGINT COMMENT '포인트 금액'")
    private Long amount;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0 COMMENT '사용된 포인트 금액'")
    private Long usedAmount = 0L;

    @Column(nullable = false, columnDefinition = "BOOLEAN COMMENT '관리자 수기 지급 여부'")
    private boolean isManual;

    @Column(columnDefinition = "TIMESTAMP COMMENT '만료일시'")
    private LocalDateTime expireAt;

    @Column(columnDefinition = "TIMESTAMP COMMENT '취소일시'")
    private LocalDateTime canceledAt;

    @OneToMany(mappedBy = "point", cascade = CascadeType.ALL)
    private List<PointTransaction> transactions = new ArrayList<>();

    @Version
    private Long version;

    // 현재 사용할 수 있는 잔여 포인트를 계산한다.
    public long getAvailableAmount() {
        return amount - usedAmount;
    }

    // 적립 후 아직 한 번도 사용하지 않은 포인트인지 확인한다.
    public boolean isUnused() {
        return usedAmount == 0;
    }

    // 차감 가능한 잔여 포인트가 있는지 확인한다.
    public boolean hasAvailableAmount() {
        return getAvailableAmount() > 0;
    }

    // 기준 시각에 만료된 포인트인지 확인한다.
    public boolean isExpired(LocalDateTime now) {
        return !expireAt.isAfter(now);
    }

    // 사용 가능한 포인트에서 요청 금액만큼 차감한다.
    public void use(long amount) {
        if (amount < 1 || amount > getAvailableAmount()) {
            throw new IllegalArgumentException("Invalid point use amount.");
        }
        this.usedAmount += amount;
    }

    // 사용 취소 금액만큼 사용 금액을 되돌린다.
    public void restoreUsedAmount(long amount) {
        if (amount < 1 || amount > usedAmount) {
            throw new IllegalArgumentException("Invalid point restore amount.");
        }
        this.usedAmount -= amount;
    }

    // 적립 취소 시각을 기록해 포인트를 취소 상태로 만든다.
    public void cancel(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    private void addToMember(Member member) {
        this.member = member;
        member.getPoints().add(this);
    }

    // 회원에게 신규 적립 포인트를 생성한다.
    public static Point createPoint(Member member, Long amount, boolean isManual, int daysToExpire) {
        Point point = new Point();
        point.amount = amount;
        point.isManual = isManual;
        point.expireAt = LocalDateTime.now().plusDays(daysToExpire);
        point.addToMember(member);
        return point;
    }
}
