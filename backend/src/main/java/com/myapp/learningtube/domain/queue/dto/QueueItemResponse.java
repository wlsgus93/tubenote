package com.myapp.learningtube.domain.queue.dto;

import com.myapp.learningtube.domain.video.LearningStatus;
import com.myapp.learningtube.domain.video.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "학습 큐 항목 + UserVideo/Video 요약")
public class QueueItemResponse {

    @Schema(description = "큐 항목 ID", example = "12")
    private Long queueItemId;

    @Schema(description = "UserVideo ID", example = "340")
    private Long userVideoId;

    @Schema(description = "큐 구분", example = "TODAY")
    private String queueType;

    @Schema(description = "동일 queueType 내 순서(0부터 연속)", example = "0")
    private int position;

    @Schema(description = "큐에 추가된 시각(엔티티 createdAt)")
    private Instant addedAt;

    @Schema(description = "영상 제목")
    private String videoTitle;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "학습 상태")
    private LearningStatus learningStatus;

    @Schema(description = "우선순위")
    private Priority priority;

    @Schema(description = "시청 진행 초(UserVideo.lastPositionSec)")
    private int progressSeconds;

    @Schema(description = "영상 길이 초(없으면 null)")
    private Integer durationSeconds;

    public Long getQueueItemId() {
        return queueItemId;
    }

    public void setQueueItemId(Long queueItemId) {
        this.queueItemId = queueItemId;
    }

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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
}
