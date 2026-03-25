package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.channel.Channel;
import com.myapp.learningtube.domain.subscription.dto.ChannelSummaryResponse;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionResponse;

public final class SubscriptionDtoMapper {

    private SubscriptionDtoMapper() {}

    public static SubscriptionResponse toResponse(UserSubscription us) {
        Channel ch = us.getChannel();
        ChannelSummaryResponse cs = new ChannelSummaryResponse();
        cs.setChannelId(ch.getId());
        cs.setYoutubeChannelId(ch.getYoutubeChannelId());
        cs.setTitle(ch.getTitle());
        cs.setThumbnailUrl(ch.getThumbnailUrl());
        cs.setCustomUrl(ch.getCustomUrl());
        cs.setChannelLastSyncedAt(ch.getLastSyncedAt());

        SubscriptionResponse r = new SubscriptionResponse();
        r.setSubscriptionId(us.getId());
        r.setChannel(cs);
        r.setCategory(us.getCategory());
        r.setFavorite(us.isFavorite());
        r.setLearningChannel(us.isLearningChannel());
        r.setNote(us.getNote());
        r.setLastSyncedAt(us.getLastSyncedAt());
        r.setLastChannelVideosSyncedAt(us.getLastChannelVideosSyncedAt());
        r.setUnreadNewVideoCount(us.getUnreadNewVideoCount());
        r.setCreatedAt(us.getCreatedAt());
        r.setUpdatedAt(us.getUpdatedAt());
        return r;
    }
}
