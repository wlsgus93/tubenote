package com.myapp.learningtube.domain.collection.dto;

import com.myapp.learningtube.domain.collection.CollectionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "컬렉션 부분 수정")
public class UpdateCollectionRequest {

    @Size(max = 200)
    @Schema(description = "이름 — trim 후 저장, 동일 사용자 내 대소문자 무시 중복 불가")
    private String name;

    @Size(max = 2000)
    @Schema(description = "설명")
    private String description;

    @Schema(description = "공개 범위")
    private CollectionVisibility visibility;

    @Schema(description = "사용자 목록 내 정렬 순서(작을수록 앞)")
    private Integer sortOrder;

    @Size(max = 2048)
    @Schema(description = "대표 썸네일 URL")
    private String coverThumbnailUrl;

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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCoverThumbnailUrl() {
        return coverThumbnailUrl;
    }

    public void setCoverThumbnailUrl(String coverThumbnailUrl) {
        this.coverThumbnailUrl = coverThumbnailUrl;
    }
}
