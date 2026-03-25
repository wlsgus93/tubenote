package com.myapp.learningtube.domain.video;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.user.User;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * 사용자별 학습 상태·진행률·핀·보관함 (User × Video).
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "user_videos",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_user_videos_user_video", columnNames = {"user_id", "video_id"}),
        indexes = {
            @Index(name = "idx_user_videos_user_updated", columnList = "user_id,updated_at"),
            @Index(name = "idx_user_videos_user_archived", columnList = "user_id,archived"),
            @Index(name = "idx_user_videos_user_learning", columnList = "user_id,learning_status"),
        })
public class UserVideo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_status", nullable = false, length = 32)
    private LearningStatus learningStatus = LearningStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Priority priority = Priority.NORMAL;

    @Column(name = "last_position_sec", nullable = false)
    private int lastPositionSec;

    /** 0–100, null 이면 미설정 */
    @Column(name = "watch_percent")
    private Integer watchPercent;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false)
    private boolean archived;

    /** learningStatus가 COMPLETED가 된 최초 시각. COMPLETED가 아니면 null. */
    @Column(name = "completed_at")
    private Instant completedAt;

    protected UserVideo() {}

    public UserVideo(User user, Video video) {
        this.user = user;
        this.video = video;
        this.learningStatus = LearningStatus.NOT_STARTED;
        this.priority = Priority.NORMAL;
        this.lastPositionSec = 0;
        this.pinned = false;
        this.archived = false;
    }

    public User getUser() {
        return user;
    }

    public Video getVideo() {
        return video;
    }

    public LearningStatus getLearningStatus() {
        return learningStatus;
    }

    public void setLearningStatus(LearningStatus learningStatus) {
        this.learningStatus = learningStatus;
    }

    /**
     * 학습 상태 변경 + 완료 시각 정책: COMPLETED 진입 시 최초 1회만 {@code completedAt} 설정, 그 외 상태면 {@code
     * null}.
     */
    public void changeLearningStatus(LearningStatus next) {
        this.learningStatus = next;
        if (next == LearningStatus.COMPLETED) {
            if (this.completedAt == null) {
                this.completedAt = Instant.now();
            }
        } else {
            this.completedAt = null;
        }
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
}
