package com.myapp.learningtube.domain.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "보관함(아카이브) 여부")
public class UpdateArchiveRequest {

    @NotNull
    @Schema(description = "아카이브 여부", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean archived;

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
