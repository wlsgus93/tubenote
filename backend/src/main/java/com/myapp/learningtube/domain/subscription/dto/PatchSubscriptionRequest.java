package com.myapp.learningtube.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "구독(사용자 설정) 부분 수정 — 전달된 필드만 갱신")
public class PatchSubscriptionRequest {

    @Size(max = 100)
    @Schema(description = "카테고리 — 빈 문자열이면 null 저장")
    private String category;

    @Schema(description = "즐겨찾기")
    @JsonProperty("isFavorite")
    private Boolean favorite;

    @Schema(description = "학습 채널 표시")
    @JsonProperty("isLearningChannel")
    private Boolean learningChannel;

    @Size(max = 10000)
    @Schema(description = "메모 — 빈 문자열이면 null 저장")
    private String note;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("isFavorite")
    public Boolean getFavorite() {
        return favorite;
    }

    @JsonProperty("isFavorite")
    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    @JsonProperty("isLearningChannel")
    public Boolean getLearningChannel() {
        return learningChannel;
    }

    @JsonProperty("isLearningChannel")
    public void setLearningChannel(Boolean learningChannel) {
        this.learningChannel = learningChannel;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
