package com.myapp.learningtube.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description =
                "메인 대시보드 집계. 리스트 필드는 데이터 없으면 빈 배열 `[]`. `weeklySummary` 는 항상 객체(내부 카운트는 0 가능).")
public class DashboardResponse {

    @Schema(
            description =
                    "오늘의 추천(임시): 우선순위·미완료 기반. Queue TODAY 큐로 대체 예정")
    private List<DashboardVideoCardDto> todayPick = new ArrayList<>();

    @Schema(description = "이어보기: 진행 초가 있고 완료가 아닌 영상")
    private List<DashboardVideoCardDto> continueWatching = new ArrayList<>();

    @Schema(description = "최근 노트")
    private List<DashboardNoteCardDto> recentNotes = new ArrayList<>();

    @Schema(description = "미완료·진행 중 영상(NOT_STARTED, IN_PROGRESS)")
    private List<DashboardVideoCardDto> incompleteVideos = new ArrayList<>();

    @Schema(description = "즐겨찾기 구독 채널의 신규 피드 요약")
    private List<DashboardFavoriteChannelDto> favoriteChannelUpdates = new ArrayList<>();

    @Schema(description = "주간 요약(항상 non-null)")
    private DashboardWeeklySummaryDto weeklySummary;

    public List<DashboardVideoCardDto> getTodayPick() {
        return todayPick;
    }

    public void setTodayPick(List<DashboardVideoCardDto> todayPick) {
        this.todayPick = todayPick;
    }

    public List<DashboardVideoCardDto> getContinueWatching() {
        return continueWatching;
    }

    public void setContinueWatching(List<DashboardVideoCardDto> continueWatching) {
        this.continueWatching = continueWatching;
    }

    public List<DashboardNoteCardDto> getRecentNotes() {
        return recentNotes;
    }

    public void setRecentNotes(List<DashboardNoteCardDto> recentNotes) {
        this.recentNotes = recentNotes;
    }

    public List<DashboardVideoCardDto> getIncompleteVideos() {
        return incompleteVideos;
    }

    public void setIncompleteVideos(List<DashboardVideoCardDto> incompleteVideos) {
        this.incompleteVideos = incompleteVideos;
    }

    public List<DashboardFavoriteChannelDto> getFavoriteChannelUpdates() {
        return favoriteChannelUpdates;
    }

    public void setFavoriteChannelUpdates(List<DashboardFavoriteChannelDto> favoriteChannelUpdates) {
        this.favoriteChannelUpdates = favoriteChannelUpdates;
    }

    public DashboardWeeklySummaryDto getWeeklySummary() {
        return weeklySummary;
    }

    public void setWeeklySummary(DashboardWeeklySummaryDto weeklySummary) {
        this.weeklySummary = weeklySummary;
    }
}
