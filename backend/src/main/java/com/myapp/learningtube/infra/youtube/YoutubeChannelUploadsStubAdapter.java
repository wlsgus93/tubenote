package com.myapp.learningtube.infra.youtube;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "learningtube.youtube.stub", havingValue = "true", matchIfMissing = true)
public class YoutubeChannelUploadsStubAdapter implements YoutubeChannelUploadsPort {

    @Override
    public boolean requiresOAuthAccessToken() {
        return false;
    }

    @Override
    public List<YoutubeUploadVideoItem> fetchRecentUploads(
            String accessToken, String channelYoutubeId, int maxResults) {
        int n = Math.min(Math.max(maxResults, 1), 20);
        List<YoutubeUploadVideoItem> list = new ArrayList<>();
        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        for (int i = 0; i < n; i++) {
            String vid = "STUB_VID_" + channelYoutubeId.hashCode() + "_" + i;
            list.add(
                    new YoutubeUploadVideoItem(
                            vid,
                            "[Stub] Recent upload " + (i + 1) + " — " + channelYoutubeId,
                            "Stub 채널 최근 업로드 피드(로컬용)",
                            base.plusSeconds((long) (n - i) * 3600),
                            "https://i.ytimg.com/img/no_thumbnail.jpg"));
        }
        return list;
    }
}
