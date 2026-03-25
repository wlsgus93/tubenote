package com.myapp.learningtube.domain.dashboard;

import com.myapp.learningtube.domain.dashboard.dto.DashboardFeedVideoMiniDto;
import com.myapp.learningtube.domain.dashboard.dto.DashboardNoteCardDto;
import com.myapp.learningtube.domain.dashboard.dto.DashboardVideoCardDto;
import com.myapp.learningtube.domain.note.Note;
import com.myapp.learningtube.domain.subscription.SubscriptionRecentVideo;
import com.myapp.learningtube.domain.video.Priority;
import com.myapp.learningtube.domain.video.UserVideo;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import com.myapp.learningtube.domain.video.Video;

public final class DashboardMapper {

    private static final int NOTE_BODY_PREVIEW_MAX = 120;

    private DashboardMapper() {}

    public static int priorityRank(Priority p) {
        if (p == null) {
            return 99;
        }
        return switch (p) {
            case URGENT -> 0;
            case HIGH -> 1;
            case NORMAL -> 2;
            case LOW -> 3;
        };
    }

    public static DashboardVideoCardDto toVideoCard(UserVideo uv) {
        Video v = uv.getVideo();
        DashboardVideoCardDto d = new DashboardVideoCardDto();
        d.setUserVideoId(uv.getId());
        d.setVideoId(v.getId());
        d.setYoutubeVideoId(v.getYoutubeVideoId());
        d.setTitle(v.getTitle());
        d.setThumbnailUrl(v.getThumbnailUrl());
        d.setChannelTitle(v.getChannelTitle());
        d.setLearningStatus(uv.getLearningStatus().name());
        d.setPriority(uv.getPriority().name());
        d.setProgressSeconds(uv.getLastPositionSec());
        d.setDurationSeconds(v.getDurationSeconds());
        d.setWatchPercent(uv.getWatchPercent());
        d.setUpdatedAt(uv.getUpdatedAt());
        return d;
    }

    public static DashboardNoteCardDto toNoteCard(Note n) {
        DashboardNoteCardDto d = new DashboardNoteCardDto();
        d.setNoteId(n.getId());
        d.setUserVideoId(n.getUserVideo().getId());
        d.setVideoTitle(n.getUserVideo().getVideo().getTitle());
        d.setNoteType(n.getNoteType().name());
        d.setBodyPreview(previewBody(n.getBody(), NOTE_BODY_PREVIEW_MAX));
        d.setCreatedAt(n.getCreatedAt());
        return d;
    }

    public static DashboardFeedVideoMiniDto toFeedMini(
            SubscriptionRecentVideo srv, Long userId, UserVideoRepository userVideoRepository) {
        Video v = srv.getVideo();
        DashboardFeedVideoMiniDto d = new DashboardFeedVideoMiniDto();
        d.setYoutubeVideoId(v.getYoutubeVideoId());
        d.setTitle(v.getTitle());
        d.setThumbnailUrl(v.getThumbnailUrl());
        d.setPublishedAt(v.getPublishedAt());
        d.setNew(!userVideoRepository.existsByUser_IdAndVideo_Id(userId, v.getId()));
        return d;
    }

    private static String previewBody(String body, int maxChars) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String t = body.strip();
        if (t.length() <= maxChars) {
            return t;
        }
        return t.substring(0, maxChars) + "…";
    }
}
