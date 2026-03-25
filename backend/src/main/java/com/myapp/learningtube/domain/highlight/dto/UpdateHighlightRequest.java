package com.myapp.learningtube.domain.highlight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "하이라이트 부분 수정")
public class UpdateHighlightRequest {

    @Min(0)
    @Schema(description = "시작 시각(초)")
    private Integer startSec;

    @Min(0)
    @Schema(description = "종료 시각(초)")
    private Integer endSec;

    @Size(max = 5000)
    @Schema(description = "구간 메모")
    private String memo;

    @Schema(description = "복습 대상")
    private Boolean reviewTarget;

    @Schema(description = "핀")
    private Boolean pinned;

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
