package com.myapp.learningtube.domain.queue.dto;

import com.myapp.learningtube.domain.queue.QueueType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "큐 항목 수정 — queueType·position 중 최소 하나 필수")
public class UpdateQueueItemRequest {

    @Schema(
            description =
                    "다른 큐로 이동. 변경 시 이전 queueType은 남은 항목만으로 position 0..n-1 재압축, 새 큐에서는 지정 position(또는 맨 뒤)에 삽입.",
            example = "WEEKLY")
    private QueueType queueType;

    @Schema(description = "해당 queueType(변경 후 최종 타입) 기준 목표 인덱스(0부터)", example = "1")
    private Integer position;

    public QueueType getQueueType() {
        return queueType;
    }

    public void setQueueType(QueueType queueType) {
        this.queueType = queueType;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
