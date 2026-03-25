package com.myapp.learningtube.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공용 채널 메타 요약(내부 DB 기준)")
public class ChannelSummaryResponse {

    @Schema(description = "내부 채널 PK", example = "12")
    private Long channelId;

    @Schema(description = "YouTube 채널 ID", example = "UCxxxxxxxx")
    private String youtubeChannelId;

    @Schema(description = "채널 제목")
    private String title;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "customUrl (있을 때)")
    private String customUrl;

    @Schema(description = "채널 메타 마지막 동기화 시각(UTC)")
    private Instant channelLastSyncedAt;

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getYoutubeChannelId() {
        return youtubeChannelId;
    }

    public void setYoutubeChannelId(String youtubeChannelId) {
        this.youtubeChannelId = youtubeChannelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCustomUrl() {
        return customUrl;
    }

    public void setCustomUrl(String customUrl) {
        this.customUrl = customUrl;
    }

    public Instant getChannelLastSyncedAt() {
        return channelLastSyncedAt;
    }

    public void setChannelLastSyncedAt(Instant channelLastSyncedAt) {
        this.channelLastSyncedAt = channelLastSyncedAt;
    }
}
