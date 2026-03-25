package com.myapp.learningtube.domain.video.support;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * YouTube Data API 미연동 시 메타데이터 스텁.
 */
@Component
@Primary
public class StubVideoMetadataAdapter implements VideoMetadataPort {

    @Override
    public VideoMetadataSnapshot fetchByYoutubeVideoId(String youtubeVideoId) {
        return new VideoMetadataSnapshot(
                youtubeVideoId,
                "[Stub] Video " + youtubeVideoId,
                "YouTube API 연동 전 placeholder 설명입니다.",
                120,
                "https://i.ytimg.com/vi/" + youtubeVideoId + "/hqdefault.jpg",
                "Stub Channel",
                "UC_STUB_" + youtubeVideoId.substring(0, Math.min(4, youtubeVideoId.length())));
    }
}
