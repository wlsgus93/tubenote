package com.myapp.learningtube.infra.youtube.transcript;

import java.util.List;

/**
 * 영상별 자막·캡션 수집. REST 구현은 captions.list + 다운로드 파이프라인 등으로 확장.
 */
public interface YoutubeTranscriptPort {

    /**
     * @param youtubeVideoId YouTube video id (11자 등)
     * @return 수집된 트랙(세그먼트 포함). 없으면 빈 리스트 — 정상 케이스.
     */
    List<FetchedTranscriptTrack> fetchTracks(String youtubeVideoId);
}
