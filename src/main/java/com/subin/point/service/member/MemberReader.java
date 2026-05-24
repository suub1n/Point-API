package com.subin.point.service.member;

import com.subin.point.entity.Member;
import com.subin.point.exception.MemberServiceException;
import com.subin.point.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.subin.point.dto.reponse.Code.NOT_FOUND_MEMBER;

@RequiredArgsConstructor
@Component
public class MemberReader {

    private final MemberRepository memberRepository;

    // 회원 ID로 조회하고 없으면 공통 예외를 발생시킨다.
    public Member getById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberServiceException(NOT_FOUND_MEMBER));
    }
}
