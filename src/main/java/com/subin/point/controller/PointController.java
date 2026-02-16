package com.subin.point.controller;

import com.subin.point.dto.point.*;
import com.subin.point.dto.reponse.Code;
import com.subin.point.dto.reponse.Response;
import com.subin.point.entity.Point;
import com.subin.point.exception.handler.dto.ExceptionResponseDTO;
import com.subin.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
@RestController
@Tag(name = "Points", description = "포인트 API")
public class PointController {

    private final PointService pointService;

    // 포인트 적립
    @PostMapping("/earnings")
    @Operation(summary = "포인트 적립", description = "포인트를 적립합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "회원 없음", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<Response<EarnResponseDTO>> earnPoint(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "포인트 적립 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EarnRequestDTO.class))
            )
            @Valid @RequestBody EarnRequestDTO request
    ) {
        Point point = pointService.earn(request.getMemberId(), request.getAmount(), request.isManual(), request.getExpireDays());

        return Response.of(Code.REQUEST_SUCCESS, new EarnResponseDTO(point));
    }

    // 포인트 적립 취소
    @PostMapping("/earnings/cancellations")
    @Operation(summary = "포인트 적립 취소", description = "적립된 포인트를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "회원 없음", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<Response<Void>> cancelEarn(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "포인트 적립 취소 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EarnCancelRequestDTO.class))
            )
            @Valid @RequestBody EarnCancelRequestDTO request
    ) {
        pointService.cancelEarnedPoint(request.getMemberId(), request.getAmount());
        return Response.of(Code.REQUEST_SUCCESS);
    }

    // 포인트 사용
    @PostMapping("/usages")
    @Operation(summary = "포인트 사용", description = "포인트를 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "회원 없음", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<Response<Void>> usePoint(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "포인트 사용 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UseRequestDTO.class))
            )
            @Valid @RequestBody UseRequestDTO request
    ) {
        pointService.usePoint(request.getMemberId(), request.getAmount(), request.getOrderId());
        return Response.of(Code.REQUEST_SUCCESS);
    }

    // 포인트 사용 취소
    @PostMapping("/usages/cancellations")
    @Operation(summary = "포인트 사용 취소", description = "사용된 포인트를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "회원 없음", content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<Response<Void>> cancelPointUse(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "포인트 사용 취소 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UseCancelRequestDTO.class))
            )
            @Valid @RequestBody UseCancelRequestDTO request
    ) {
        pointService.cancelPointUse(request.getMemberId(), request.getOrderId(), request.getAmount());
        return Response.of(Code.REQUEST_SUCCESS);
    }
}
