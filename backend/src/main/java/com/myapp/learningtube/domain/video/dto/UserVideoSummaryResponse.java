package com.myapp.learningtube.domain.video.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myapp.learningtube.domain.video.LearningStatus;
import com.myapp.learningtube.domain.video.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "내 영상 목록 항목")
public class UserVideoSummaryResponse {

    @Schema(description = "UserVideo PK", example = "42")
    private Long userVideoId;

    @Schema(description = "공용 Video PK")
    private Long videoId;

    @Schema(description = "YouTube video id")
    private String youtubeVideoId;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "썸네일")
    private String thumbnailUrl;

    @Schema(description = "채널명")
    private String channelTitle;

    @Schema(description = "학습 상태")
    private LearningStatus learningStatus;

    @Schema(description = "우선순위")
    private Priority priority;

    @Schema(description = "마지막 재생 위치(초)")
    private int lastPositionSec;

    @Schema(description = "시청 진행률 0–100, 미설정 시 null")
    private Integer watchPercent;

    @Schema(description = "핀 고정")
    private boolean pinned;

    @Schema(description = "보관함(아카이브)")
    private boolean archived;

    @Schema(description = "학습 완료(COMPLETED)로 처음 전환된 시각(UTC); 미완료·재진행 시 null")
    private Instant completedAt;

    @Schema(description = "수정 시각(UTC)")
    private Instant updatedAt;

    @Schema(description = "YouTube 업로드 시각(UTC), 공용 Video 메타 없으면 null")
    private Instant videoPublishedAt;

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

    public LearningStatus getLearningStatus() {
        return learningStatus;
    }

    public void setLearningStatus(LearningStatus learningStatus) {
        this.learningStatus = learningStatus;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getLastPositionSec() {
        return lastPositionSec;
    }

    public void setLastPositionSec(int lastPositionSec) {
        this.lastPositionSec = lastPositionSec;
    }

    /** 대시보드 {@code DashboardVideoCardDto.progressSeconds} 와 동일 값(중복 필드로 프론트 매핑 단순화). */
    @Schema(description = "진행 위치(초) — lastPositionSec 과 동일")
    public int getProgressSeconds() {
        return lastPositionSec;
    }

    public Integer getWatchPercent() {
        return watchPercent;
    }

    public void setWatchPercent(Integer watchPercent) {
        this.watchPercent = watchPercent;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getVideoPublishedAt() {
        return videoPublishedAt;
    }

    public void setVideoPublishedAt(Instant videoPublishedAt) {
        this.videoPublishedAt = videoPublishedAt;
    }
}
