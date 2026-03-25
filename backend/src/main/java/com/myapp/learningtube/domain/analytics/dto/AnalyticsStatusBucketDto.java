package com.myapp.learningtube.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "학습 상태별 건수(보관함 제외)")
public class AnalyticsStatusBucketDto {

    @Schema(description = "LearningStatus 이름")
    private String learningStatus;

    @Schema(description = "UserVideo 건수")
    private long count;

    public String getLearningStatus() {
        return learningStatus;
    }

    public void setLearningStatus(String learningStatus) {
        this.learningStatus = learningStatus;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
