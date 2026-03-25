package com.myapp.learningtube.domain.collection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "컬렉션 내 영상 순서 — 현재 멤버와 동일한 집합·순서만 허용")
public class ReorderCollectionVideosRequest {

    @NotEmpty
    @Schema(
            description = "정렬 후 UserVideo id 목록(전체 멤버와 동일한 다중집합)",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> orderedUserVideoIds;

    public List<Long> getOrderedUserVideoIds() {
        return orderedUserVideoIds;
    }

    public void setOrderedUserVideoIds(List<Long> orderedUserVideoIds) {
        this.orderedUserVideoIds = orderedUserVideoIds;
    }
}
