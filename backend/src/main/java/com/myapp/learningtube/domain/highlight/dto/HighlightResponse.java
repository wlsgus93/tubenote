package com.myapp.learningtube.domain.highlight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "하이라이트 응답")
public class HighlightResponse {

    @Schema(description = "하이라이트 PK")
    private Long highlightId;

    @Schema(description = "소속 UserVideo PK")
    private Long userVideoId;

    @Schema(description = "시작(초)")
    private int startSec;

    @Schema(description = "종료(초)")
    private int endSec;

    @Schema(description = "메모")
    private String memo;

    @Schema(description = "복습 대상")
    private boolean reviewTarget;

    @Schema(description = "핀")
    private boolean pinned;

    @Schema(description = "생성 시각(UTC)")
    private Instant createdAt;

    @Schema(description = "수정 시각(UTC)")
    private Instant updatedAt;

    public Long getHighlightId() {
        return highlightId;
    }

    public void setHighlightId(Long highlightId) {
        this.highlightId = highlightId;
    }

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
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
