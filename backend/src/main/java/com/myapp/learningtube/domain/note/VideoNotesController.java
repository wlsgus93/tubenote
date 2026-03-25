package com.myapp.learningtube.domain.note;

import com.myapp.learningtube.domain.note.dto.CreateNoteRequest;
import com.myapp.learningtube.domain.note.dto.NoteResponse;
import com.myapp.learningtube.domain.auth.CustomUserPrincipal;
import com.myapp.learningtube.global.logging.RequestIdFilter;
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
@RequestMapping("/api/v1/videos/{userVideoId}/notes")
@Validated
@Tag(name = "Notes", description = "UserVideo 소속 학습 노트")
@SecurityRequirement(name = "bearerAuth")
public class VideoNotesController {

    private final NoteService noteService;

    public VideoNotesController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    @Operation(
            summary = "노트 생성",
            description = "GENERAL은 positionSec 없음. TIMESTAMP는 positionSec(초) 필수, 영상 길이 이하.")
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
    public ApiSuccessResponse<NoteResponse> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long userVideoId,
            @Valid @RequestBody CreateNoteRequest body,
            HttpServletRequest request) {
        NoteResponse data = noteService.create(principal.getId(), userVideoId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @GetMapping
    @Operation(
            summary = "노트 목록",
            description = "핀 우선, 이어서 수정일시 내림차순. page 기본 1, size 기본 50(최대 200).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공 — data 배열, meta 페이징"),
        @ApiResponse(
                responseCode = "404",
                description = "UserVideo 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<List<NoteResponse>> list(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long userVideoId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size,
            HttpServletRequest request) {
        Page<Note> result = noteService.listForUserVideo(principal.getId(), userVideoId, page, size);
        List<NoteResponse> list = result.getContent().stream().map(NoteDtoMapper::toResponse).toList();
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
