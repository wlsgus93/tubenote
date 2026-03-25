package com.myapp.learningtube.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "즐겨찾기 구독 채널 + 최근 피드 일부")
public class DashboardFavoriteChannelDto {

    @Schema(description = "UserSubscription PK")
    private Long subscriptionId;

    @Schema(description = "내부 Channel PK")
    private Long channelId;

    @Schema(description = "채널 제목")
    private String channelTitle;

    @Schema(description = "UserVideo 미등록 피드 영상 수")
    private int unreadNewVideoCount;

    @Schema(description = "최근 피드 영상(개수 제한)")
    private List<DashboardFeedVideoMiniDto> recentVideos = new ArrayList<>();

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public int getUnreadNewVideoCount() {
        return unreadNewVideoCount;
    }

    public void setUnreadNewVideoCount(int unreadNewVideoCount) {
        this.unreadNewVideoCount = unreadNewVideoCount;
    }

    public List<DashboardFeedVideoMiniDto> getRecentVideos() {
        return recentVideos;
    }

    public void setRecentVideos(List<DashboardFeedVideoMiniDto> recentVideos) {
        this.recentVideos = recentVideos;
    }
}
