package com.subin.point.service;

import com.subin.point.dto.reponse.Code;
import com.subin.point.entity.Member;
import com.subin.point.exception.MemberServiceException;
import com.subin.point.repository.MemberRepository;
import com.subin.point.service.member.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberReader memberReader;

    // 중복 이름을 검증한 뒤 신규 회원을 생성한다.
    @Transactional
    public Member createMember(String name) {

        // 중복 닉네임 검증
        if (memberRepository.existsByName(name)) {
            throw new MemberServiceException(Code.DUPLICATE_NAME);
        }

        return memberRepository.save(Member.createMember(name));
    }

    // 회원의 1회 적립 한도와 최대 보유 한도를 변경한다.
    @Transactional
    public void updateMember(Long memberId, Long maxEarnPoint, Long maxHoldPoint) {
        Member member = memberReader.getById(memberId);

        // 회원 적립, 보유 포인트 설정 변경
        member.updateMemberPointSettings(maxEarnPoint, maxHoldPoint);
    }
}
