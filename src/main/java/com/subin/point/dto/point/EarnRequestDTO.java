package com.subin.point.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "포인트 적립 요청")
public class EarnRequestDTO {
    // 회원 ID
    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 id를 입력해주세요.")
    private Long memberId;

    // 적립 Point 금액
    @Schema(description = "적립 포인트 금액", example = "5000", minimum = "1")
    @NotNull(message = "포인트를 입력해주세요.")
    @Min(value = 1, message = "1 이상의 포인트를 입력해주세요.")
    private Long amount;

    // 관리자 수기 지급 여부
    @Schema(description = "관리자 수기 지급 여부", example = "true")
    private boolean isManual;

    // 포인트 만료일
    @Schema(description = "만료일까지의 일수(미입력 시 기본값 사용)", example = "365", minimum = "1", nullable = true)
    @Min(value = 1, message = "포인트 만료일은 1 이상이어야 합니다.")
    private Integer expireDays;
}
