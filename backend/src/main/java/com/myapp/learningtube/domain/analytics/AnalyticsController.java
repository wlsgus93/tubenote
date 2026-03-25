package com.myapp.learningtube.domain.analytics;

import com.myapp.learningtube.domain.analytics.dto.AnalyticsChannelStatDto;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsCollectionStatDto;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsDailyResponse;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsStatusBucketDto;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsSummaryResponse;
import com.myapp.learningtube.domain.auth.CustomUserPrincipal;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.global.filter.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import com.myapp.learningtube.global.response.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(
        name = "Analytics",
        description = "통계·분석 전용 읽기 API(실시간 집계). 추후 배치·집계 테이블로 치환 가능")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "학습 요약", description = "저장 영상·상태·노트/하이라이트·추정 학습시간(진행 초 합)")
    public ApiSuccessResponse<AnalyticsSummaryResponse> summary(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest request) {
        AnalyticsSummaryResponse data = analyticsService.summary(principal.getId());
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping("/daily")
    @Operation(
            summary = "일별 추세",
            description = "완료 영상(completed_at)·노트/하이라이트 생성일 기준 일자 버킷. "
                    + "WEEK=최근 N일, MONTH=최근 M일, ALL=활동 시작일~오늘(버킷 상한은 설정).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "rangeType 오류",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<AnalyticsDailyResponse> daily(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "WEEK") String rangeType,
            HttpServletRequest request) {
        AnalyticsRangeType rt = parseRangeType(rangeType);
        AnalyticsDailyResponse data = analyticsService.daily(principal.getId(), rt);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping("/status-distribution")
    @Operation(summary = "학습 상태 분포", description = "보관함 제외 UserVideo 기준 learningStatus별 건수")
    public ApiSuccessResponse<List<AnalyticsStatusBucketDto>> statusDistribution(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest request) {
        List<AnalyticsStatusBucketDto> data = analyticsService.statusDistribution(principal.getId());
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping("/channels")
    @Operation(
            summary = "채널별 집계",
            description = "Video 채널 메타 기준(보관함 제외). 공용 Channel 매핑 시 channelId 포함.")
    public ApiSuccessResponse<List<AnalyticsChannelStatDto>> channels(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest request) {
        List<AnalyticsChannelStatDto> data = analyticsService.channelStats(principal.getId());
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping("/collections")
    @Operation(summary = "컬렉션별 집계", description = "담긴 영상 수·완료 수")
    public ApiSuccessResponse<List<AnalyticsCollectionStatDto>> collections(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest request) {
        List<AnalyticsCollectionStatDto> data = analyticsService.collectionStats(principal.getId());
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    private static AnalyticsRangeType parseRangeType(String raw) {
        if (raw == null || raw.isBlank()) {
            return AnalyticsRangeType.WEEK;
        }
        try {
            return AnalyticsRangeType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED,
                    "rangeType은 WEEK, MONTH, ALL 중 하나여야 합니다.");
        }
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return UUID.randomUUID().toString();
    }
}
