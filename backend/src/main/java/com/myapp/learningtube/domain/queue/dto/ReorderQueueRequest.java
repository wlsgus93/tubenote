package com.myapp.learningtube.domain.queue.dto;

import com.myapp.learningtube.domain.queue.QueueType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "특정 queueType 안에서 큐 항목 id 전체를 원하는 순서로 재배치")
public class ReorderQueueRequest {

    @NotNull
    @Schema(description = "대상 큐", requiredMode = Schema.RequiredMode.REQUIRED, example = "TODAY")
    private QueueType queueType;

    @NotEmpty
    @Schema(
            description = "정렬 후 LearningQueueItem id 목록 — 현재 해당 큐 멤버와 동일한 집합이어야 함",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> orderedQueueItemIds;

    public QueueType getQueueType() {
        return queueType;
    }

    public void setQueueType(QueueType queueType) {
        this.queueType = queueType;
    }

    public List<Long> getOrderedQueueItemIds() {
        return orderedQueueItemIds;
    }

    public void setOrderedQueueItemIds(List<Long> orderedQueueItemIds) {
        this.orderedQueueItemIds = orderedQueueItemIds;
    }
}
