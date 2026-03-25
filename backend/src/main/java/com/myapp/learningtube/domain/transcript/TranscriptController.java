package com.myapp.learningtube.domain.transcript;

import com.myapp.learningtube.domain.transcript.dto.SelectTranscriptTrackRequest;
import com.myapp.learningtube.domain.transcript.dto.TranscriptSyncResponse;
import com.myapp.learningtube.domain.transcript.dto.TranscriptTrackSummaryResponse;
import com.myapp.learningtube.domain.transcript.dto.TranscriptViewResponse;
import com.myapp.learningtube.global.auth.CustomUserPrincipal;
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
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/videos/{videoId}/transcript")
@Validated
@Tag(
        name = "Transcript",
        description = "공용 Video 기준 자막·스크립트(트랙/세그먼트). 내 목록에 등록된 영상만 접근.")
@SecurityRequirement(name = "bearerAuth")
public class TranscriptController {

    private final TranscriptService transcriptService;

    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    @GetMapping
    @Operation(
            summary = "선택된 트랙의 자막 조회",
            description =
                    "트랙이 없거나 선택이 없어도 200 — tracksAvailable / hasSelectedTrack / 빈 segments 로 구분. "
                            + "videoId는 공용 Video PK.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공(자막 없음도 정상 구조)"),
        @ApiResponse(
                responseCode = "403",
                description = "내 목록에 없는 영상",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Video 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<TranscriptViewResponse> getTranscript(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long videoId,
            HttpServletRequest request) {
        TranscriptViewResponse data = transcriptService.getTranscriptView(principal.getId(), videoId);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PostMapping("/sync")
    @Operation(
            summary = "자막 동기화(업스트림)",
            description =
                    "YouTube captions 등에서 수집. STEP 17 스텁은 빈 결과 반환. 세그먼트가 있는 트랙만 DB에 반영·기존 세그먼트 전면 교체.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공 — upstreamEmpty일 수 있음"),
        @ApiResponse(
                responseCode = "403",
                description = "내 목록에 없는 영상",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Video 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<TranscriptSyncResponse> sync(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long videoId,
            HttpServletRequest request) {
        TranscriptSyncResponse data = transcriptService.syncFromUpstream(principal.getId(), videoId);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping("/tracks")
    @Operation(summary = "자막 트랙 목록", description = "세그먼트 개수 포함, 본문 텍스트는 미포함")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공 — 빈 배열 가능"),
        @ApiResponse(
                responseCode = "403",
                description = "내 목록에 없는 영상",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Video 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<List<TranscriptTrackSummaryResponse>> listTracks(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long videoId,
            HttpServletRequest request) {
        List<TranscriptTrackSummaryResponse> data = transcriptService.listTracks(principal.getId(), videoId);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PostMapping("/select")
    @Operation(summary = "표시 트랙 선택", description = "동일 Video의 다른 트랙 selected는 해제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "트랙 없음·다른 영상 소속",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "내 목록에 없는 영상",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<TranscriptTrackSummaryResponse> selectTrack(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long videoId,
            @Valid @RequestBody SelectTranscriptTrackRequest body,
            HttpServletRequest request) {
        TranscriptTrackSummaryResponse data =
                transcriptService.selectTrack(principal.getId(), videoId, body);
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
