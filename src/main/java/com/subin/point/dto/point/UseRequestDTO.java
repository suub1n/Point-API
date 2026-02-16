package com.subin.point.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "포인트 사용 요청")
public class UseRequestDTO {
    // 회원 ID
    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 id를 입력해주세요.")
    private Long memberId;

    // 주문번호
    @Schema(description = "주문 번호", example = "ORDER001", minLength = 1, maxLength = 100)
    @NotNull(message = "주문번호를 입력해주세요.")
    @Size(min = 1, max = 100, message = "주문번호를 100자 이하로 입력해주세요.")
    private String orderId;

    // 사용 Point 금액
    @Schema(description = "사용 포인트 금액", example = "1200", minimum = "1")
    @NotNull(message = "포인트를 입력해주세요.")
    @Min(value = 1, message = "1 이상의 포인트를 입력해주세요.")
    private Long amount;
}
