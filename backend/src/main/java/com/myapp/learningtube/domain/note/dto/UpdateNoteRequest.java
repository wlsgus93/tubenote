package com.myapp.learningtube.domain.note.dto;

import com.myapp.learningtube.domain.note.NoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "노트 부분 수정 — 전달된 필드만 갱신")
public class UpdateNoteRequest {

    @Schema(description = "노트 유형 변경 시 positionSec 정책 재검증")
    private NoteType noteType;

    @Size(max = 20000)
    @Schema(description = "본문")
    private String body;

    @Schema(description = "TIMESTAMP일 때 재생 위치(초)")
    private Integer positionSec;

    @Schema(description = "복습 대상")
    private Boolean reviewTarget;

    @Schema(description = "핀")
    private Boolean pinned;

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

    public Boolean getReviewTarget() {
        return reviewTarget;
    }

    public void setReviewTarget(Boolean reviewTarget) {
        this.reviewTarget = reviewTarget;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }
}
