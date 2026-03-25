package com.myapp.learningtube.domain.transcript;

import com.myapp.learningtube.domain.common.BaseEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** 트랙 내 시간순 큐(자막 한 줄). 검색 인덱스는 후속 단계에서 추가. */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "transcript_segments",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_transcript_segments_track_line",
                        columnNames = {"transcript_track_id", "line_index"}),
        indexes = {
            @Index(name = "idx_transcript_segments_track_start", columnList = "transcript_track_id,start_seconds"),
        })
public class TranscriptSegment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "transcript_track_id", nullable = false)
    private TranscriptTrack transcriptTrack;

    @Column(name = "line_index", nullable = false)
    private int lineIndex;

    @Column(name = "start_seconds", nullable = false)
    private double startSeconds;

    @Column(name = "end_seconds", nullable = false)
    private double endSeconds;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    protected TranscriptSegment() {}

    public TranscriptSegment(
            TranscriptTrack transcriptTrack, int lineIndex, double startSeconds, double endSeconds, String text) {
        this.transcriptTrack = transcriptTrack;
        this.lineIndex = lineIndex;
        this.startSeconds = startSeconds;
        this.endSeconds = endSeconds;
        this.text = text;
    }

    public TranscriptTrack getTranscriptTrack() {
        return transcriptTrack;
    }

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
