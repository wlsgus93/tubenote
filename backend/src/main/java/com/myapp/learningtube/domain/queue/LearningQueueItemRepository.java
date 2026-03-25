package com.myapp.learningtube.domain.queue;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningQueueItemRepository extends JpaRepository<LearningQueueItem, Long> {

    Optional<LearningQueueItem> findByIdAndUser_Id(Long id, Long userId);

    Optional<LearningQueueItem> findByUser_IdAndUserVideo_Id(Long userId, Long userVideoId);

    boolean existsByUser_IdAndUserVideo_Id(Long userId, Long userVideoId);

    @Query(
            "SELECT q FROM LearningQueueItem q JOIN FETCH q.userVideo uv JOIN FETCH uv.video v "
                    + "WHERE q.user.id = :userId ORDER BY q.queueType ASC, q.position ASC, q.id ASC")
    List<LearningQueueItem> findAllByUserIdWithVideo(@Param("userId") Long userId);

    @Query(
            "SELECT q FROM LearningQueueItem q JOIN FETCH q.userVideo uv JOIN FETCH uv.video v "
                    + "WHERE q.user.id = :userId AND q.queueType = :queueType ORDER BY q.position ASC, q.id ASC")
    List<LearningQueueItem> findByUserIdAndQueueTypeWithVideo(
            @Param("userId") Long userId, @Param("queueType") QueueType queueType);

    List<LearningQueueItem> findByUser_IdAndQueueTypeOrderByPositionAscIdAsc(Long userId, QueueType queueType);
}
