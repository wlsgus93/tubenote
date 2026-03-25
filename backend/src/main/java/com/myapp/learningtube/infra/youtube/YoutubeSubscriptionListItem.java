package com.myapp.learningtube.infra.youtube;

/** subscriptions.list 한 항목에서 추출한 최소 필드. */
public record YoutubeSubscriptionListItem(
        String youtubeSubscriptionId, String channelId, String title, String description, String thumbnailUrl) {}
