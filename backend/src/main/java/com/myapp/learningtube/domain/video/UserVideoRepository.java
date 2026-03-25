package com.myapp.learningtube.domain.video;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserVideoRepository
        extends JpaRepository<UserVideo, Long>, JpaSpecificationExecutor<UserVideo> {

    Optional<UserVideo> findByIdAndUser_Id(Long id, Long userId);

    @Query(
            "SELECT DISTINCT uv FROM UserVideo uv JOIN FETCH uv.video v WHERE uv.id = :id AND uv.user.id = :userId")
    Optional<UserVideo> findByIdAndUserIdWithVideo(
            @Param("id") Long id, @Param("userId") Long userId);

    boolean existsByUser_IdAndVideo_Id(Long userId, Long videoId);

    @Query(
            "SELECT DISTINCT uv FROM UserVideo uv JOIN FETCH uv.video v WHERE uv.user.id = :userId AND uv.archived = false "
                    + "AND uv.learningStatus IN (com.myapp.learningtube.domain.video.LearningStatus.NOT_STARTED, "
                    + "com.myapp.learningtube.domain.video.LearningStatus.IN_PROGRESS) "
                    + "ORDER BY uv.updatedAt DESC")
    List<UserVideo> findTodayPickCandidates(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "SELECT DISTINCT uv FROM UserVideo uv JOIN FETCH uv.video v WHERE uv.user.id = :userId AND uv.archived = false "
                    + "AND uv.lastPositionSec > 0 AND uv.learningStatus <> com.myapp.learningtube.domain.video.LearningStatus.COMPLETED "
                    + "ORDER BY uv.updatedAt DESC")
    List<UserVideo> findContinueWatching(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "SELECT DISTINCT uv FROM UserVideo uv JOIN FETCH uv.video v WHERE uv.user.id = :userId AND uv.archived = false "
                    + "AND uv.learningStatus IN (com.myapp.learningtube.domain.video.LearningStatus.NOT_STARTED, "
                    + "com.myapp.learningtube.domain.video.LearningStatus.IN_PROGRESS) "
                    + "ORDER BY uv.updatedAt DESC")
    List<UserVideo> findIncompleteVideos(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "SELECT COUNT(uv) FROM UserVideo uv WHERE uv.user.id = :userId AND uv.learningStatus = "
                    + "com.myapp.learningtube.domain.video.LearningStatus.IN_PROGRESS AND uv.archived = false "
                    + "AND uv.updatedAt >= :since")
    long countInProgressUpdatedSince(@Param("userId") Long userId, @Param("since") Instant since);

    @Query(
            "SELECT COUNT(uv) FROM UserVideo uv WHERE uv.user.id = :userId AND uv.completedAt IS NOT NULL "
                    + "AND uv.completedAt >= :since")
    long countCompletedSince(@Param("userId") Long userId, @Param("since") Instant since);

    long countByUser_Id(Long userId);

    long countByUser_IdAndArchivedFalseAndLearningStatus(Long userId, LearningStatus learningStatus);

    @Query(
            "SELECT COUNT(uv) FROM UserVideo uv WHERE uv.user.id = :userId AND uv.archived = false AND "
                    + "uv.learningStatus NOT IN (com.myapp.learningtube.domain.video.LearningStatus.IN_PROGRESS, "
                    + "com.myapp.learningtube.domain.video.LearningStatus.COMPLETED)")
    long countOnHoldExcludedFromActive(@Param("userId") Long userId);

    @Query(
            "SELECT COALESCE(SUM(uv.lastPositionSec), 0) FROM UserVideo uv WHERE uv.user.id = :userId AND "
                    + "uv.archived = false")
    Long sumLastPositionSecForUser(@Param("userId") Long userId);

    @Query(
            "SELECT uv.learningStatus, COUNT(uv) FROM UserVideo uv WHERE uv.user.id = :userId AND "
                    + "uv.archived = false GROUP BY uv.learningStatus")
    List<Object[]> countGroupedByLearningStatus(@Param("userId") Long userId);

    @Query("SELECT MIN(uv.createdAt) FROM UserVideo uv WHERE uv.user.id = :userId")
    Instant findMinCreatedAt(@Param("userId") Long userId);
}
