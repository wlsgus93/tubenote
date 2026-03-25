package com.myapp.learningtube.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "대시보드용 UserVideo 요약 카드")
public class DashboardVideoCardDto {

    @Schema(description = "UserVideo PK")
    private Long userVideoId;

    @Schema(description = "공용 Video PK")
    private Long videoId;

    @Schema(description = "YouTube video id")
    private String youtubeVideoId;

    @Schema(description = "영상 제목")
    private String title;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "채널명 스냅샷")
    private String channelTitle;

    @Schema(description = "학습 상태")
    private String learningStatus;

    @Schema(description = "우선순위")
    private String priority;

    @Schema(description = "마지막 재생 위치(초)")
    private int progressSeconds;

    @Schema(description = "영상 길이(초), 미상 시 null — 상세 API durationSeconds 와 동일 출처")
    private Integer durationSeconds;

    @Schema(description = "시청 진행률 0–100, 미설정 시 null")
    private Integer watchPercent;

    @Schema(description = "UserVideo 수정 시각")
    private Instant updatedAt;

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public String getLearningStatus() {
        return learningStatus;
    }

    public void setLearningStatus(String learningStatus) {
        this.learningStatus = learningStatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getProgressSeconds() {
        return progressSeconds;
    }

    public void setProgressSeconds(int progressSeconds) {
        this.progressSeconds = progressSeconds;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getWatchPercent() {
        return watchPercent;
    }

    public void setWatchPercent(Integer watchPercent) {
        this.watchPercent = watchPercent;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
