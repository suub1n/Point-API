package com.subin.point.dto.member;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "회원 포인트 설정 변경 요청")
public class UpdateMemberRequestDTO {
    // 1회 최대 적립 포인트
    @Schema(description = "1회 최대 적립 포인트", example = "100000", minimum = "1")
    @NotNull(message = "1회 최대 적립 포인트를 입력해주세요.")
    @Min(value = 1, message = "1회 최대 적립 포인트는 1 이상이어야 합니다.")
    private Long maxEarnPoint;

    // 최대 보유 포인트
    @Schema(description = "최대 보유 포인트", example = "150000", minimum = "1")
    @NotNull(message = "최대 보유 포인트를 입력해주세요.")
    @Min(value = 1, message = "최대 보유 포인트는 1 이상이어야 합니다.")
    private Long maxHoldPoint;
}
