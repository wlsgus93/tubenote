package com.myapp.learningtube.domain.video.support;

/** 외부 API(또는 스텁)에서 가져온 메타 스냅샷. */
public record VideoMetadataSnapshot(
        String youtubeVideoId,
        String title,
        String description,
        Integer durationSeconds,
        String thumbnailUrl,
        String channelTitle,
        String channelYoutubeId) {}
