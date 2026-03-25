package com.myapp.learningtube.domain.collection;

import com.myapp.learningtube.domain.collection.dto.AddCollectionVideoRequest;
import com.myapp.learningtube.domain.collection.dto.CollectionDetailResponse;
import com.myapp.learningtube.domain.collection.dto.CollectionResponse;
import com.myapp.learningtube.domain.collection.dto.CollectionVideoItemResponse;
import com.myapp.learningtube.domain.collection.dto.CreateCollectionRequest;
import com.myapp.learningtube.domain.collection.dto.ReorderCollectionVideosRequest;
import com.myapp.learningtube.domain.collection.dto.UpdateCollectionRequest;
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
@RequestMapping("/api/v1/collections")
@Validated
@Tag(name = "Collections", description = "학습 컬렉션(폴더) 및 UserVideo 담기")
@SecurityRequirement(name = "bearerAuth")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    @Operation(summary = "컬렉션 생성", description = "sortOrder는 사용자 기준 자동 부여(맨 뒤)")
    public ApiSuccessResponse<CollectionResponse> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateCollectionRequest body,
            HttpServletRequest request) {
        CollectionResponse data = collectionService.create(principal.getId(), body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping
    @Operation(
            summary = "내 컬렉션 목록",
            description = "각 항목에 videoCount 포함. page 기본 1, size 기본 20(최대 100).")
    public ApiSuccessResponse<List<CollectionResponse>> list(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {
        Page<CollectionResponse> result = collectionService.listCollections(principal.getId(), page, size);
        PageMeta meta = PageMeta.from(result, page, "sortOrder,asc;updatedAt,desc");
        return ApiSuccessResponse.ok(resolveRequestId(request), result.getContent(), meta);
    }

    @GetMapping("/{collectionId}")
    @Operation(
            summary = "컬렉션 상세",
            description = "videoCount + 상위 3개 영상 썸네일 previewThumbnailUrls(확장 여지)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<CollectionDetailResponse> getOne(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long collectionId,
            HttpServletRequest request) {
        CollectionDetailResponse data = collectionService.getDetail(principal.getId(), collectionId);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @PatchMapping("/{collectionId}")
    @Operation(summary = "컬렉션 수정")
    public ApiSuccessResponse<CollectionResponse> patch(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long collectionId,
            @Valid @RequestBody UpdateCollectionRequest body,
            HttpServletRequest request) {
        CollectionResponse data = collectionService.update(principal.getId(), collectionId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @DeleteMapping("/{collectionId}")
    @Operation(
            summary = "컬렉션 삭제",
            description = "연결된 CollectionVideo 행은 cascade로 함께 삭제(물리 삭제)")
    public ApiSuccessResponse<Void> deleteCollection(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long collectionId,
            HttpServletRequest request) {
        collectionService.deleteCollection(principal.getId(), collectionId);
        return ApiSuccessResponse.ok(resolveRequestId(request), null);
    }

    @PostMapping("/{collectionId}/videos")
    @Operation(summary = "컬렉션에 UserVideo 추가", description = "본인 UserVideo만, 컬렉션당 중복 불가")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "404",
                description = "컬렉션 또는 UserVideo 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "이미 포함됨",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<CollectionVideoItemResponse> addVideo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long collectionId,
            @Valid @RequestBody AddCollectionVideoRequest body,
            HttpServletRequest request) {
        CollectionVideoItemResponse data = collectionService.addVideo(principal.getId(), collectionId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping("/{collectionId}/videos")
    @Operation(summary = "컬렉션에 담긴 영상 목록", description = "sortOrder 오름차순, 페이징")
    public ApiSuccessResponse<List<CollectionVideoItemResponse>> listVideos(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long collectionId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size,
            HttpServletRequest request) {
        Page<CollectionVideoItemResponse> result =
                collectionService.listVideos(principal.getId(), collectionId, page, size);
        PageMeta meta = PageMeta.from(result, page, "position,asc;id,asc");
        return ApiSuccessResponse.ok(resolveRequestId(request), result.getContent(), meta);
    }

    @DeleteMapping("/{collectionId}/videos/{userVideoId}")
    @Operation(summary = "컬렉션에서 UserVideo 제거")
    public ApiSuccessResponse<Void> removeVideo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long collectionId,
            @PathVariable Long userVideoId,
            HttpServletRequest request) {
        collectionService.removeVideo(principal.getId(), collectionId, userVideoId);
        return ApiSuccessResponse.ok(resolveRequestId(request), null);
    }

    @PatchMapping("/{collectionId}/videos/order")
    @Operation(
            summary = "컬렉션 내 순서 변경",
            description = "orderedUserVideoIds는 현재 멤버와 동일한 집합(순서만 변경)")
    public ApiSuccessResponse<Void> reorderVideos(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long collectionId,
            @Valid @RequestBody ReorderCollectionVideosRequest body,
            HttpServletRequest request) {
        collectionService.reorderVideos(principal.getId(), collectionId, body);
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
