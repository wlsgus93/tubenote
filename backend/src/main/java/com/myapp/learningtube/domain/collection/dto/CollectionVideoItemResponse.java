package com.myapp.learningtube.domain.collection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "컬렉션에 담긴 항목( UserVideo + 공용 Video 요약 )")
public class CollectionVideoItemResponse {

    @Schema(description = "UserVideo PK")
    private Long userVideoId;

    @Schema(description = "컬렉션 내 표시 순서(오름차순)")
    private int position;

    @Schema(description = "공용 Video PK")
    private Long videoId;

    @Schema(description = "YouTube video id")
    private String youtubeVideoId;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "썸네일")
    private String thumbnailUrl;

    @Schema(description = "항목 추가 후 갱신 시각")
    private Instant updatedAt;

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
