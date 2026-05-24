package com.subin.point.repository;

import com.subin.point.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findFirstByName(String name);

    boolean existsByName(String name);
}
