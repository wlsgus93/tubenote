package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.channel.Channel;
import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.user.User;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * 사용자별 YouTube 구독(연결) — 공용 {@link Channel} 참조. 동일 사용자·채널 중복 불가.
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "user_subscriptions",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_user_subscriptions_user_channel", columnNames = {"user_id", "channel_id"}),
        indexes = {
            @Index(name = "idx_user_subscriptions_user_updated", columnList = "user_id,updated_at"),
        })
public class UserSubscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    /** YouTube subscriptions.list 항목 id (재동기화·추후 확장용). */
    @Column(name = "youtube_subscription_id", length = 128)
    private String youtubeSubscriptionId;

    @Column(length = 100)
    private String category;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite;

    @Column(name = "is_learning_channel", nullable = false)
    private boolean learningChannel;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    /**
     * 피드에 있으나 아직 {@code UserVideo}로 담지 않은 영상 수(동기화 후 재계산).
     */
    @Column(name = "unread_new_video_count", nullable = false)
    private int unreadNewVideoCount;

    @Column(name = "last_channel_videos_synced_at")
    private Instant lastChannelVideosSyncedAt;

    protected UserSubscription() {}

    public UserSubscription(User user, Channel channel, String youtubeSubscriptionId) {
        this.user = user;
        this.channel = channel;
        this.youtubeSubscriptionId = youtubeSubscriptionId;
    }

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getYoutubeSubscriptionId() {
        return youtubeSubscriptionId;
    }

    public void setYoutubeSubscriptionId(String youtubeSubscriptionId) {
        this.youtubeSubscriptionId = youtubeSubscriptionId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isLearningChannel() {
        return learningChannel;
    }

    public void setLearningChannel(boolean learningChannel) {
        this.learningChannel = learningChannel;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public int getUnreadNewVideoCount() {
        return unreadNewVideoCount;
    }

    public void setUnreadNewVideoCount(int unreadNewVideoCount) {
        this.unreadNewVideoCount = unreadNewVideoCount;
    }

    public Instant getLastChannelVideosSyncedAt() {
        return lastChannelVideosSyncedAt;
    }

    public void setLastChannelVideosSyncedAt(Instant lastChannelVideosSyncedAt) {
        this.lastChannelVideosSyncedAt = lastChannelVideosSyncedAt;
    }
}
