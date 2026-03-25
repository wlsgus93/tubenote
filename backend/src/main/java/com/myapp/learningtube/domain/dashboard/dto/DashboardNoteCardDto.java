package com.myapp.learningtube.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "대시보드 최근 노트 카드")
public class DashboardNoteCardDto {

    @Schema(description = "Note PK")
    private Long noteId;

    @Schema(description = "소속 UserVideo PK")
    private Long userVideoId;

    @Schema(description = "영상 제목")
    private String videoTitle;

    @Schema(description = "노트 유형")
    private String noteType;

    @Schema(description = "본문 미리보기(길이 제한)")
    private String bodyPreview;

    @Schema(description = "노트 작성(생성) 시각")
    private Instant createdAt;

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }

    public void setBodyPreview(String bodyPreview) {
        this.bodyPreview = bodyPreview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
