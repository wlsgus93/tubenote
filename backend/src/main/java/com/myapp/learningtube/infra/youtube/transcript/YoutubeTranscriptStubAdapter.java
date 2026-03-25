package com.myapp.learningtube.infra.youtube.transcript;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * STEP 17: 실제 YouTube captions API 미연동 시 빈 결과 반환. 배치·REST 어댑터로 교체 가능.
 */
@Component
public class YoutubeTranscriptStubAdapter implements YoutubeTranscriptPort {

    private static final Logger log = LoggerFactory.getLogger(YoutubeTranscriptStubAdapter.class);

    @Override
    public List<FetchedTranscriptTrack> fetchTracks(String youtubeVideoId) {
        log.debug("transcript stub fetch youtubeVideoId={} (no upstream call)", youtubeVideoId);
        return List.of();
    }
}
