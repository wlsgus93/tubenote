package com.myapp.learningtube.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "학습 요약 통계(실시간 집계)")
public class AnalyticsSummaryResponse {

    @Schema(description = "저장(UserVideo) 총 개수(보관함 포함)")
    private long totalSavedVideos;

    @Schema(description = "진행 중(보관함 제외)")
    private long inProgressCount;

    @Schema(description = "완료(보관함 제외)")
    private long completedCount;

    @Schema(description = "미시작·중단 등(보관함 제외, IN_PROGRESS·COMPLETED 제외)")
    private long onHoldCount;

    @Schema(description = "노트 총 개수")
    private long totalNoteCount;

    @Schema(description = "하이라이트 총 개수")
    private long totalHighlightCount;

    @Schema(description = "추정 학습 시간(초) — last_position_sec 합(보관함 제외)")
    private long estimatedLearningSeconds;

    public long getTotalSavedVideos() {
        return totalSavedVideos;
    }

    public void setTotalSavedVideos(long totalSavedVideos) {
        this.totalSavedVideos = totalSavedVideos;
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

    public long getOnHoldCount() {
        return onHoldCount;
    }

    public void setOnHoldCount(long onHoldCount) {
        this.onHoldCount = onHoldCount;
    }

    public long getTotalNoteCount() {
        return totalNoteCount;
    }

    public void setTotalNoteCount(long totalNoteCount) {
        this.totalNoteCount = totalNoteCount;
    }

    public long getTotalHighlightCount() {
        return totalHighlightCount;
    }

    public void setTotalHighlightCount(long totalHighlightCount) {
        this.totalHighlightCount = totalHighlightCount;
    }

    public long getEstimatedLearningSeconds() {
        return estimatedLearningSeconds;
    }

    public void setEstimatedLearningSeconds(long estimatedLearningSeconds) {
        this.estimatedLearningSeconds = estimatedLearningSeconds;
    }
}
