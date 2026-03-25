package com.myapp.learningtube.domain.highlight;

import com.myapp.learningtube.domain.highlight.dto.HighlightResponse;
import com.myapp.learningtube.domain.highlight.dto.UpdateHighlightRequest;
import com.myapp.learningtube.domain.auth.CustomUserPrincipal;
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
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/highlights")
@Tag(name = "Highlights", description = "하이라이트 id 기준 수정·삭제")
@SecurityRequirement(name = "bearerAuth")
public class HighlightController {

    private final HighlightService highlightService;

    public HighlightController(HighlightService highlightService) {
        this.highlightService = highlightService;
    }

    @PatchMapping("/{highlightId}")
    @Operation(summary = "하이라이트 수정", description = "소유자만")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "검증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<HighlightResponse> patch(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long highlightId,
            @Valid @RequestBody UpdateHighlightRequest body,
            HttpServletRequest request) {
        HighlightResponse data = highlightService.update(principal.getId(), highlightId, body);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    @DeleteMapping("/{highlightId}")
    @Operation(summary = "하이라이트 삭제", description = "소유자만")
    public ApiSuccessResponse<Void> delete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long highlightId,
            HttpServletRequest request) {
        highlightService.delete(principal.getId(), highlightId);
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
