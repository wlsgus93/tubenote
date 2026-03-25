package com.myapp.learningtube.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "일별 활동 버킷(UTC 날짜)")
public class AnalyticsDailyPointDto {

    @Schema(description = "날짜(ISO-8601 date, UTC)", example = "2026-03-20")
    private String date;

    @Schema(description = "해당 일 완료 처리된 UserVideo 수(completed_at 기준)")
    private long completedVideoCount;

    @Schema(description = "해당 일 생성된 노트 수")
    private long createdNoteCount;

    @Schema(description = "해당 일 생성된 하이라이트 수")
    private long createdHighlightCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCompletedVideoCount() {
        return completedVideoCount;
    }

    public void setCompletedVideoCount(long completedVideoCount) {
        this.completedVideoCount = completedVideoCount;
    }

    public long getCreatedNoteCount() {
        return createdNoteCount;
    }

    public void setCreatedNoteCount(long createdNoteCount) {
        this.createdNoteCount = createdNoteCount;
    }

    public long getCreatedHighlightCount() {
        return createdHighlightCount;
    }

    public void setCreatedHighlightCount(long createdHighlightCount) {
        this.createdHighlightCount = createdHighlightCount;
    }
}
