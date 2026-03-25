package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.video.Video;
import com.myapp.learningtube.domain.video.VideoRepository;
import com.myapp.learningtube.domain.video.VideoSourceType;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.infra.youtube.YoutubeChannelUploadsPort;
import com.myapp.learningtube.infra.youtube.YoutubeUploadVideoItem;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 1건(채널) 단위 트랜잭션 — 채널 실패가 다른 채널에 영향 주지 않음. 배치·스케줄러에서 동일 빈 호출 가능.
 */
@Service
public class SubscriptionChannelUpdatesSyncItemService {

    public record SyncOutcome(int createdVideos, int updatedVideos) {}

    private static final Logger log = LoggerFactory.getLogger(SubscriptionChannelUpdatesSyncItemService.class);

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final VideoRepository videoRepository;
    private final SubscriptionRecentVideoRepository subscriptionRecentVideoRepository;
    private final YoutubeChannelUploadsPort youtubeChannelUploadsPort;

    public SubscriptionChannelUpdatesSyncItemService(
            UserSubscriptionRepository userSubscriptionRepository,
            VideoRepository videoRepository,
            SubscriptionRecentVideoRepository subscriptionRecentVideoRepository,
            YoutubeChannelUploadsPort youtubeChannelUploadsPort) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.videoRepository = videoRepository;
        this.subscriptionRecentVideoRepository = subscriptionRecentVideoRepository;
        this.youtubeChannelUploadsPort = youtubeChannelUploadsPort;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public SyncOutcome syncOne(Long userId, Long subscriptionId, String accessToken, int maxVideos) {
        UserSubscription us =
                userSubscriptionRepository
                        .findByIdAndUser_Id(subscriptionId, userId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.SUBSCRIPTION_NOT_FOUND, "구독 정보를 찾을 수 없습니다."));
        String channelYtId = us.getChannel().getYoutubeChannelId();
        String channelTitle = us.getChannel().getTitle();

        List<YoutubeUploadVideoItem> items =
                youtubeChannelUploadsPort.fetchRecentUploads(accessToken, channelYtId, maxVideos);

        Set<Long> keptVideoIds = new LinkedHashSet<>();
        int createdV = 0;
        int updatedV = 0;
        Instant now = Instant.now();

        for (YoutubeUploadVideoItem item : items) {
            UpsertResult ur = upsertVideo(item, channelTitle, channelYtId);
            keptVideoIds.add(ur.videoId());
            if (ur.created()) {
                createdV++;
            } else {
                updatedV++;
            }
        }

        if (keptVideoIds.isEmpty()) {
            subscriptionRecentVideoRepository.deleteByUserSubscriptionId(subscriptionId);
        } else {
            subscriptionRecentVideoRepository.deleteByUserSubscriptionIdAndVideoIdNotIn(
                    subscriptionId, keptVideoIds);
        }

        for (Long vid : keptVideoIds) {
            SubscriptionRecentVideo row =
                    subscriptionRecentVideoRepository
                            .findByUserSubscription_IdAndVideo_Id(subscriptionId, vid)
                            .orElse(null);
            if (row == null) {
                subscriptionRecentVideoRepository.save(
                        new SubscriptionRecentVideo(us, videoRepository.getReferenceById(vid), now));
            } else {
                row.setFeedSyncedAt(now);
            }
        }

        subscriptionRecentVideoRepository.flush();
        long unread = subscriptionRecentVideoRepository.countNotImportedForUser(userId, subscriptionId);
        us.setUnreadNewVideoCount((int) Math.min(unread, Integer.MAX_VALUE));
        us.setLastChannelVideosSyncedAt(now);

        log.debug(
                "channel updates subscription synced userId={} subscriptionId={} channelId={} videos={} unread={}",
                userId,
                subscriptionId,
                us.getChannel().getId(),
                keptVideoIds.size(),
                unread);
        return new SyncOutcome(createdV, updatedV);
    }

    private UpsertResult upsertVideo(YoutubeUploadVideoItem item, String channelTitle, String channelYtId) {
        String ytId = item.youtubeVideoId();
        if (ytId == null || ytId.isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "YouTube video id가 비어 있습니다.");
        }
        var existing = videoRepository.findByYoutubeVideoId(ytId);
        if (existing.isPresent()) {
            Video v = existing.get();
            applyVideoFields(v, item, channelTitle, channelYtId);
            return new UpsertResult(v.getId(), false);
        }
        Video v =
                new Video(
                        ytId,
                        safeTitle(item.title()),
                        item.description(),
                        null,
                        blankToNull(item.thumbnailUrl()),
                        channelTitle,
                        channelYtId,
                        VideoSourceType.YOUTUBE);
        v.setPublishedAt(item.publishedAt());
        Video saved = videoRepository.save(v);
        return new UpsertResult(saved.getId(), true);
    }

    private static void applyVideoFields(
            Video v, YoutubeUploadVideoItem item, String channelTitle, String channelYtId) {
        v.setTitle(safeTitle(item.title()));
        v.setDescription(item.description());
        v.setThumbnailUrl(blankToNull(item.thumbnailUrl()));
        v.setChannelTitle(channelTitle);
        v.setChannelYoutubeId(channelYtId);
        if (item.publishedAt() != null) {
            v.setPublishedAt(item.publishedAt());
        }
    }

    private static String safeTitle(String title) {
        return title != null && !title.isBlank() ? title : "(제목 없음)";
    }

    private static String blankToNull(String s) {
        return s != null && !s.isBlank() ? s : null;
    }

    private record UpsertResult(long videoId, boolean created) {}
}
