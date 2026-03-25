package com.myapp.learningtube.domain.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독 채널별 최신 업로드 동기화 요약")
public class ChannelUpdatesSyncResponse {

    @Schema(description = "동기화 시도에 성공한 구독(채널) 수")
    private int processedChannels;

    @Schema(description = "이번 실행에서 신규 insert 된 공용 Video 행 수")
    private int createdVideos;

    @Schema(description = "이번 실행에서 기존 Video 메타가 갱신된 수")
    private int updatedVideos;

    @Schema(description = "채널 단위 실패 수(API·파싱·구독 없음 등)")
    private int failedChannels;

    public ChannelUpdatesSyncResponse() {}

    public ChannelUpdatesSyncResponse(
            int processedChannels, int createdVideos, int updatedVideos, int failedChannels) {
        this.processedChannels = processedChannels;
        this.createdVideos = createdVideos;
        this.updatedVideos = updatedVideos;
        this.failedChannels = failedChannels;
    }

    public int getProcessedChannels() {
        return processedChannels;
    }

    public void setProcessedChannels(int processedChannels) {
        this.processedChannels = processedChannels;
    }

    public int getCreatedVideos() {
        return createdVideos;
    }

    public void setCreatedVideos(int createdVideos) {
        this.createdVideos = createdVideos;
    }

    public int getUpdatedVideos() {
        return updatedVideos;
    }

    public void setUpdatedVideos(int updatedVideos) {
        this.updatedVideos = updatedVideos;
    }

    public int getFailedChannels() {
        return failedChannels;
    }

    public void setFailedChannels(int failedChannels) {
        this.failedChannels = failedChannels;
    }
}
