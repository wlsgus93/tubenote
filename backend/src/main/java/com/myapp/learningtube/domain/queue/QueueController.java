package com.myapp.learningtube.domain.queue;

import com.myapp.learningtube.domain.queue.dto.AddQueueItemRequest;
import com.myapp.learningtube.domain.queue.dto.QueueItemResponse;
import com.myapp.learningtube.domain.queue.dto.ReorderQueueRequest;
import com.myapp.learningtube.domain.queue.dto.UpdateQueueItemRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queue")
@Validated
@Tag(name = "Queue", description = "오늘/주간/백로그 학습 큐 (UserVideo 기준)")
@SecurityRequirement(name = "bearerAuth")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping
    @Operation(
            summary = "내 학습 큐 목록",
            description =
                    "queueType 생략 시 TODAY → WEEKLY → BACKLOG 순, 각 타입 내 position 오름차순. "
                            + "필터 시 해당 타입만 반환.")
    public ApiSuccessResponse<List<QueueItemResponse>> list(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) QueueType queueType,
            HttpServletRequest request) {
        List<QueueItemResponse> data = queueService.list(principal.getId(), queueType);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PostMapping
    @Operation(
            summary = "큐에 UserVideo 추가",
            description = "본인 UserVideo만. 동일 영상은 큐 전체에서 1번만(유니크). 삽입 후 position 0..n-1 재정렬.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "UserVideo 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "이미 큐에 있음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<QueueItemResponse> add(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AddQueueItemRequest body,
            HttpServletRequest request) {
        QueueItemResponse data = queueService.add(principal.getId(), body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PatchMapping("/reorder")
    @Operation(
            summary = "큐 내 순서 일괄 변경",
            description = "특정 queueType에 대해 orderedQueueItemIds가 현재 멤버와 동일한 집합이어야 함. position은 0부터 재부여.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "집합 불일치·중복 id",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<Void> reorder(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ReorderQueueRequest body,
            HttpServletRequest request) {
        queueService.reorder(principal.getId(), body);
        return ApiSuccessResponse.ok(resolveRequestId(request), null);
    }

    @PatchMapping("/{queueItemId:\\d+}")
    @Operation(
            summary = "큐 항목 수정",
            description =
                    "queueType 변경 시: 이전 타입은 남은 항목만 compact, 새 타입에는 position(또는 맨 뒤)에 삽입. "
                            + "같은 타입에서 position만 바꿀 때는 해당 버킷 내 재정렬.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "큐 항목 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<QueueItemResponse> patch(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long queueItemId,
            @Valid @RequestBody UpdateQueueItemRequest body,
            HttpServletRequest request) {
        QueueItemResponse data = queueService.update(principal.getId(), queueItemId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @DeleteMapping("/{queueItemId:\\d+}")
    @Operation(summary = "큐 항목 제거", description = "삭제 후 동일 queueType 내 position compact")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<Void> delete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long queueItemId,
            HttpServletRequest request) {
        queueService.delete(principal.getId(), queueItemId);
        return ApiSuccessResponse.ok(resolveRequestId(request), null);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return UUID.randomUUID().toString();
    }
}
