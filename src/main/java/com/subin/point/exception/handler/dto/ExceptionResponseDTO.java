package com.subin.point.exception.handler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Schema(description = "예외 응답")
public class ExceptionResponseDTO {
    @Schema(description = "에러 코드", example = "NOT_FOUND_MEMBER")
    private String code;
    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다.")
    private String message;
}
