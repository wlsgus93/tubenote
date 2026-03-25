package com.myapp.learningtube.domain.video.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myapp.learningtube.domain.video.VideoSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "UserVideo 상세 + 공용 Video 메타")
public class UserVideoDetailResponse extends UserVideoSummaryResponse {

    @Schema(description = "영상 설명(길 수 있음)")
    private String description;

    @Schema(description = "재생 길이(초), 미상 시 null")
    private Integer durationSeconds;

    @Schema(description = "메타 출처 유형")
    private VideoSourceType sourceType;

    @Schema(description = "UserVideo 생성 시각")
    private Instant userVideoCreatedAt;

    @Schema(description = "이 UserVideo에 연결된 노트 개수(상세 조회 시 집계)")
    private long noteCount;

    @Schema(description = "하이라이트 개수")
    private long highlightCount;

    @Schema(description = "노트·하이라이트 중 reviewTarget=true 인 항목 수 합계")
    private long reviewTargetCount;

    @Schema(description = "공용 Video 기준 자막 트랙이 DB에 1개 이상 있는지(세그먼트 유무와 무관)")
    private boolean transcriptTracksAvailable;

    @Schema(description = "선택된 자막 트랙이 있는지 — 자막 탭 기본 표시 판단")
    private boolean transcriptHasSelection;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public VideoSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(VideoSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Instant getUserVideoCreatedAt() {
        return userVideoCreatedAt;
    }

    public void setUserVideoCreatedAt(Instant userVideoCreatedAt) {
        this.userVideoCreatedAt = userVideoCreatedAt;
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

    public long getReviewTargetCount() {
        return reviewTargetCount;
    }

    public void setReviewTargetCount(long reviewTargetCount) {
        this.reviewTargetCount = reviewTargetCount;
    }

    public boolean isTranscriptTracksAvailable() {
        return transcriptTracksAvailable;
    }

    public void setTranscriptTracksAvailable(boolean transcriptTracksAvailable) {
        this.transcriptTracksAvailable = transcriptTracksAvailable;
    }

    public boolean isTranscriptHasSelection() {
        return transcriptHasSelection;
    }

    public void setTranscriptHasSelection(boolean transcriptHasSelection) {
        this.transcriptHasSelection = transcriptHasSelection;
    }

    /** 요약 필드까지 한 번에 채우기용 */
    public static UserVideoDetailResponse fromSummary(UserVideoSummaryResponse s) {
        UserVideoDetailResponse d = new UserVideoDetailResponse();
        d.setUserVideoId(s.getUserVideoId());
        d.setVideoId(s.getVideoId());
        d.setYoutubeVideoId(s.getYoutubeVideoId());
        d.setTitle(s.getTitle());
        d.setThumbnailUrl(s.getThumbnailUrl());
        d.setChannelTitle(s.getChannelTitle());
        d.setLearningStatus(s.getLearningStatus());
        d.setPriority(s.getPriority());
        d.setLastPositionSec(s.getLastPositionSec());
        d.setWatchPercent(s.getWatchPercent());
        d.setPinned(s.isPinned());
        d.setArchived(s.isArchived());
        d.setCompletedAt(s.getCompletedAt());
        d.setUpdatedAt(s.getUpdatedAt());
        d.setVideoPublishedAt(s.getVideoPublishedAt());
        return d;
    }
}
