package com.myapp.learningtube.domain.collection;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.video.UserVideo;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 컬렉션 ↔ UserVideo 매핑. 동일 컬렉션에 동일 UserVideo 중복 불가. 정렬은 {@code position}(DB 컬럼 {@code
 * sort_order}).
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "collection_videos",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_collection_videos_collection_user_video", columnNames = {"collection_id", "user_video_id"}),
        indexes = {
            @Index(name = "idx_collection_videos_collection_sort", columnList = "collection_id,sort_order"),
        })
public class CollectionVideo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_video_id", nullable = false)
    private UserVideo userVideo;

    /** 컬렉션 내 표시 순서(오름차순). 기존 스키마 컬럼명은 sort_order 유지. */
    @Column(name = "sort_order", nullable = false)
    private int position;

    protected CollectionVideo() {}

    public CollectionVideo(Collection collection, UserVideo userVideo, int position) {
        this.collection = collection;
        this.userVideo = userVideo;
        this.position = position;
    }

    public Collection getCollection() {
        return collection;
    }

    public UserVideo getUserVideo() {
        return userVideo;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
