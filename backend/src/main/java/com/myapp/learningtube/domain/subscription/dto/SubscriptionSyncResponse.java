package com.myapp.learningtube.domain.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "YouTube 구독 목록 동기화 결과 요약")
public class SubscriptionSyncResponse {

    @Schema(description = "성공적으로 반영된 항목 수 (신규+갱신)")
    private int syncedCount;

    @Schema(description = "신규 UserSubscription 행 수")
    private int createdCount;

    @Schema(description = "기존 UserSubscription 갱신 수")
    private int updatedCount;

    @Schema(description = "스킵/실패한 항목 수(빈 채널 ID, DB 충돌 등)")
    private int failedCount;

    public SubscriptionSyncResponse() {}

    public SubscriptionSyncResponse(int syncedCount, int createdCount, int updatedCount, int failedCount) {
        this.syncedCount = syncedCount;
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.failedCount = failedCount;
    }

    public int getSyncedCount() {
        return syncedCount;
    }

    public void setSyncedCount(int syncedCount) {
        this.syncedCount = syncedCount;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }
}
