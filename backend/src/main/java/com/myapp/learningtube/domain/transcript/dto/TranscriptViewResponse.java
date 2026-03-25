package com.myapp.learningtube.domain.transcript.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "선택된 트랙 기준 자막 뷰 — 없으면 정상적으로 빈 필드")
public class TranscriptViewResponse {

    @Schema(description = "공용 Video PK", example = "12")
    private Long videoId;

    @Schema(description = "DB에 트랙이 하나라도 있는지")
    private boolean tracksAvailable;

    @Schema(description = "선택된 트랙이 있는지(없으면 segments 비어 있음)")
    private boolean hasSelectedTrack;

    @Schema(description = "선택된 트랙 메타; 없으면 null")
    private TranscriptTrackSummaryResponse selectedTrack;

    @Schema(description = "선택 트랙의 세그먼트(시간순); 없으면 빈 배열")
    private List<TranscriptSegmentResponse> segments = new ArrayList<>();

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public boolean isTracksAvailable() {
        return tracksAvailable;
    }

    public void setTracksAvailable(boolean tracksAvailable) {
        this.tracksAvailable = tracksAvailable;
    }

    public boolean isHasSelectedTrack() {
        return hasSelectedTrack;
    }

    public void setHasSelectedTrack(boolean hasSelectedTrack) {
        this.hasSelectedTrack = hasSelectedTrack;
    }

    public TranscriptTrackSummaryResponse getSelectedTrack() {
        return selectedTrack;
    }

    public void setSelectedTrack(TranscriptTrackSummaryResponse selectedTrack) {
        this.selectedTrack = selectedTrack;
    }

    public List<TranscriptSegmentResponse> getSegments() {
        return segments;
    }

    public void setSegments(List<TranscriptSegmentResponse> segments) {
        this.segments = segments != null ? segments : new ArrayList<>();
    }
}
