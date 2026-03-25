package com.myapp.learningtube.domain.note.dto;

import com.myapp.learningtube.domain.note.NoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "노트 응답")
public class NoteResponse {

    @Schema(description = "노트 PK")
    private Long noteId;

    @Schema(description = "소속 UserVideo PK")
    private Long userVideoId;

    @Schema(description = "유형")
    private NoteType noteType;

    @Schema(description = "본문")
    private String body;

    @Schema(description = "TIMESTAMP 노트의 재생 위치(초); GENERAL은 null")
    private Integer positionSec;

    @Schema(description = "복습 대상")
    private boolean reviewTarget;

    @Schema(description = "핀")
    private boolean pinned;

    @Schema(description = "생성 시각(UTC)")
    private Instant createdAt;

    @Schema(description = "수정 시각(UTC)")
    private Instant updatedAt;

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
