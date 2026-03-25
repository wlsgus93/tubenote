package com.myapp.learningtube.domain.queue;

import com.myapp.learningtube.domain.queue.dto.QueueItemResponse;
import com.myapp.learningtube.domain.video.UserVideo;

public final class QueueDtoMapper {

    private QueueDtoMapper() {}

    public static QueueItemResponse toResponse(LearningQueueItem item) {
        UserVideo uv = item.getUserVideo();
        var v = uv.getVideo();
        QueueItemResponse r = new QueueItemResponse();
        r.setQueueItemId(item.getId());
        r.setUserVideoId(uv.getId());
        r.setQueueType(item.getQueueType().name());
        r.setPosition(item.getPosition());
        r.setAddedAt(item.getCreatedAt());
        r.setVideoTitle(v.getTitle());
        r.setThumbnailUrl(v.getThumbnailUrl());
        r.setLearningStatus(uv.getLearningStatus());
        r.setPriority(uv.getPriority());
        r.setProgressSeconds(uv.getLastPositionSec());
        r.setDurationSeconds(v.getDurationSeconds());
        return r;
    }
}
