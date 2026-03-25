package com.myapp.learningtube.domain.collection.dto;

import com.myapp.learningtube.domain.collection.CollectionVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "컬렉션 생성")
public class CreateCollectionRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(
            description = "이름 — 저장 시 trim. 동일 사용자 내 LOWER(trim(name)) 기준 중복 불가",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 2000)
    @Schema(description = "설명")
    private String description;

    @Schema(description = "공개 범위", defaultValue = "PRIVATE")
    private CollectionVisibility visibility = CollectionVisibility.PRIVATE;

    @Schema(description = "대표 썸네일 URL(선택, 추후 자동 동기화 가능)")
    @Size(max = 2048)
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

    public String getCoverThumbnailUrl() {
        return coverThumbnailUrl;
    }

    public void setCoverThumbnailUrl(String coverThumbnailUrl) {
        this.coverThumbnailUrl = coverThumbnailUrl;
    }
}
