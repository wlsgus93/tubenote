package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.channel.Channel;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionRecentVideoResponse;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import com.myapp.learningtube.domain.video.Video;

public final class SubscriptionRecentVideoMapper {

    private SubscriptionRecentVideoMapper() {}

    public static SubscriptionRecentVideoResponse toResponse(
            SubscriptionRecentVideo srv, Long userId, UserVideoRepository userVideoRepository) {
        UserSubscription us = srv.getUserSubscription();
        Channel ch = us.getChannel();
        Video v = srv.getVideo();
        boolean isNew = !userVideoRepository.existsByUser_IdAndVideo_Id(userId, v.getId());

        SubscriptionRecentVideoResponse r = new SubscriptionRecentVideoResponse();
        r.setSubscriptionId(us.getId());
        r.setChannelId(ch.getId());
        r.setChannelTitle(ch.getTitle());
        r.setVideoId(v.getId());
        r.setYoutubeVideoId(v.getYoutubeVideoId());
        r.setTitle(v.getTitle());
        r.setThumbnailUrl(v.getThumbnailUrl());
        r.setPublishedAt(v.getPublishedAt());
        r.setNew(isNew);
        r.setSyncedAt(srv.getFeedSyncedAt());
        return r;
    }
}
