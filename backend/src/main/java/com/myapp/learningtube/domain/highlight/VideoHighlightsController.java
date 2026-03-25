package com.myapp.learningtube.domain.highlight;

import com.myapp.learningtube.domain.highlight.dto.CreateHighlightRequest;
import com.myapp.learningtube.domain.highlight.dto.HighlightResponse;
import com.myapp.learningtube.global.auth.CustomUserPrincipal;
import com.myapp.learningtube.global.filter.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import com.myapp.learningtube.global.response.ApiSuccessResponse;
import com.myapp.learningtube.global.response.PageMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/videos/{userVideoId}/highlights")
@Validated
@Tag(name = "Highlights", description = "UserVideo 소속 구간 하이라이트")
@SecurityRequirement(name = "bearerAuth")
public class VideoHighlightsController {

    private final HighlightService highlightService;

    public VideoHighlightsController(HighlightService highlightService) {
        this.highlightService = highlightService;
    }

    @PostMapping
    @Operation(summary = "하이라이트 생성", description = "0 ≤ startSec ≤ endSec, 영상 길이 알려진 경우 endSec 상한 적용")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "검증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "UserVideo 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<HighlightResponse> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long userVideoId,
            @Valid @RequestBody CreateHighlightRequest body,
            HttpServletRequest request) {
        HighlightResponse data = highlightService.create(principal.getId(), userVideoId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping
    @Operation(summary = "하이라이트 목록", description = "핀 우선·updatedAt 내림차순, 페이징 동일 정책")
    public ApiSuccessResponse<List<HighlightResponse>> list(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long userVideoId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size,
            HttpServletRequest request) {
        Page<Highlight> result = highlightService.listForUserVideo(principal.getId(), userVideoId, page, size);
        List<HighlightResponse> list =
                result.getContent().stream().map(HighlightDtoMapper::toResponse).toList();
        PageMeta meta = PageMeta.from(result, page, "pinned,desc;updatedAt,desc");
        return ApiSuccessResponse.ok(resolveRequestId(request), list, meta);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return UUID.randomUUID().toString();
    }
}
