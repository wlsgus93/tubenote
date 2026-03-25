package com.myapp.learningtube.domain.video.dto;

import com.myapp.learningtube.domain.video.LearningStatus;
import com.myapp.learningtube.domain.video.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학습 상태 변경 (우선순위는 선택)")
public class UpdateLearningStateRequest {

    @NotNull
    @Schema(description = "학습 상태", requiredMode = Schema.RequiredMode.REQUIRED)
    private LearningStatus learningStatus;

    @Schema(description = "우선순위; null이면 기존 값 유지")
    private Priority priority;

    public LearningStatus getLearningStatus() {
        return learningStatus;
    }

    public void setLearningStatus(LearningStatus learningStatus) {
        this.learningStatus = learningStatus;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
