package com.myapp.learningtube.domain.queue;

import com.myapp.learningtube.domain.queue.dto.AddQueueItemRequest;
import com.myapp.learningtube.domain.queue.dto.QueueItemResponse;
import com.myapp.learningtube.domain.queue.dto.ReorderQueueRequest;
import com.myapp.learningtube.domain.queue.dto.UpdateQueueItemRequest;
import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.domain.video.UserVideo;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private final LearningQueueItemRepository learningQueueItemRepository;
    private final UserVideoRepository userVideoRepository;
    private final UserRepository userRepository;

    public QueueService(
            LearningQueueItemRepository learningQueueItemRepository,
            UserVideoRepository userVideoRepository,
            UserRepository userRepository) {
        this.learningQueueItemRepository = learningQueueItemRepository;
        this.userVideoRepository = userVideoRepository;
        this.userRepository = userRepository;
    }

    public List<QueueItemResponse> list(Long userId, QueueType queueTypeFilter) {
        List<LearningQueueItem> rows =
                queueTypeFilter == null
                        ? learningQueueItemRepository.findAllByUserIdWithVideo(userId)
                        : learningQueueItemRepository.findByUserIdAndQueueTypeWithVideo(userId, queueTypeFilter);
        log.debug(
                "queue list userId={} queueTypeFilter={} size={}",
                userId,
                queueTypeFilter,
                rows.size());
        return rows.stream().map(QueueDtoMapper::toResponse).toList();
    }

    @Transactional
    public QueueItemResponse add(Long userId, AddQueueItemRequest body) {
        Long uvId = body.getUserVideoId();
        userVideoRepository
                .findByIdAndUser_Id(uvId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_VIDEO_NOT_FOUND, "내 목록에 없는 영상입니다."));
        if (learningQueueItemRepository.existsByUser_IdAndUserVideo_Id(userId, uvId)) {
            throw new BusinessException(
                    ErrorCode.QUEUE_USER_VIDEO_ALREADY_IN_QUEUE, "이미 학습 큐에 포함된 영상입니다(타입당 1행 정책).");
        }
        User user = userRepository.getReferenceById(userId);
        UserVideo uv = userVideoRepository.getReferenceById(uvId);
        LearningQueueItem item = new LearningQueueItem(user, uv, body.getQueueType(), 0);
        insertNewItemAtIndex(userId, body.getQueueType(), item, body.getPosition());
        learningQueueItemRepository.flush();
        log.info(
                "queue item added userId={} queueItemId={} userVideoId={} queueType={}",
                userId,
                item.getId(),
                uvId,
                item.getQueueType());
        return QueueDtoMapper.toResponse(item);
    }

    @Transactional
    public QueueItemResponse update(Long userId, Long queueItemId, UpdateQueueItemRequest body) {
        if (body.getQueueType() == null && body.getPosition() == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "queueType 또는 position 중 하나 이상을 보내 주세요.");
        }
        LearningQueueItem item = loadOwned(userId, queueItemId);
        QueueType currentType = item.getQueueType();
        QueueType targetType = body.getQueueType() != null ? body.getQueueType() : currentType;

        if (targetType == currentType) {
            if (body.getPosition() != null) {
                moveWithinBucket(userId, item, body.getPosition());
            }
            LearningQueueItem refreshed = loadOwned(userId, queueItemId);
            log.info(
                    "queue item updated userId={} queueItemId={} userVideoId={} queueType={}",
                    userId,
                    refreshed.getId(),
                    refreshed.getUserVideo().getId(),
                    refreshed.getQueueType());
            return QueueDtoMapper.toResponse(refreshed);
        }

        item.setQueueType(targetType);
        learningQueueItemRepository.saveAndFlush(item);
        compactPositions(userId, currentType);
        placeExistingItemAtIndex(userId, targetType, item, body.getPosition());

        LearningQueueItem refreshed = loadOwned(userId, queueItemId);
        log.info(
                "queue item moved userId={} queueItemId={} userVideoId={} fromType={} toType={}",
                userId,
                refreshed.getId(),
                refreshed.getUserVideo().getId(),
                currentType,
                refreshed.getQueueType());
        return QueueDtoMapper.toResponse(refreshed);
    }

    @Transactional
    public void delete(Long userId, Long queueItemId) {
        LearningQueueItem item = loadOwned(userId, queueItemId);
        QueueType t = item.getQueueType();
        Long uvId = item.getUserVideo().getId();
        learningQueueItemRepository.delete(item);
        compactPositions(userId, t);
        log.info("queue item deleted userId={} queueItemId={} userVideoId={} queueType={}", userId, queueItemId, uvId, t);
    }

    @Transactional
    public void reorder(Long userId, ReorderQueueRequest body) {
        QueueType t = body.getQueueType();
        List<Long> ordered = body.getOrderedQueueItemIds();
        if (ordered.size() != new HashSet<>(ordered).size()) {
            throw new BusinessException(ErrorCode.QUEUE_ORDER_INVALID, "orderedQueueItemIds에 중복이 있을 수 없습니다.");
        }
        List<LearningQueueItem> all =
                learningQueueItemRepository.findByUser_IdAndQueueTypeOrderByPositionAscIdAsc(userId, t);
        Set<Long> current = all.stream().map(LearningQueueItem::getId).collect(Collectors.toSet());
        Set<Long> want = new HashSet<>(ordered);
        if (!current.equals(want) || current.size() != ordered.size()) {
            throw new BusinessException(
                    ErrorCode.QUEUE_ORDER_INVALID, "orderedQueueItemIds는 해당 큐의 현재 멤버와 동일한 집합이어야 합니다.");
        }
        Map<Long, LearningQueueItem> byId = all.stream().collect(Collectors.toMap(LearningQueueItem::getId, x -> x));
        for (int i = 0; i < ordered.size(); i++) {
            LearningQueueItem row = byId.get(ordered.get(i));
            if (row == null) {
                throw new BusinessException(ErrorCode.QUEUE_ORDER_INVALID, "알 수 없는 queueItemId가 포함되었습니다.");
            }
            row.setPosition(i);
        }
        learningQueueItemRepository.saveAll(all);
        log.info("queue reordered userId={} queueType={} size={}", userId, t, ordered.size());
    }

    private LearningQueueItem loadOwned(Long userId, Long queueItemId) {
        return learningQueueItemRepository
                .findByIdAndUser_Id(queueItemId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUEUE_ITEM_NOT_FOUND, "큐 항목을 찾을 수 없습니다."));
    }

    /**
     * 동일 bucket 내 0..n-1로 재부여(삭제·타입 이동 후 gap 제거).
     */
    private void compactPositions(Long userId, QueueType bucket) {
        List<LearningQueueItem> members =
                learningQueueItemRepository.findByUser_IdAndQueueTypeOrderByPositionAscIdAsc(userId, bucket);
        boolean dirty = false;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getPosition() != i) {
                members.get(i).setPosition(i);
                dirty = true;
            }
        }
        if (dirty) {
            learningQueueItemRepository.saveAll(members);
        }
    }

    private void insertNewItemAtIndex(Long userId, QueueType bucket, LearningQueueItem newItem, Integer desiredIndex) {
        List<LearningQueueItem> members =
                new ArrayList<>(
                        learningQueueItemRepository.findByUser_IdAndQueueTypeOrderByPositionAscIdAsc(userId, bucket));
        int idx =
                desiredIndex == null
                        ? members.size()
                        : Math.min(Math.max(0, desiredIndex), members.size());
        members.add(idx, newItem);
        for (int i = 0; i < members.size(); i++) {
            members.get(i).setPosition(i);
        }
        learningQueueItemRepository.saveAll(members);
    }

    private void moveWithinBucket(Long userId, LearningQueueItem item, int desiredIndex) {
        QueueType bucket = item.getQueueType();
        List<LearningQueueItem> members =
                new ArrayList<>(
                        learningQueueItemRepository.findByUser_IdAndQueueTypeOrderByPositionAscIdAsc(userId, bucket));
        members.removeIf(x -> x.getId().equals(item.getId()));
        int idx = Math.min(Math.max(0, desiredIndex), members.size());
        members.add(idx, item);
        for (int i = 0; i < members.size(); i++) {
            members.get(i).setPosition(i);
        }
        learningQueueItemRepository.saveAll(members);
    }

    private void placeExistingItemAtIndex(Long userId, QueueType bucket, LearningQueueItem item, Integer desiredIndex) {
        List<LearningQueueItem> members =
                new ArrayList<>(
                        learningQueueItemRepository.findByUser_IdAndQueueTypeOrderByPositionAscIdAsc(userId, bucket));
        members.removeIf(x -> x.getId().equals(item.getId()));
        int idx =
                desiredIndex == null
                        ? members.size()
                        : Math.min(Math.max(0, desiredIndex), members.size());
        members.add(idx, item);
        for (int i = 0; i < members.size(); i++) {
            members.get(i).setPosition(i);
        }
        learningQueueItemRepository.saveAll(members);
    }
}
