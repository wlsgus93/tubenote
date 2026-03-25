package com.myapp.learningtube.domain.collection;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionVideoRepository extends JpaRepository<CollectionVideo, Long> {

    Page<CollectionVideo> findByCollection_IdOrderByPositionAscIdAsc(Long collectionId, Pageable pageable);

    boolean existsByCollection_IdAndUserVideo_Id(Long collectionId, Long userVideoId);

    Optional<CollectionVideo> findByCollection_IdAndUserVideo_Id(Long collectionId, Long userVideoId);

    void deleteByCollection_IdAndUserVideo_Id(Long collectionId, Long userVideoId);

    long countByCollection_Id(Long collectionId);

    List<CollectionVideo> findAllByCollection_IdOrderByPositionAscIdAsc(Long collectionId);

    List<CollectionVideo> findTop3ByCollection_IdOrderByPositionAscIdAsc(Long collectionId);

    @Query("SELECT cv.collection.id, COUNT(cv) FROM CollectionVideo cv WHERE cv.collection.id IN :ids GROUP BY cv.collection.id")
    List<Object[]> countGroupedByCollectionIds(@Param("ids") List<Long> ids);

    @Query("SELECT COALESCE(MAX(cv.position), -1) FROM CollectionVideo cv WHERE cv.collection.id = :collectionId")
    int findMaxPositionByCollectionId(@Param("collectionId") Long collectionId);
}
