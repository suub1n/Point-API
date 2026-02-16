package com.subin.point.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "포인트 적립 취소 요청")
public class EarnCancelRequestDTO {
    // 회원 ID
    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 id를 입력해주세요.")
    private Long memberId;

    // 적립 취소 Point
    @Schema(description = "적립 취소 포인트 금액", example = "700", minimum = "1")
    @NotNull(message = "포인트를 입력해주세요.")
    @Min(value = 1, message = "1 이상의 포인트를 입력해주세요.")
    private Long amount;
}
