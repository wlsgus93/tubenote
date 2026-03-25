package com.myapp.learningtube.domain.highlight;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.video.UserVideo;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "highlights",
        indexes = {
            @Index(name = "idx_highlights_user_video_updated", columnList = "user_video_id,updated_at"),
            @Index(name = "idx_highlights_user_video_review", columnList = "user_video_id,review_target"),
        })
public class Highlight extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_video_id", nullable = false)
    private UserVideo userVideo;

    @Column(name = "start_sec", nullable = false)
    private int startSec;

    @Column(name = "end_sec", nullable = false)
    private int endSec;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "review_target", nullable = false)
    private boolean reviewTarget;

    @Column(nullable = false)
    private boolean pinned;

    protected Highlight() {}

    public Highlight(UserVideo userVideo, int startSec, int endSec, String memo, boolean reviewTarget, boolean pinned) {
        this.userVideo = userVideo;
        this.startSec = startSec;
        this.endSec = endSec;
        this.memo = memo;
        this.reviewTarget = reviewTarget;
        this.pinned = pinned;
    }

    public UserVideo getUserVideo() {
        return userVideo;
    }

    public int getStartSec() {
        return startSec;
    }

    public void setStartSec(int startSec) {
        this.startSec = startSec;
    }

    public int getEndSec() {
        return endSec;
    }

    public void setEndSec(int endSec) {
        this.endSec = endSec;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean isReviewTarget() {
        return reviewTarget;
    }

    public void setReviewTarget(boolean reviewTarget) {
        this.reviewTarget = reviewTarget;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
