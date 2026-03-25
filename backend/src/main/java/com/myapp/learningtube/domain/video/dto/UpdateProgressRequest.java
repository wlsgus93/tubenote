package com.myapp.learningtube.domain.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "재생 위치·진행률 갱신 (둘 중 하나 이상 필요)")
public class UpdateProgressRequest {

    @PositiveOrZero
    @Schema(description = "마지막 재생 위치(초)", example = "120")
    private Integer lastPositionSec;

    @Min(0)
    @Max(100)
    @Schema(description = "시청 진행률 0–100", example = "45")
    private Integer watchPercent;

    public Integer getLastPositionSec() {
        return lastPositionSec;
    }

    public void setLastPositionSec(Integer lastPositionSec) {
        this.lastPositionSec = lastPositionSec;
    }

    public Integer getWatchPercent() {
        return watchPercent;
    }

    public void setWatchPercent(Integer watchPercent) {
        this.watchPercent = watchPercent;
    }
}
