package com.myapp.learningtube.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "컬렉션별 영상·완료 집계")
public class AnalyticsCollectionStatDto {

    @Schema(description = "Collection PK")
    private Long collectionId;

    @Schema(description = "컬렉션 이름")
    private String collectionName;

    @Schema(description = "담긴 UserVideo 수")
    private long videoCount;

    @Schema(description = "그 중 학습 완료 수")
    private long completedVideoCount;

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public long getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(long videoCount) {
        this.videoCount = videoCount;
    }

    public long getCompletedVideoCount() {
        return completedVideoCount;
    }

    public void setCompletedVideoCount(long completedVideoCount) {
        this.completedVideoCount = completedVideoCount;
    }
}
