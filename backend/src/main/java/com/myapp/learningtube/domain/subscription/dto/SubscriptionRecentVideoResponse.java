package com.myapp.learningtube.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "구독 채널 피드의 최근 업로드 1건(내부 DB)")
public class SubscriptionRecentVideoResponse {

    @Schema(description = "UserSubscription PK")
    private Long subscriptionId;

    @Schema(description = "내부 Channel PK")
    private Long channelId;

    @Schema(description = "채널 제목 스냅샷")
    private String channelTitle;

    @Schema(description = "내부 Video PK")
    private Long videoId;

    @Schema(description = "YouTube video id")
    private String youtubeVideoId;

    @Schema(description = "영상 제목")
    private String title;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "YouTube 공개 시각(가능한 경우)")
    private Instant publishedAt;

    @Schema(description = "아직 내 UserVideo로 담지 않은 영상이면 true")
    @JsonProperty("isNew")
    private boolean isNew;

    @Schema(description = "피드에 반영된 시각(마지막 sync)")
    private Instant syncedAt;

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
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

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    @JsonProperty("isNew")
    public boolean isNew() {
        return isNew;
    }

    @JsonProperty("isNew")
    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Instant syncedAt) {
        this.syncedAt = syncedAt;
    }
}
