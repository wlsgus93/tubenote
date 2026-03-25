package com.myapp.learningtube.domain.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "핀 고정 여부")
public class UpdatePinRequest {

    @NotNull
    @Schema(description = "핀 여부", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean pinned;

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }
}
