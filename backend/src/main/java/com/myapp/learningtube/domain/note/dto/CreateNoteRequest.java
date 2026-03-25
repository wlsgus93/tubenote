package com.myapp.learningtube.domain.note.dto;

import com.myapp.learningtube.domain.note.NoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "노트 생성")
public class CreateNoteRequest {

    @NotNull
    @Schema(description = "노트 유형", requiredMode = Schema.RequiredMode.REQUIRED)
    private NoteType noteType;

    @NotBlank
    @Size(max = 20000)
    @Schema(description = "본문", requiredMode = Schema.RequiredMode.REQUIRED)
    private String body;

    @Schema(description = "TIMESTAMP 타입일 때 필수(초, 0 이상). GENERAL이면 생략(null).")
    private Integer positionSec;

    @Schema(description = "복습 대상 여부", defaultValue = "false")
    private boolean reviewTarget;

    @Schema(description = "핀", defaultValue = "false")
    private boolean pinned;

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
