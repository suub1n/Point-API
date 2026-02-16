package com.subin.point.dto.member;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "회원 생성 요청")
public class CreateMemberRequestDTO {
    // 이름
    @Schema(description = "회원 이름", example = "테스트회원", minLength = 1, maxLength = 30)
    @NotNull(message = "이름을 입력해주세요.")
    @Size(min = 1, max = 30, message = "이름은 1자 이상 30자 이하이어야 합니다.")
    private String name;
}
