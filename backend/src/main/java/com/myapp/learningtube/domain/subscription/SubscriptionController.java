package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.subscription.dto.ChannelUpdatesSyncResponse;
import com.myapp.learningtube.domain.subscription.dto.PatchSubscriptionRequest;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionRecentVideoResponse;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionResponse;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionSyncResponse;
import com.myapp.learningtube.domain.auth.CustomUserPrincipal;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Validated
@Tag(
        name = "Subscriptions",
        description =
                "YouTube 구독 채널 동기화, 채널 최신 업로드 피드(조회는 DB, `.../sync`·`channel-updates/sync` 만 외부 API)")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionChannelUpdatesService subscriptionChannelUpdatesService;

    public SubscriptionController(
            SubscriptionService subscriptionService,
            SubscriptionChannelUpdatesService subscriptionChannelUpdatesService) {
        this.subscriptionService = subscriptionService;
        this.subscriptionChannelUpdatesService = subscriptionChannelUpdatesService;
    }

    @PostMapping("/channel-updates/sync")
    @Operation(
            summary = "구독 채널 최신 업로드 동기화",
            description =
                    "각 구독 채널의 uploads 플레이리스트(또는 stub)에서 최근 영상을 가져와 공용 Video upsert 및 "
                            + "피드(`subscription_recent_videos`)를 갱신합니다. `unreadNewVideoCount` 는 "
                            + "UserVideo 미등록 기준으로 재계산됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공(채널별 일부 실패는 failedChannels에 반영)"),
        @ApiResponse(
                responseCode = "400",
                description = "토큰 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<ChannelUpdatesSyncResponse> syncChannelUpdates(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest request) {
        ChannelUpdatesSyncResponse data =
                subscriptionChannelUpdatesService.syncAllSubscribedChannels(principal.getId());
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping("/recent-videos")
    @Operation(
            summary = "전체 구독 채널 최근 업로드 피드",
            description = "내부 DB만 조회. `publishedAt` 내림차순.")
    public ApiSuccessResponse<List<SubscriptionRecentVideoResponse>> listAllRecentVideos(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {
        Page<SubscriptionRecentVideoResponse> result =
                subscriptionChannelUpdatesService.listRecentAll(principal.getId(), page, size);
        PageMeta meta = PageMeta.from(result, page, "publishedAt,desc");
        return ApiSuccessResponse.ok(resolveRequestId(request), result.getContent(), meta);
    }

    @PostMapping("/sync")
    @Operation(
            summary = "구독 채널 동기화",
            description =
                    "YouTube Data API `subscriptions.list`(또는 stub)로 목록을 가져와 공용 Channel·UserSubscription 에 반영. "
                            + "`learningtube.youtube.stub=false` 일 때는 UserOAuthAccount(GOOGLE)의 액세스 토큰이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "토큰 없음 등",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "YouTube 인증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "502",
                description = "YouTube API 오류",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "429",
                description = "할당량 초과",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<SubscriptionSyncResponse> sync(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest request) {
        SubscriptionSyncResponse data = subscriptionService.syncFromYoutube(principal.getId());
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping
    @Operation(summary = "내 구독 목록", description = "내부 DB 기준. `updatedAt` 내림차순.")
    public ApiSuccessResponse<List<SubscriptionResponse>> list(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {
        Page<SubscriptionResponse> result = subscriptionService.listSubscriptions(principal.getId(), page, size);
        PageMeta meta = PageMeta.from(result, page, "updatedAt,desc");
        return ApiSuccessResponse.ok(resolveRequestId(request), result.getContent(), meta);
    }

    @GetMapping("/{subscriptionId}/recent-videos")
    @Operation(summary = "특정 구독 채널의 최근 업로드", description = "내부 DB만 조회. 본인 구독만.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "구독 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<List<SubscriptionRecentVideoResponse>> listRecentForSubscription(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long subscriptionId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {
        Page<SubscriptionRecentVideoResponse> result =
                subscriptionChannelUpdatesService.listRecentForSubscription(
                        principal.getId(), subscriptionId, page, size);
        PageMeta meta = PageMeta.from(result, page, "publishedAt,desc");
        return ApiSuccessResponse.ok(resolveRequestId(request), result.getContent(), meta);
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "구독 단건", description = "본인 구독만 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<SubscriptionResponse> getOne(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long subscriptionId,
            HttpServletRequest request) {
        SubscriptionResponse data = subscriptionService.getSubscription(principal.getId(), subscriptionId);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PatchMapping("/{subscriptionId}")
    @Operation(
            summary = "구독 설정 수정",
            description = "category, isFavorite, isLearningChannel, note — 전달된 필드만 갱신")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "수정 필드 없음·검증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<SubscriptionResponse> patch(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long subscriptionId,
            @Valid @RequestBody PatchSubscriptionRequest body,
            HttpServletRequest request) {
        SubscriptionResponse data =
                subscriptionService.patchSubscription(principal.getId(), subscriptionId, body);
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
