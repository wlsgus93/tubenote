package com.myapp.learningtube.domain.collection;

import com.myapp.learningtube.domain.collection.dto.CollectionDetailResponse;
import com.myapp.learningtube.domain.collection.dto.CollectionResponse;
import com.myapp.learningtube.domain.collection.dto.CollectionVideoItemResponse;
import com.myapp.learningtube.domain.video.Video;
import java.util.List;

public final class CollectionDtoMapper {

    private CollectionDtoMapper() {}

    public static CollectionResponse toResponse(Collection c, long videoCount) {
        CollectionResponse r = new CollectionResponse();
        r.setCollectionId(c.getId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setVisibility(c.getVisibility());
        r.setSortOrder(c.getSortOrder());
        r.setVideoCount(videoCount);
        r.setCoverThumbnailUrl(c.getCoverThumbnailUrl());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }

    public static CollectionDetailResponse toDetail(Collection c, long videoCount, List<String> previewThumbnails) {
        CollectionDetailResponse d = new CollectionDetailResponse();
        d.setCollectionId(c.getId());
        d.setName(c.getName());
        d.setDescription(c.getDescription());
        d.setVisibility(c.getVisibility());
        d.setSortOrder(c.getSortOrder());
        d.setVideoCount(videoCount);
        d.setCoverThumbnailUrl(c.getCoverThumbnailUrl());
        d.setUpdatedAt(c.getUpdatedAt());
        d.setCreatedAt(c.getCreatedAt());
        d.setPreviewThumbnailUrls(previewThumbnails);
        return d;
    }

    public static CollectionVideoItemResponse toVideoItem(CollectionVideo cv) {
        CollectionVideoItemResponse r = new CollectionVideoItemResponse();
        r.setUserVideoId(cv.getUserVideo().getId());
        r.setPosition(cv.getPosition());
        Video v = cv.getUserVideo().getVideo();
        r.setVideoId(v.getId());
        r.setYoutubeVideoId(v.getYoutubeVideoId());
        r.setTitle(v.getTitle());
        r.setThumbnailUrl(v.getThumbnailUrl());
        r.setUpdatedAt(cv.getUpdatedAt());
        return r;
    }
}
