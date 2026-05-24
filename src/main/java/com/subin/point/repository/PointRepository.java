package com.subin.point.repository;

import com.subin.point.entity.Member;
import com.subin.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    @Query("SELECT p FROM Point p WHERE p.member = :member AND p.expireAt > CURRENT_TIMESTAMP AND p.canceledAt IS NULL")
    List<Point> findAvailableByMember(@Param("member") Member member);

    @Query("SELECT COALESCE(SUM(p.amount - p.usedAmount), 0) FROM Point p WHERE p.member = :member AND p.expireAt > CURRENT_TIMESTAMP AND p.canceledAt IS NULL")
    long sumAvailableAmountByMember(@Param("member") Member member);

    default List<Point> availablePointsByMember(Member member) {
        return findAvailableByMember(member);
    }
}
