package com.myapp.learningtube.domain.transcript.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동기화 결과 — 업스트림에 자막이 없어도 성공(0건)일 수 있음")
public class TranscriptSyncResponse {

    @Schema(description = "처리한 트랙 수(세그먼트가 있는 트랙만 upsert)")
    private int tracksUpserted;

    @Schema(description = "저장한 세그먼트 총행 수")
    private int segmentsWritten;

    @Schema(description = "외부 어댑터가 트랙을 반환하지 않았거나 모두 빈 큐인 경우")
    private boolean upstreamEmpty;

    @Schema(description = "사람이 읽을 수 있는 요약(스텁·오류 힌트)")
    private String message;

    public int getTracksUpserted() {
        return tracksUpserted;
    }

    public void setTracksUpserted(int tracksUpserted) {
        this.tracksUpserted = tracksUpserted;
    }

    public int getSegmentsWritten() {
        return segmentsWritten;
    }

    public void setSegmentsWritten(int segmentsWritten) {
        this.segmentsWritten = segmentsWritten;
    }

    public boolean isUpstreamEmpty() {
        return upstreamEmpty;
    }

    public void setUpstreamEmpty(boolean upstreamEmpty) {
        this.upstreamEmpty = upstreamEmpty;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
