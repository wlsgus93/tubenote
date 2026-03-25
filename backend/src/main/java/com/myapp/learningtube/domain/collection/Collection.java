package com.myapp.learningtube.domain.collection;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.user.User;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "collections",
        indexes = {
            @Index(name = "idx_collections_user_sort", columnList = "user_id,sort_order"),
            @Index(name = "idx_collections_user_updated", columnList = "user_id,updated_at"),
        })
public class Collection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CollectionVisibility visibility = CollectionVisibility.PRIVATE;

    /** 사용자 컬렉션 탭에서의 순서(오름차순). */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /**
     * 대표 썸네일 URL(수동 설정 또는 배치 동기화). 다중 미리보기는 API에서 하위 UserVideo 메타로 조합 가능.
     */
    @Column(name = "cover_thumbnail_url", length = 2048)
    private String coverThumbnailUrl;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    private List<CollectionVideo> collectionVideos = new ArrayList<>();

    protected Collection() {}

    public Collection(User user, String name, String description, CollectionVisibility visibility, int sortOrder) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.visibility = visibility != null ? visibility : CollectionVisibility.PRIVATE;
        this.sortOrder = sortOrder;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CollectionVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(CollectionVisibility visibility) {
        this.visibility = visibility;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCoverThumbnailUrl() {
        return coverThumbnailUrl;
    }

    public void setCoverThumbnailUrl(String coverThumbnailUrl) {
        this.coverThumbnailUrl = coverThumbnailUrl;
    }

    public List<CollectionVideo> getCollectionVideos() {
        return collectionVideos;
    }
}
