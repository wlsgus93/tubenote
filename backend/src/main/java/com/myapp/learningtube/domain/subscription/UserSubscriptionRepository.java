package com.myapp.learningtube.domain.subscription;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndChannel_Id(Long userId, Long channelId);

    Optional<UserSubscription> findByUser_IdAndChannel_Id(Long userId, Long channelId);

    Page<UserSubscription> findByUser_IdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    @Query(
            "SELECT us FROM UserSubscription us JOIN FETCH us.channel WHERE us.id = :id AND us.user.id = :userId")
    Optional<UserSubscription> findByIdAndUserIdWithChannel(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT us FROM UserSubscription us JOIN FETCH us.channel WHERE us.user.id = :userId")
    List<UserSubscription> findAllByUserIdWithChannel(@Param("userId") Long userId);

    List<UserSubscription> findByUser_IdAndFavoriteTrueOrderByUnreadNewVideoCountDescUpdatedAtDesc(
            Long userId, Pageable pageable);
}
