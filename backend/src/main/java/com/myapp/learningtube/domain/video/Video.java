package com.myapp.learningtube.domain.video;

import com.myapp.learningtube.domain.common.BaseEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * 플랫폼 공용 영상 메타데이터 (YouTube ID 기준 1행).
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "videos",
        uniqueConstraints = @UniqueConstraint(name = "uk_videos_youtube_video_id", columnNames = "youtube_video_id"),
        indexes = {
            @Index(name = "idx_videos_channel_youtube_id", columnList = "channel_youtube_id"),
        })
public class Video extends BaseEntity {

    @Column(name = "youtube_video_id", nullable = false, length = 32)
    private String youtubeVideoId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Column(name = "channel_title", length = 255)
    private String channelTitle;

    @Column(name = "channel_youtube_id", length = 64)
    private String channelYoutubeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private VideoSourceType sourceType = VideoSourceType.YOUTUBE;

    /** YouTube 업로드 시각(UTC). 동기화·피드 정렬용. */
    @Column(name = "published_at")
    private Instant publishedAt;

    protected Video() {}

    public Video(
            String youtubeVideoId,
            String title,
            String description,
            Integer durationSeconds,
            String thumbnailUrl,
            String channelTitle,
            String channelYoutubeId,
            VideoSourceType sourceType) {
        this.youtubeVideoId = youtubeVideoId;
        this.title = title;
        this.description = description;
        this.durationSeconds = durationSeconds;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;
        this.channelYoutubeId = channelYoutubeId;
        this.sourceType = sourceType != null ? sourceType : VideoSourceType.YOUTUBE;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public String getChannelYoutubeId() {
        return channelYoutubeId;
    }

    public VideoSourceType getSourceType() {
        return sourceType;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public void setChannelYoutubeId(String channelYoutubeId) {
        this.channelYoutubeId = channelYoutubeId;
    }

    public void setSourceType(VideoSourceType sourceType) {
        this.sourceType = sourceType != null ? sourceType : VideoSourceType.YOUTUBE;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
