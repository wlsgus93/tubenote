package com.myapp.learningtube.infra.youtube;

import java.util.List;

/**
 * 채널의 최근 업로드 영상 목록. 구현: {@code channels.list} → uploads playlist → {@code playlistItems.list}.
 */
public interface YoutubeChannelUploadsPort {

    boolean requiresOAuthAccessToken();

    List<YoutubeUploadVideoItem> fetchRecentUploads(String accessToken, String channelYoutubeId, int maxResults);
}
