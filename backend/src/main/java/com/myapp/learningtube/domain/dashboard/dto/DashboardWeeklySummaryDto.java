package com.myapp.learningtube.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "이번 주(UTC 월요일 00:00 기준) 활동 요약 + 스냅샷 지표")
public class DashboardWeeklySummaryDto {

    @Schema(description = "집계 주간 시작 시각(UTC)")
    private Instant weekStartUtc;

    @Schema(
            description =
                    "이번 주에 `updatedAt`이 갱신된 진행 중(IN_PROGRESS) 영상 수(보관함 제외). Queue 도입 전 활동 지표.")
    private long inProgressCount;

    @Schema(description = "이번 주에 완료 처리된 영상 수(`completedAt` 기준)")
    private long completedCount;

    @Schema(description = "이번 주 생성된 노트 수")
    private long noteCount;

    @Schema(description = "이번 주 생성된 하이라이트 수")
    private long highlightCount;

    public Instant getWeekStartUtc() {
        return weekStartUtc;
    }

    public void setWeekStartUtc(Instant weekStartUtc) {
        this.weekStartUtc = weekStartUtc;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getNoteCount() {
        return noteCount;
    }

    public void setNoteCount(long noteCount) {
        this.noteCount = noteCount;
    }

    public long getHighlightCount() {
        return highlightCount;
    }

    public void setHighlightCount(long highlightCount) {
        this.highlightCount = highlightCount;
    }
}
