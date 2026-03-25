package com.myapp.learningtube.domain.queue;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.video.UserVideo;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 사용자 학습 큐 항목. {@link UserVideo}당 최대 1행(모든 {@link QueueType} 통틀어 중복 불가).
 *
 * <p>확장 후보(스키마 미반영): {@code scheduledDate}, 메모, 큐 전용 완료 플래그 등은 동일 테이블에 nullable 컬럼으로
 * 추가 가능.
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "learning_queue_items",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_learning_queue_items_user_user_video",
                        columnNames = {"user_id", "user_video_id"}),
        indexes = {
            @Index(name = "idx_learning_queue_user_type_sort", columnList = "user_id,queue_type,sort_order"),
        })
public class LearningQueueItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_video_id", nullable = false)
    private UserVideo userVideo;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", nullable = false, length = 32)
    private QueueType queueType;

    /** 동일 (user, queueType) 내 0부터 연속 정수. */
    @Column(name = "sort_order", nullable = false)
    private int position;

    protected LearningQueueItem() {}

    public LearningQueueItem(User user, UserVideo userVideo, QueueType queueType, int position) {
        this.user = user;
        this.userVideo = userVideo;
        this.queueType = queueType;
        this.position = position;
    }

    public User getUser() {
        return user;
    }

    public UserVideo getUserVideo() {
        return userVideo;
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public void setQueueType(QueueType queueType) {
        this.queueType = queueType;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
