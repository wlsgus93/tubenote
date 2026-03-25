package com.myapp.learningtube.domain.collection;

import com.myapp.learningtube.domain.collection.dto.AddCollectionVideoRequest;
import com.myapp.learningtube.domain.collection.dto.CollectionDetailResponse;
import com.myapp.learningtube.domain.collection.dto.CollectionResponse;
import com.myapp.learningtube.domain.collection.dto.CollectionVideoItemResponse;
import com.myapp.learningtube.domain.collection.dto.CreateCollectionRequest;
import com.myapp.learningtube.domain.collection.dto.ReorderCollectionVideosRequest;
import com.myapp.learningtube.domain.collection.dto.UpdateCollectionRequest;
import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.domain.video.UserVideo;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CollectionService {

    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

    private final CollectionRepository collectionRepository;
    private final CollectionVideoRepository collectionVideoRepository;
    private final UserRepository userRepository;
    private final UserVideoRepository userVideoRepository;

    public CollectionService(
            CollectionRepository collectionRepository,
            CollectionVideoRepository collectionVideoRepository,
            UserRepository userRepository,
            UserVideoRepository userVideoRepository) {
        this.collectionRepository = collectionRepository;
        this.collectionVideoRepository = collectionVideoRepository;
        this.userRepository = userRepository;
        this.userVideoRepository = userVideoRepository;
    }

    @Transactional
    public CollectionResponse create(Long userId, CreateCollectionRequest body) {
        User user = userRepository.getReferenceById(userId);
        String nameTrimmed = CollectionNamePolicy.trimName(body.getName());
        if (nameTrimmed.isEmpty()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "컬렉션 이름은 공백만일 수 없습니다.");
        }
        assertCollectionNameUnique(userId, nameTrimmed, -1L);
        int nextOrder = collectionRepository.findMaxSortOrderByUserId(userId) + 1;
        Collection c =
                new Collection(
                        user,
                        nameTrimmed,
                        body.getDescription(),
                        body.getVisibility() != null ? body.getVisibility() : CollectionVisibility.PRIVATE,
                        nextOrder);
        if (body.getCoverThumbnailUrl() != null && !body.getCoverThumbnailUrl().isBlank()) {
            c.setCoverThumbnailUrl(body.getCoverThumbnailUrl());
        }
        Collection saved = collectionRepository.save(c);
        log.info("collection created userId={} collectionId={}", userId, saved.getId());
        return CollectionDtoMapper.toResponse(saved, 0L);
    }

    public Page<CollectionResponse> listCollections(Long userId, int pageOneBased, int size) {
        int pageIndex = Math.max(0, pageOneBased - 1);
        Page<Collection> page =
                collectionRepository.findByUser_IdOrderBySortOrderAscUpdatedAtDesc(
                        userId, PageRequest.of(pageIndex, size));
        List<Long> ids = page.getContent().stream().map(Collection::getId).toList();
        Map<Long, Long> counts = videoCountsForCollectionIds(ids);
        return page.map(col -> CollectionDtoMapper.toResponse(col, counts.getOrDefault(col.getId(), 0L)));
    }

    public CollectionDetailResponse getDetail(Long userId, Long collectionId) {
        Collection c = loadOwned(userId, collectionId);
        long cnt = collectionVideoRepository.countByCollection_Id(collectionId);
        List<String> previews = buildPreviewThumbnails(collectionId);
        log.debug("collection detail userId={} collectionId={}", userId, collectionId);
        return CollectionDtoMapper.toDetail(c, cnt, previews);
    }

    @Transactional
    public CollectionResponse update(Long userId, Long collectionId, UpdateCollectionRequest body) {
        if (body.getName() == null
                && body.getDescription() == null
                && body.getVisibility() == null
                && body.getSortOrder() == null
                && body.getCoverThumbnailUrl() == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "수정할 필드를 하나 이상 보내 주세요.");
        }
        Collection c = loadOwned(userId, collectionId);
        if (body.getName() != null) {
            String nameTrimmed = CollectionNamePolicy.trimName(body.getName());
            if (nameTrimmed.isEmpty()) {
                throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "컬렉션 이름은 공백만일 수 없습니다.");
            }
            assertCollectionNameUnique(userId, nameTrimmed, collectionId);
            c.setName(nameTrimmed);
        }
        if (body.getDescription() != null) {
            c.setDescription(body.getDescription());
        }
        if (body.getVisibility() != null) {
            c.setVisibility(body.getVisibility());
        }
        if (body.getSortOrder() != null) {
            c.setSortOrder(body.getSortOrder());
        }
        if (body.getCoverThumbnailUrl() != null) {
            c.setCoverThumbnailUrl(
                    body.getCoverThumbnailUrl().isBlank() ? null : body.getCoverThumbnailUrl());
        }
        log.info("collection updated userId={} collectionId={}", userId, collectionId);
        long cnt = collectionVideoRepository.countByCollection_Id(collectionId);
        return CollectionDtoMapper.toResponse(c, cnt);
    }

    @Transactional
    public void deleteCollection(Long userId, Long collectionId) {
        Collection c = loadOwned(userId, collectionId);
        // 연결된 CollectionVideo는 Collection 쪽 cascade(ALL + orphanRemoval)로 함께 삭제.
        collectionRepository.delete(c);
        log.info("collection deleted userId={} collectionId={}", userId, collectionId);
    }

    @Transactional
    public CollectionVideoItemResponse addVideo(Long userId, Long collectionId, AddCollectionVideoRequest body) {
        Collection c = loadOwned(userId, collectionId);
        Long uvId = body.getUserVideoId();
        userVideoRepository
                .findByIdAndUser_Id(uvId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_VIDEO_NOT_FOUND, "내 목록에 없는 영상입니다."));
        if (collectionVideoRepository.existsByCollection_IdAndUserVideo_Id(collectionId, uvId)) {
            throw new BusinessException(ErrorCode.COLLECTION_VIDEO_DUPLICATE, "이미 이 컬렉션에 포함된 영상입니다.");
        }
        UserVideo uv = userVideoRepository.getReferenceById(uvId);
        int next = collectionVideoRepository.findMaxPositionByCollectionId(collectionId) + 1;
        CollectionVideo cv = new CollectionVideo(c, uv, next);
        CollectionVideo saved = collectionVideoRepository.save(cv);
        log.info(
                "collection video added userId={} collectionId={} userVideoId={}",
                userId,
                collectionId,
                uvId);
        return CollectionDtoMapper.toVideoItem(saved);
    }

    public Page<CollectionVideoItemResponse> listVideos(Long userId, Long collectionId, int pageOneBased, int size) {
        loadOwned(userId, collectionId);
        int pageIndex = Math.max(0, pageOneBased - 1);
        Page<CollectionVideo> page =
                collectionVideoRepository.findByCollection_IdOrderByPositionAscIdAsc(
                        collectionId, PageRequest.of(pageIndex, size));
        log.debug(
                "collection videos list userId={} collectionId={} page={} total={}",
                userId,
                collectionId,
                pageOneBased,
                page.getTotalElements());
        return page.map(CollectionDtoMapper::toVideoItem);
    }

    @Transactional
    public void removeVideo(Long userId, Long collectionId, Long userVideoId) {
        loadOwned(userId, collectionId);
        CollectionVideo cv =
                collectionVideoRepository
                        .findByCollection_IdAndUserVideo_Id(collectionId, userVideoId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.COLLECTION_VIDEO_NOT_FOUND, "컬렉션에 해당 영상이 없습니다."));
        collectionVideoRepository.delete(cv);
        log.info(
                "collection video removed userId={} collectionId={} userVideoId={}",
                userId,
                collectionId,
                userVideoId);
    }

    @Transactional
    public void reorderVideos(Long userId, Long collectionId, ReorderCollectionVideosRequest body) {
        loadOwned(userId, collectionId);
        List<Long> ordered = body.getOrderedUserVideoIds();
        if (ordered.size() != new HashSet<>(ordered).size()) {
            throw new BusinessException(
                    ErrorCode.COLLECTION_VIDEO_ORDER_INVALID, "orderedUserVideoIds에 중복이 있을 수 없습니다.");
        }
        List<CollectionVideo> all =
                collectionVideoRepository.findAllByCollection_IdOrderByPositionAscIdAsc(collectionId);
        Set<Long> current =
                all.stream().map(cv -> cv.getUserVideo().getId()).collect(Collectors.toSet());
        Set<Long> want = new HashSet<>(ordered);
        if (!current.equals(want) || current.size() != ordered.size()) {
            throw new BusinessException(
                    ErrorCode.COLLECTION_VIDEO_ORDER_INVALID,
                    "orderedUserVideoIds는 현재 컬렉션 멤버와 동일한 집합이어야 합니다.");
        }
        Map<Long, CollectionVideo> byUv =
                all.stream().collect(Collectors.toMap(cv -> cv.getUserVideo().getId(), cv -> cv));
        for (int i = 0; i < ordered.size(); i++) {
            CollectionVideo cv = byUv.get(ordered.get(i));
            if (cv == null) {
                throw new BusinessException(
                        ErrorCode.COLLECTION_VIDEO_ORDER_INVALID, "알 수 없는 userVideoId가 포함되었습니다.");
            }
            cv.setPosition(i);
        }
        log.info("collection videos reordered userId={} collectionId={} size={}", userId, collectionId, ordered.size());
    }

    private void assertCollectionNameUnique(Long userId, String nameTrimmed, long excludeCollectionId) {
        String key = CollectionNamePolicy.normalizedKey(nameTrimmed);
        long cnt =
                collectionRepository.countByUserIdAndNormalizedName(userId, key, excludeCollectionId);
        if (cnt > 0) {
            throw new BusinessException(
                    ErrorCode.COLLECTION_NAME_DUPLICATE, "같은 이름의 컬렉션이 이미 있습니다(대소문자·앞뒤 공백 무시).");
        }
    }

    private Collection loadOwned(Long userId, Long collectionId) {
        return collectionRepository
                .findByIdAndUser_Id(collectionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND, "컬렉션을 찾을 수 없습니다."));
    }

    private Map<Long, Long> videoCountsForCollectionIds(List<Long> collectionIds) {
        Map<Long, Long> map = new HashMap<>();
        if (collectionIds.isEmpty()) {
            return map;
        }
        for (Object[] row : collectionVideoRepository.countGroupedByCollectionIds(collectionIds)) {
            Long cid = (Long) row[0];
            long n = row[1] instanceof Long l ? l : ((Number) row[1]).longValue();
            map.put(cid, n);
        }
        return map;
    }

    private List<String> buildPreviewThumbnails(Long collectionId) {
        List<CollectionVideo> top = collectionVideoRepository.findTop3ByCollection_IdOrderByPositionAscIdAsc(collectionId);
        List<String> urls = new ArrayList<>();
        for (CollectionVideo cv : top) {
            String u = cv.getUserVideo().getVideo().getThumbnailUrl();
            if (u != null && !u.isBlank()) {
                urls.add(u);
            }
        }
        return urls;
    }
}
