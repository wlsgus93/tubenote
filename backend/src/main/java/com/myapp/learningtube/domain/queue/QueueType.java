package com.myapp.learningtube.domain.queue;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학습 큐 구분: 오늘 / 주간 / 백로그")
public enum QueueType {
    @Schema(description = "오늘 볼 영상")
    TODAY,
    @Schema(description = "주간 학습 영상")
    WEEKLY,
    @Schema(description = "백로그")
    BACKLOG
}
