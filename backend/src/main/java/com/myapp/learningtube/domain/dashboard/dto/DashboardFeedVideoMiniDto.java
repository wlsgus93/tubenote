package com.myapp.learningtube.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "즐겨찾기 채널 피드 내 영상 미니 카드")
public class DashboardFeedVideoMiniDto {

    private String youtubeVideoId;
    private String title;
    private String thumbnailUrl;
    private Instant publishedAt;

    @JsonProperty("isNew")
    private boolean isNew;

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
}
