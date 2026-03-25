package com.myapp.learningtube.domain.video.support;

public interface VideoMetadataPort {

    VideoMetadataSnapshot fetchByYoutubeVideoId(String youtubeVideoId);
}
