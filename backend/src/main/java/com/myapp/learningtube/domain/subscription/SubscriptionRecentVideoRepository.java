package com.myapp.learningtube.domain.subscription;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRecentVideoRepository extends JpaRepository<SubscriptionRecentVideo, Long> {

    Optional<SubscriptionRecentVideo> findByUserSubscription_IdAndVideo_Id(Long userSubscriptionId, Long videoId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SubscriptionRecentVideo srv WHERE srv.userSubscription.id = :subId")
    int deleteByUserSubscriptionId(@Param("subId") Long subscriptionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "DELETE FROM SubscriptionRecentVideo srv WHERE srv.userSubscription.id = :subId AND srv.video.id NOT IN :videoIds")
    int deleteByUserSubscriptionIdAndVideoIdNotIn(
            @Param("subId") Long subscriptionId, @Param("videoIds") Collection<Long> videoIds);

    @Query(
            "SELECT COUNT(srv) FROM SubscriptionRecentVideo srv JOIN srv.userSubscription us "
                    + "WHERE us.id = :subId AND us.user.id = :userId AND NOT EXISTS "
                    + "(SELECT 1 FROM UserVideo uv WHERE uv.user.id = :userId AND uv.video.id = srv.video.id)")
    long countNotImportedForUser(@Param("userId") Long userId, @Param("subId") Long subscriptionId);

    @Query(
            "SELECT srv FROM SubscriptionRecentVideo srv JOIN srv.userSubscription us JOIN srv.video v "
                    + "WHERE us.id = :subId AND us.user.id = :userId ORDER BY v.publishedAt DESC, srv.id DESC")
    Page<SubscriptionRecentVideo> findForOwnedSubscription(
            @Param("userId") Long userId, @Param("subId") Long subscriptionId, Pageable pageable);

    @Query(
            "SELECT srv FROM SubscriptionRecentVideo srv JOIN srv.userSubscription us JOIN srv.video v "
                    + "WHERE us.user.id = :userId ORDER BY v.publishedAt DESC, srv.id DESC")
    Page<SubscriptionRecentVideo> findAllForUser(@Param("userId") Long userId, Pageable pageable);
}
