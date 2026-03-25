package com.myapp.learningtube.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "채널(Video 메타)별 집계 — 공용 Channel 행이 있으면 channelId 설정")
public class AnalyticsChannelStatDto {

    @Schema(description = "내부 Channel PK — 매핑 없으면 null")
    private Long channelId;

    @Schema(description = "YouTube 채널 ID(없으면 빈 문자열)")
    private String youtubeChannelId;

    @Schema(description = "채널 제목 스냅샷")
    private String channelTitle;

    @Schema(description = "저장된 영상 수(보관함 제외)")
    private long savedVideoCount;

    @Schema(description = "완료 수")
    private long completedVideoCount;

    @Schema(description = "노트 수")
    private long noteCount;

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getYoutubeChannelId() {
        return youtubeChannelId;
    }

    public void setYoutubeChannelId(String youtubeChannelId) {
        this.youtubeChannelId = youtubeChannelId;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public long getSavedVideoCount() {
        return savedVideoCount;
    }

    public void setSavedVideoCount(long savedVideoCount) {
        this.savedVideoCount = savedVideoCount;
    }

    public long getCompletedVideoCount() {
        return completedVideoCount;
    }

    public void setCompletedVideoCount(long completedVideoCount) {
        this.completedVideoCount = completedVideoCount;
    }

    public long getNoteCount() {
        return noteCount;
    }

    public void setNoteCount(long noteCount) {
        this.noteCount = noteCount;
    }
}
