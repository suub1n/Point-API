package com.subin.point.controller;

import com.subin.point.dto.member.CreateMemberRequestDTO;
import com.subin.point.dto.member.CreateMemberResponseDTO;
import com.subin.point.dto.member.UpdateMemberRequestDTO;
import com.subin.point.dto.reponse.Code;
import com.subin.point.dto.reponse.Response;
import com.subin.point.entity.Member;
import com.subin.point.exception.handler.dto.ExceptionResponseDTO;
import com.subin.point.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
@Tag(name = "Members", description = "회원 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("")
    @Operation(summary = "회원 생성", description = "새로운 회원을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 중복 닉네임", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<Response<CreateMemberResponseDTO>> createMember(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원 생성 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateMemberRequestDTO.class))
            )
            @Valid @RequestBody CreateMemberRequestDTO request
    ) {
        Member member = memberService.createMember(request.getName());
        CreateMemberResponseDTO createMemberDTO = new CreateMemberResponseDTO(member);
        return Response.of(Code.REQUEST_SUCCESS, createMemberDTO);
    }

    @PatchMapping("/{memberId}/point-settings")
    @Operation(summary = "회원 포인트 설정 변경", description = "회원의 1회 최대 적립 포인트, 최대 보유 포인트를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "회원 없음", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<Response<Void>> updateMember(
            @Parameter(description = "회원 ID", required = true, example = "1")
            @PathVariable("memberId") Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원 포인트 설정 변경 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateMemberRequestDTO.class))
            )
            @Valid @RequestBody UpdateMemberRequestDTO request
    ) {
        memberService.updateMember(memberId, request.getMaxEarnPoint(), request.getMaxHoldPoint());
        return Response.of(Code.REQUEST_SUCCESS);
    }
}
