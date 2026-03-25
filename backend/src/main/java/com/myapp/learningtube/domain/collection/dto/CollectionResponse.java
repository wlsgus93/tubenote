package com.myapp.learningtube.domain.collection.dto;

import com.myapp.learningtube.domain.collection.CollectionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "컬렉션 요약(목록)")
public class CollectionResponse {

    @Schema(description = "컬렉션 PK")
    private Long collectionId;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "공개 범위")
    private CollectionVisibility visibility;

    @Schema(description = "목록 정렬 순서")
    private int sortOrder;

    @Schema(description = "포함된 UserVideo 개수")
    private long videoCount;

    @Schema(description = "대표 썸네일(수동/동기화)")
    private String coverThumbnailUrl;

    @Schema(description = "수정 시각")
    private Instant updatedAt;

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CollectionVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(CollectionVisibility visibility) {
        this.visibility = visibility;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public long getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(long videoCount) {
        this.videoCount = videoCount;
    }

    public String getCoverThumbnailUrl() {
        return coverThumbnailUrl;
    }

    public void setCoverThumbnailUrl(String coverThumbnailUrl) {
        this.coverThumbnailUrl = coverThumbnailUrl;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
