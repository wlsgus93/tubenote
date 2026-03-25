package com.myapp.learningtube.domain.video.dto;

import com.myapp.learningtube.domain.video.LearningStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "URL 임포트 결과 (UserVideo 식별자 중심)")
public class ImportVideoUrlResponse {

    @Schema(description = "사용자-영상 연결 PK (API 경로의 userVideoId)", example = "42")
    private Long userVideoId;

    @Schema(description = "공용 Video PK", example = "10")
    private Long videoId;

    @Schema(description = "YouTube video id", example = "dQw4w9WgXcQ")
    private String youtubeVideoId;

    @Schema(description = "제목(스텁 또는 캐시)")
    private String title;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "초기 학습 상태")
    private LearningStatus learningStatus;

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
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

    public LearningStatus getLearningStatus() {
        return learningStatus;
    }

    public void setLearningStatus(LearningStatus learningStatus) {
        this.learningStatus = learningStatus;
    }
}
