package com.subin.point.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", columnDefinition = "BIGINT COMMENT '포인트 ID'")
    private Point point;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", columnDefinition = "BIGINT COMMENT '회원 ID'")
    private Member member;

    @Column(nullable = false, columnDefinition = "BIGINT COMMENT '거래 포인트 금액'")
    private Long amount;

    @Column(columnDefinition = "VARCHAR(255) COMMENT '주문 ID'")
    private String orderId;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '거래 생성일시'")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) COMMENT '거래 유형'")
    private TransactionType type;

    // 신규 적립 거래를 생성한다.
    public static PointTransaction earn(Member member, Point point) {
        return createTransaction(point.getAmount(), null, TransactionType.EARN, member, point);
    }

    // 사용 취소로 복원된 기존 포인트의 적립 거래를 생성한다.
    public static PointTransaction restoredEarn(Long amount, String orderId, Member member, Point point) {
        return createTransaction(amount, orderId, TransactionType.EARN, member, point);
    }

    // 적립 취소 거래를 생성한다.
    public static PointTransaction cancelEarn(Member member, Point point) {
        return createTransaction(point.getAmount(), null, TransactionType.CANCEL, member, point);
    }

    // 주문 사용 거래를 생성한다.
    public static PointTransaction use(Long amount, String orderId, Member member, Point point) {
        return createTransaction(amount, orderId, TransactionType.USE, member, point);
    }

    // 주문 사용 취소 거래를 생성한다.
    public static PointTransaction cancelUse(Long amount, String orderId, Member member, Point point) {
        return createTransaction(amount, orderId, TransactionType.USECANCEL, member, point);
    }

    // 만료 포인트 재적립 거래를 생성한다.
    public static PointTransaction reissue(Long amount, String orderId, Member member, Point point) {
        return createTransaction(amount, orderId, TransactionType.REISSUE, member, point);
    }

    // 거래 공통 필드를 설정한다.
    private static PointTransaction createTransaction(Long amount, String orderId, TransactionType type, Member member, Point point) {
        PointTransaction transaction = new PointTransaction();
        transaction.setAmount(amount);
        transaction.setOrderId(orderId);
        transaction.setType(type);
        transaction.setMember(member);
        transaction.setPoint(point);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
