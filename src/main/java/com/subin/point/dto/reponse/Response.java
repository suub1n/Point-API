package com.subin.point.dto.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
@Schema(description = "공통 응답")
public class Response<T> {

    @Schema(description = "응답 코드", example = "REQUEST_SUCCESS")
    private String code;
    @Schema(description = "응답 메시지", example = "성공")
    private String message;
    @Schema(description = "응답 데이터")
    private T data;

    public static <T> ResponseEntity<Response<T>> of(Code code, T data) {
        return ResponseEntity.status(code.getStatus()).body(new Response<>(code.name(), code.getMessage(), data));
    }

    public static <T> ResponseEntity<Response<T>> of(Code code) {
        return of(code, null);
    }
}
