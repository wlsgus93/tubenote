package com.myapp.learningtube.domain.collection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "컬렉션 상세 — 미리보기 썸네일은 하위 영상에서 최대 3개까지(확장 여지)")
public class CollectionDetailResponse extends CollectionResponse {

    @Schema(description = "생성 시각")
    private Instant createdAt;

    @Schema(description = "정렬 상위 영상 썸네일 미리보기(최대 3)")
    private List<String> previewThumbnailUrls = new ArrayList<>();

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getPreviewThumbnailUrls() {
        return previewThumbnailUrls;
    }

    public void setPreviewThumbnailUrls(List<String> previewThumbnailUrls) {
        this.previewThumbnailUrls = previewThumbnailUrls != null ? previewThumbnailUrls : new ArrayList<>();
    }
}
