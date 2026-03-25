package com.myapp.learningtube.domain.video;

import com.myapp.learningtube.domain.video.dto.ImportVideoUrlResponse;
import com.myapp.learningtube.domain.video.dto.UserVideoDetailResponse;
import com.myapp.learningtube.domain.video.dto.UserVideoSummaryResponse;

public final class UserVideoDtoMapper {

    private UserVideoDtoMapper() {}

    public static UserVideoSummaryResponse toSummary(UserVideo uv) {
        Video v = uv.getVideo();
        UserVideoSummaryResponse r = new UserVideoSummaryResponse();
        r.setUserVideoId(uv.getId());
        r.setVideoId(v.getId());
        r.setYoutubeVideoId(v.getYoutubeVideoId());
        r.setTitle(v.getTitle());
        r.setThumbnailUrl(v.getThumbnailUrl());
        r.setChannelTitle(v.getChannelTitle());
        r.setLearningStatus(uv.getLearningStatus());
        r.setPriority(uv.getPriority());
        r.setLastPositionSec(uv.getLastPositionSec());
        r.setWatchPercent(uv.getWatchPercent());
        r.setPinned(uv.isPinned());
        r.setArchived(uv.isArchived());
        r.setCompletedAt(uv.getCompletedAt());
        r.setUpdatedAt(uv.getUpdatedAt());
        r.setVideoPublishedAt(v.getPublishedAt());
        return r;
    }

    public static UserVideoDetailResponse toDetail(UserVideo uv) {
        Video v = uv.getVideo();
        UserVideoDetailResponse d = UserVideoDetailResponse.fromSummary(toSummary(uv));
        d.setDescription(v.getDescription());
        d.setDurationSeconds(v.getDurationSeconds());
        d.setSourceType(v.getSourceType());
        d.setUserVideoCreatedAt(uv.getCreatedAt());
        d.setVideoPublishedAt(v.getPublishedAt());
        return d;
    }

    public static ImportVideoUrlResponse toImportResponse(UserVideo uv) {
        Video v = uv.getVideo();
        ImportVideoUrlResponse r = new ImportVideoUrlResponse();
        r.setUserVideoId(uv.getId());
        r.setVideoId(v.getId());
        r.setYoutubeVideoId(v.getYoutubeVideoId());
        r.setTitle(v.getTitle());
        r.setThumbnailUrl(v.getThumbnailUrl());
        r.setLearningStatus(uv.getLearningStatus());
        return r;
    }
}
