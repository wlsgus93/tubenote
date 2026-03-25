package com.myapp.learningtube.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "사용자 구독(연결) + 공용 채널 요약")
public class SubscriptionResponse {

    @Schema(description = "UserSubscription PK", example = "101")
    private Long subscriptionId;

    @Schema(description = "연결된 공용 채널")
    private ChannelSummaryResponse channel;

    @Schema(description = "사용자 지정 카테고리")
    private String category;

    @Schema(description = "즐겨찾기")
    @JsonProperty("isFavorite")
    private boolean favorite;

    @Schema(description = "학습 채널로 표시")
    @JsonProperty("isLearningChannel")
    private boolean learningChannel;

    @Schema(description = "메모")
    private String note;

    @Schema(description = "YouTube 구독 동기화로 마지막 반영된 시각")
    private Instant lastSyncedAt;

    @Schema(description = "채널 최신 업로드 피드 동기화 시각")
    private Instant lastChannelVideosSyncedAt;

    @Schema(
            description =
                    "피드에 있으나 아직 UserVideo로 담지 않은 영상 수(채널 업로드 sync 후 재계산)")
    private int unreadNewVideoCount;

    @Schema(description = "생성 시각(UTC)")
    private Instant createdAt;

    @Schema(description = "수정 시각(UTC)")
    private Instant updatedAt;

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public ChannelSummaryResponse getChannel() {
        return channel;
    }

    public void setChannel(ChannelSummaryResponse channel) {
        this.channel = channel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isLearningChannel() {
        return learningChannel;
    }

    public void setLearningChannel(boolean learningChannel) {
        this.learningChannel = learningChannel;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Instant getLastChannelVideosSyncedAt() {
        return lastChannelVideosSyncedAt;
    }

    public void setLastChannelVideosSyncedAt(Instant lastChannelVideosSyncedAt) {
        this.lastChannelVideosSyncedAt = lastChannelVideosSyncedAt;
    }

    public int getUnreadNewVideoCount() {
        return unreadNewVideoCount;
    }

    public void setUnreadNewVideoCount(int unreadNewVideoCount) {
        this.unreadNewVideoCount = unreadNewVideoCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
