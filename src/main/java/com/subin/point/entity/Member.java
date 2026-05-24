package com.subin.point.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class Member {

    public static final long DEFAULT_MAX_EARN_POINT = 100_000L;
    public static final long DEFAULT_MAX_HOLD_POINT = 150_000L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, columnDefinition = "VARCHAR(64) COMMENT '이름'")
    private String name;

    @Column(nullable = false, columnDefinition = "BIGINT COMMENT '1회 최대 적립 포인트'")
    private Long maxEarnPoint;

    @Column(nullable = false, columnDefinition = "BIGINT COMMENT '최대 보유 포인트'")
    private Long maxHoldPoint;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Point> points = new ArrayList<>();

    // 기본 포인트 한도를 가진 신규 회원을 생성한다.
    public static Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setMaxEarnPoint(DEFAULT_MAX_EARN_POINT);
        member.setMaxHoldPoint(DEFAULT_MAX_HOLD_POINT);
        return member;
    }

    // 회원의 포인트 적립/보유 한도를 변경한다.
    public void updateMemberPointSettings(Long maxEarnPoint, Long maxHoldPoint) {
        this.setMaxEarnPoint(maxEarnPoint);
        this.setMaxHoldPoint(maxHoldPoint);
    }
}
