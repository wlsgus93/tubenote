package com.myapp.learningtube.domain.note;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Page<Note> findByUserVideo_IdOrderByPinnedDescUpdatedAtDesc(Long userVideoId, Pageable pageable);

    Optional<Note> findByIdAndUserVideo_User_Id(Long id, Long userId);

    long countByUserVideo_Id(Long userVideoId);

    long countByUserVideo_IdAndReviewTargetTrue(Long userVideoId);

    @Query(
            "SELECT n FROM Note n JOIN FETCH n.userVideo uv JOIN FETCH uv.video v WHERE uv.user.id = :userId "
                    + "ORDER BY n.createdAt DESC")
    List<Note> findRecentForDashboard(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Note n JOIN n.userVideo uv WHERE uv.user.id = :userId AND n.createdAt >= :since")
    long countCreatedSince(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT COUNT(n) FROM Note n JOIN n.userVideo uv WHERE uv.user.id = :userId")
    long countAllForUser(@Param("userId") Long userId);

    @Query("SELECT MIN(n.createdAt) FROM Note n JOIN n.userVideo uv WHERE uv.user.id = :userId")
    Instant findMinCreatedAtForUser(@Param("userId") Long userId);
}
