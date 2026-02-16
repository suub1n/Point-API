package com.subin.point.dto.member;

import com.subin.point.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "회원 생성 응답 데이터")
public class CreateMemberResponseDTO {
    // 회원 ID
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    // 이름
    @Schema(description = "회원 이름", example = "테스트회원")
    private String name;
    // 1회 최대 적립 포인트
    @Schema(description = "1회 최대 적립 포인트", example = "100000")
    private Long maxEarnPoint;
    // 최대 보유 포인트
    @Schema(description = "최대 보유 포인트", example = "150000")
    private Long maxHoldPoint;

    public CreateMemberResponseDTO(Member member) {
        memberId = member.getId();
        name = member.getName();
        maxEarnPoint = member.getMaxEarnPoint();
        maxHoldPoint = member.getMaxHoldPoint();
    }
}
