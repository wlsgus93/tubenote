package com.myapp.learningtube.infra.youtube;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * YouTube API 미연동 시 고정 2채널 반환. 최근 업로드 동기화 없음.
 */
@Component
@ConditionalOnProperty(name = "learningtube.youtube.stub", havingValue = "true", matchIfMissing = true)
public class YoutubeSubscriptionsStubAdapter implements YoutubeSubscriptionsPort {

    @Override
    public boolean requiresOAuthAccessToken() {
        return false;
    }

    @Override
    public List<YoutubeSubscriptionListItem> fetchMineSubscribedChannels(String accessToken) {
        return List.of(
                new YoutubeSubscriptionListItem(
                        "stub_sub_1",
                        "UC_STUB_DEMO_01",
                        "[Stub] Learning Channel A",
                        "YouTube 연동 전 데모 채널입니다.",
                        "https://i.ytimg.com/img/no_thumbnail.jpg"),
                new YoutubeSubscriptionListItem(
                        "stub_sub_2",
                        "UC_STUB_DEMO_02",
                        "[Stub] Learning Channel B",
                        "배치·스케줄러 확장 시 실제 API로 교체됩니다.",
                        "https://i.ytimg.com/img/no_thumbnail.jpg"));
    }
}
