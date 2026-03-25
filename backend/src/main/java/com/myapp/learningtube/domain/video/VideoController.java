package com.myapp.learningtube.domain.video;

import com.myapp.learningtube.domain.video.dto.ImportVideoUrlRequest;
import com.myapp.learningtube.domain.video.dto.ImportVideoUrlResponse;
import com.myapp.learningtube.domain.video.dto.UpdateArchiveRequest;
import com.myapp.learningtube.domain.video.dto.UpdateLearningStateRequest;
import com.myapp.learningtube.domain.video.dto.UpdatePinRequest;
import com.myapp.learningtube.domain.video.dto.UpdateProgressRequest;
import com.myapp.learningtube.domain.video.dto.UserVideoDetailResponse;
import com.myapp.learningtube.domain.video.dto.UserVideoSummaryResponse;
import com.myapp.learningtube.global.auth.CustomUserPrincipal;
import com.myapp.learningtube.global.filter.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import com.myapp.learningtube.global.response.ApiSuccessResponse;
import com.myapp.learningtube.global.response.PageMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/videos")
@Validated
@Tag(name = "Videos", description = "공용 Video 메타 + 사용자별 UserVideo (학습 상태)")
@SecurityRequirement(name = "bearerAuth")
public class VideoController {

    private final UserVideoService userVideoService;

    public VideoController(UserVideoService userVideoService) {
        this.userVideoService = userVideoService;
    }

    @PostMapping("/import-url")
    @Operation(summary = "YouTube URL로 영상 등록", description = "공용 Video가 없으면 생성 후 UserVideo를 추가합니다. 중복 시 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "URL 파싱 실패·검증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "미인증",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "이미 등록된 영상",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<ImportVideoUrlResponse> importUrl(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ImportVideoUrlRequest body,
            HttpServletRequest request) {
        ImportVideoUrlResponse data = userVideoService.importFromUrl(principal.getId(), body.getUrl());
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping
    @Operation(
            summary = "내 영상 목록",
            description =
                    "경로는 **`/api/v1/videos`** (v1 필수). 페이징·학습상태·아카이브·제목 검색(부분일치)·정렬. "
                            + "sort는 `필드,방향` (예: updatedAt,desc / video.title,asc). "
                            + "응답 `data`는 항상 배열(0건이면 `[]`), `meta`에 `PageMeta`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공 — data는 배열, meta에 페이징"),
        @ApiResponse(
                responseCode = "401",
                description = "미인증",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<List<UserVideoSummaryResponse>> list(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "학습 상태 필터") @RequestParam(required = false) LearningStatus learningStatus,
            @Parameter(description = "아카이브 여부 필터 (미전달 시 전체)") @RequestParam(required = false) Boolean archived,
            @Parameter(description = "제목 부분 검색") @RequestParam(required = false) String q,
            @Parameter(description = "정렬: 필드,asc|desc") @RequestParam(required = false) String sort,
            HttpServletRequest request) {
        String sortExpr = sort == null || sort.isBlank() ? "updatedAt,desc" : sort.trim();
        Page<UserVideo> result =
                userVideoService.listForUser(principal.getId(), page, size, learningStatus, archived, q, sortExpr);
        List<UserVideoSummaryResponse> list =
                result.getContent().stream().map(UserVideoDtoMapper::toSummary).toList();
        PageMeta meta = PageMeta.from(result, page, sortExpr);
        return ApiSuccessResponse.ok(resolveRequestId(request), list, meta);
    }

    @GetMapping("/{userVideoId}")
    @Operation(
            summary = "UserVideo 상세",
            description =
                    "경로 변수는 **UserVideo PK** (`videoId` 아님). noteCount·highlightCount·transcriptTracksAvailable·"
                            + "transcriptHasSelection 포함. 시각 필드는 ISO-8601 UTC(`Instant`).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "없거나 소유 아님",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<UserVideoDetailResponse> getOne(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "UserVideo PK", required = true, example = "42") @PathVariable Long userVideoId,
            HttpServletRequest request) {
        UserVideoDetailResponse data = userVideoService.getDetail(principal.getId(), userVideoId);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PatchMapping("/{userVideoId}/learning-state")
    @Operation(
            summary = "학습 상태·우선순위 변경",
            description =
                    "priority 생략 시 유지. COMPLETED로 최초 전환 시 completedAt 설정, COMPLETED가 아니면 completedAt은 null로 초기화. "
                            + "응답은 GET 상세와 동일하게 집계·자막 메타 포함.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공 — UserVideoDetailResponse"),
        @ApiResponse(
                responseCode = "400",
                description = "검증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "없거나 소유 아님",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<UserVideoDetailResponse> patchLearningState(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "UserVideo PK", required = true) @PathVariable Long userVideoId,
            @Valid @RequestBody UpdateLearningStateRequest body,
            HttpServletRequest request) {
        UserVideoDetailResponse data = userVideoService.updateLearningState(principal.getId(), userVideoId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PatchMapping("/{userVideoId}/progress")
    @Operation(
            summary = "재생 위치·진행률",
            description =
                    "`lastPositionSec`, `watchPercent` 중 **최소 하나** 필수. 재생 위치는 알려진 `durationSeconds` 초과 불가. "
                            + "응답은 GET 상세와 동일하게 집계·자막 메타 포함.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공 — UserVideoDetailResponse"),
        @ApiResponse(
                responseCode = "400",
                description = "둘 다 null·위치 초과·검증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "없거나 소유 아님",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<UserVideoDetailResponse> patchProgress(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "UserVideo PK", required = true) @PathVariable Long userVideoId,
            @Valid @RequestBody UpdateProgressRequest body,
            HttpServletRequest request) {
        UserVideoDetailResponse data = userVideoService.updateProgress(principal.getId(), userVideoId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PatchMapping("/{userVideoId}/pin")
    @Operation(summary = "핀 고정", description = "pinned true/false")
    public ApiSuccessResponse<UserVideoDetailResponse> patchPin(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long userVideoId,
            @Valid @RequestBody UpdatePinRequest body,
            HttpServletRequest request) {
        UserVideoDetailResponse data = userVideoService.updatePin(principal.getId(), userVideoId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PatchMapping("/{userVideoId}/archive")
    @Operation(summary = "보관함(아카이브)", description = "archived true/false")
    public ApiSuccessResponse<UserVideoDetailResponse> patchArchive(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long userVideoId,
            @Valid @RequestBody UpdateArchiveRequest body,
            HttpServletRequest request) {
        UserVideoDetailResponse data = userVideoService.updateArchive(principal.getId(), userVideoId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return UUID.randomUUID().toString();
    }
}
