package com.myapp.learningtube.domain.highlight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "하이라이트 구간 생성")
public class CreateHighlightRequest {

    @NotNull
    @Min(0)
    @Schema(description = "시작 시각(초)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer startSec;

    @NotNull
    @Min(0)
    @Schema(description = "종료 시각(초), 시작 이상", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer endSec;

    @Size(max = 5000)
    @Schema(description = "구간 메모(선택)")
    private String memo;

    @Schema(description = "복습 대상", defaultValue = "false")
    private boolean reviewTarget;

    @Schema(description = "핀", defaultValue = "false")
    private boolean pinned;

    public Integer getStartSec() {
        return startSec;
    }

    public void setStartSec(Integer startSec) {
        this.startSec = startSec;
    }

    public Integer getEndSec() {
        return endSec;
    }

    public void setEndSec(Integer endSec) {
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
