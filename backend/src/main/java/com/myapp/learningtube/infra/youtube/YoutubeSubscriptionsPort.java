package com.myapp.learningtube.infra.youtube;

import java.util.List;

/**
 * YouTube 구독 목록 조회 포트. 실제 호출은 {@code learningtube.youtube.stub=false} 일 때만.
 */
public interface YoutubeSubscriptionsPort {

    /** 스텁 구현은 false — 액세스 토큰 없이 동작. */
    boolean requiresOAuthAccessToken();

    /**
     * 내 채널 구독 목록(페이지네이션 내부 처리). 토큰 필수 여부는 {@link #requiresOAuthAccessToken()}.
     */
    List<YoutubeSubscriptionListItem> fetchMineSubscribedChannels(String accessToken);
}
