package com.myapp.learningtube.domain.transcript.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자막 한 구간")
public class TranscriptSegmentResponse {

    @Schema(description = "트랙 내 순번(0부터)", example = "0")
    private int lineIndex;

    @Schema(description = "시작 시각(초)", example = "1.2")
    private double startSeconds;

    @Schema(description = "종료 시각(초)", example = "3.5")
    private double endSeconds;

    @Schema(description = "자막 텍스트")
    private String text;

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public double getStartSeconds() {
        return startSeconds;
    }

    public void setStartSeconds(double startSeconds) {
        this.startSeconds = startSeconds;
    }

    public double getEndSeconds() {
        return endSeconds;
    }

    public void setEndSeconds(double endSeconds) {
        this.endSeconds = endSeconds;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
