package com.myapp.learningtube.domain.queue.dto;

import com.myapp.learningtube.domain.queue.QueueType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "큐에 UserVideo 추가")
public class AddQueueItemRequest {

    @NotNull
    @Schema(description = "내 UserVideo ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "340")
    private Long userVideoId;

    @NotNull
    @Schema(description = "담을 큐 종류", requiredMode = Schema.RequiredMode.REQUIRED, example = "TODAY")
    private QueueType queueType;

    @Schema(
            description =
                    "삽입 위치(0부터). 생략 시 맨 뒤. 동일 queueType 내 기존 항목은 뒤로 밀림. 이후 position은 0..n-1로 재정렬됨.",
            example = "0")
    private Integer position;

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
    }

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
