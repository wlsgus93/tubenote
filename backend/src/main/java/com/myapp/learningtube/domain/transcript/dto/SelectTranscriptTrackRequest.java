package com.myapp.learningtube.domain.transcript.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "표시할 자막 트랙 선택")
public class SelectTranscriptTrackRequest {

    @NotNull
    @Schema(description = "선택할 TranscriptTrack id", requiredMode = Schema.RequiredMode.REQUIRED, example = "9")
    private Long trackId;

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }
}
