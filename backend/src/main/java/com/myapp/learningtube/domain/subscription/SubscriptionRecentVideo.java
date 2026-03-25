package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.video.Video;
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
 * 구독 채널별 “최근 업로드” 피드 행 — {@link UserVideo} 자동 생성 없이 메타만 연결.
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "subscription_recent_videos",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_subscription_recent_sub_video",
                        columnNames = {"user_subscription_id", "video_id"}),
        indexes = {
            @Index(name = "idx_subscription_recent_sub", columnList = "user_subscription_id"),
            @Index(name = "idx_subscription_recent_video", columnList = "video_id"),
        })
public class SubscriptionRecentVideo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_subscription_id", nullable = false)
    private UserSubscription userSubscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "feed_synced_at", nullable = false)
    private Instant feedSyncedAt;

    protected SubscriptionRecentVideo() {}

    public SubscriptionRecentVideo(UserSubscription userSubscription, Video video, Instant feedSyncedAt) {
        this.userSubscription = userSubscription;
        this.video = video;
        this.feedSyncedAt = feedSyncedAt;
    }

    public UserSubscription getUserSubscription() {
        return userSubscription;
    }

    public Video getVideo() {
        return video;
    }

    public Instant getFeedSyncedAt() {
        return feedSyncedAt;
    }

    public void setFeedSyncedAt(Instant feedSyncedAt) {
        this.feedSyncedAt = feedSyncedAt;
    }
}
