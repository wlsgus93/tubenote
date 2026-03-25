package com.myapp.learningtube.domain.note;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.video.UserVideo;
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

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "notes",
        indexes = {
            @Index(name = "idx_notes_user_video_updated", columnList = "user_video_id,updated_at"),
            @Index(name = "idx_notes_user_video_review", columnList = "user_video_id,review_target"),
        })
public class Note extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_video_id", nullable = false)
    private UserVideo userVideo;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 32)
    private NoteType noteType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /** GENERAL 이면 null, TIMESTAMP 이면 재생 위치(초) */
    @Column(name = "position_sec")
    private Integer positionSec;

    @Column(name = "review_target", nullable = false)
    private boolean reviewTarget;

    @Column(nullable = false)
    private boolean pinned;

    protected Note() {}

    public Note(UserVideo userVideo, NoteType noteType, String body, Integer positionSec, boolean reviewTarget, boolean pinned) {
        this.userVideo = userVideo;
        this.noteType = noteType;
        this.body = body;
        this.positionSec = positionSec;
        this.reviewTarget = reviewTarget;
        this.pinned = pinned;
    }

    public UserVideo getUserVideo() {
        return userVideo;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getPositionSec() {
        return positionSec;
    }

    public void setPositionSec(Integer positionSec) {
        this.positionSec = positionSec;
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
