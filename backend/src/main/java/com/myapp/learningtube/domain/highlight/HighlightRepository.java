package com.myapp.learningtube.domain.highlight;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    Page<Highlight> findByUserVideo_IdOrderByPinnedDescUpdatedAtDesc(Long userVideoId, Pageable pageable);

    Optional<Highlight> findByIdAndUserVideo_User_Id(Long id, Long userId);

    long countByUserVideo_Id(Long userVideoId);

    long countByUserVideo_IdAndReviewTargetTrue(Long userVideoId);

    @Query(
            "SELECT COUNT(h) FROM Highlight h JOIN h.userVideo uv WHERE uv.user.id = :userId AND h.createdAt >= :since")
    long countCreatedSince(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT COUNT(h) FROM Highlight h JOIN h.userVideo uv WHERE uv.user.id = :userId")
    long countAllForUser(@Param("userId") Long userId);

    @Query("SELECT MIN(h.createdAt) FROM Highlight h JOIN h.userVideo uv WHERE uv.user.id = :userId")
    Instant findMinCreatedAtForUser(@Param("userId") Long userId);
}
