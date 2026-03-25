package com.myapp.learningtube.infra.youtube;

import java.time.Instant;

/**
 * 채널 업로드 플레이리스트(또는 검색)에서 가져온 단일 영상 스냅샷.
 */
public record YoutubeUploadVideoItem(
        String youtubeVideoId, String title, String description, Instant publishedAt, String thumbnailUrl) {}
