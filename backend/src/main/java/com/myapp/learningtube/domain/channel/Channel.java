package com.myapp.learningtube.domain.channel;

import com.myapp.learningtube.domain.common.BaseEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * 공용 YouTube 채널 메타데이터 (채널 ID 기준 1행).
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "channels",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_channels_youtube_channel_id", columnNames = "youtube_channel_id"),
        indexes = {@Index(name = "idx_channels_title", columnList = "title")})
public class Channel extends BaseEntity {

    @Column(name = "youtube_channel_id", nullable = false, length = 64)
    private String youtubeChannelId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Column(name = "custom_url", length = 255)
    private String customUrl;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    protected Channel() {}

    public Channel(String youtubeChannelId, String title, String description, String thumbnailUrl, String customUrl) {
        this.youtubeChannelId = youtubeChannelId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.customUrl = customUrl;
    }

    public String getYoutubeChannelId() {
        return youtubeChannelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCustomUrl() {
        return customUrl;
    }

    public void setCustomUrl(String customUrl) {
        this.customUrl = customUrl;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
