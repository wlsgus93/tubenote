package com.myapp.learningtube.domain.collection;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findByIdAndUser_Id(Long id, Long userId);

    Page<Collection> findByUser_IdOrderBySortOrderAscUpdatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(MAX(c.sortOrder), -1) FROM Collection c WHERE c.user.id = :userId")
    int findMaxSortOrderByUserId(@Param("userId") Long userId);

    /**
     * 동일 사용자 내 이름 중복: LOWER(TRIM(저장값)) 과 정규화 키 비교. {@code excludeId} 가 0 미만이면 제외 없음.
     */
    @Query(
            "SELECT COUNT(c) FROM Collection c WHERE c.user.id = :userId AND LOWER(TRIM(c.name)) = :norm AND "
                    + "(:excludeId < 0 OR c.id <> :excludeId)")
    long countByUserIdAndNormalizedName(
            @Param("userId") Long userId, @Param("norm") String normalizedKey, @Param("excludeId") long excludeId);
}
