package com.myapp.learningtube.infra.youtube;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "learningtube.youtube")
public class YoutubeApiProperties {

    /** true면 실제 API 호출 없이 스텁 목록 사용(로컬·CI). */
    private boolean stub = true;

    /** YouTube Data API v3 base URL (path only /subscriptions 등 붙임). */
    private String apiBaseUrl = "https://www.googleapis.com/youtube/v3";

    /** 구독 채널당 최근 업로드 피드에 유지할 영상 수(10~20 권장). */
    private int channelUpdatesMaxVideosPerChannel = 15;

    public boolean isStub() {
        return stub;
    }

    public void setStub(boolean stub) {
        this.stub = stub;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public int getChannelUpdatesMaxVideosPerChannel() {
        return channelUpdatesMaxVideosPerChannel;
    }

    public void setChannelUpdatesMaxVideosPerChannel(int channelUpdatesMaxVideosPerChannel) {
        this.channelUpdatesMaxVideosPerChannel = channelUpdatesMaxVideosPerChannel;
    }
}
