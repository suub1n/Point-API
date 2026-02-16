package com.subin.point.dto.point;

import com.subin.point.entity.Point;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "포인트 적립 응답 데이터")
public class EarnResponseDTO {
    // 포인트 ID
    @Schema(description = "포인트 ID", example = "1")
    private Long pointId;
    // 회원 ID
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    // 적립 Point 금액
    @Schema(description = "적립 포인트 금액", example = "5000")
    private Long amount;
    // 관리자 수기 지급 여부
    @Schema(description = "관리자 수기 지급 여부", example = "true")
    private boolean isManual;
    // 포인트 만료일
    @Schema(description = "포인트 만료일시", example = "2026-12-31T23:59:59")
    private LocalDateTime expireAt;

    public EarnResponseDTO(Point point) {
        this.pointId = point.getId();
        this.memberId = point.getMember().getId();
        this.amount = point.getAmount();
        this.isManual = point.isManual();
        this.expireAt = point.getExpireAt();
    }
}
