package com.myapp.learningtube.domain.dashboard;

import com.myapp.learningtube.domain.dashboard.dto.DashboardResponse;
import com.myapp.learningtube.domain.auth.CustomUserPrincipal;
import com.myapp.learningtube.global.filter.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(
        name = "Dashboard",
        description = "메인 화면용 읽기 전용 집계 — UserVideo·Note·Highlight·Subscription 피드 조합")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(
            summary = "대시보드 집계",
            description =
                    "todayPick(임시 추천), continueWatching, recentNotes, incompleteVideos, "
                            + "favoriteChannelUpdates, weeklySummary. Entity 비노출·DTO만 반환.")
    public ApiSuccessResponse<DashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest request) {
        DashboardResponse data = dashboardService.load(principal.getId());
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
