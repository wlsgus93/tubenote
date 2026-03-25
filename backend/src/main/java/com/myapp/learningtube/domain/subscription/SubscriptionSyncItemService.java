package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.channel.Channel;
import com.myapp.learningtube.domain.channel.ChannelRepository;
import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.infra.youtube.YoutubeSubscriptionListItem;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 동기화 시 항목 단위 트랜잭션 — 일부 실패해도 나머지 반영. 향후 배치/스케줄러에서 동일 빈 재사용 가능.
 */
@Service
public class SubscriptionSyncItemService {

    public enum Outcome {
        CREATED,
        UPDATED,
        FAILED
    }

    private static final Logger log = LoggerFactory.getLogger(SubscriptionSyncItemService.class);

    private final ChannelRepository channelRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionSyncItemService(
            ChannelRepository channelRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userRepository = userRepository;
    }

    /**
     * 공용 채널 메타 갱신 + 사용자 구독 행 생성/갱신. 확장: 이후 {@code ChannelRecentVideosSync} 를 여기서 후킹할 수 있음.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Outcome merge(Long userId, YoutubeSubscriptionListItem item, Instant now) {
        String ytChannelId = item.channelId();
        if (ytChannelId == null || ytChannelId.isBlank()) {
            return Outcome.FAILED;
        }
        try {
            User user = userRepository.getReferenceById(userId);
            String title = item.title() != null && !item.title().isBlank() ? item.title() : "(제목 없음)";
            String desc = item.description() != null ? item.description() : "";
            String thumb = item.thumbnailUrl() != null && !item.thumbnailUrl().isBlank() ? item.thumbnailUrl() : null;

            Channel channel =
                    channelRepository
                            .findByYoutubeChannelId(ytChannelId)
                            .orElseGet(
                                    () -> channelRepository.save(new Channel(ytChannelId, title, desc, thumb, null)));
            channel.setTitle(title);
            channel.setDescription(desc);
            channel.setThumbnailUrl(thumb);
            channel.setLastSyncedAt(now);
            channelRepository.save(channel);

            var existing = userSubscriptionRepository.findByUser_IdAndChannel_Id(userId, channel.getId());
            if (existing.isEmpty()) {
                UserSubscription us = new UserSubscription(user, channel, item.youtubeSubscriptionId());
                us.setLastSyncedAt(now);
                userSubscriptionRepository.save(us);
                log.debug(
                        "subscription sync created userId={} subscriptionId={} channelId={}",
                        userId,
                        us.getId(),
                        channel.getId());
                return Outcome.CREATED;
            }
            UserSubscription us = existing.get();
            us.setYoutubeSubscriptionId(item.youtubeSubscriptionId());
            us.setLastSyncedAt(now);
            log.debug(
                    "subscription sync updated userId={} subscriptionId={} channelId={}",
                    userId,
                    us.getId(),
                    channel.getId());
            return Outcome.UPDATED;
        } catch (DataIntegrityViolationException e) {
            log.warn("subscription sync integrity userId={} youtubeChannelId={}", userId, ytChannelId);
            return Outcome.FAILED;
        } catch (Exception e) {
            log.warn(
                    "subscription sync item failed userId={} youtubeChannelId={} message={}",
                    userId,
                    ytChannelId,
                    e.getMessage());
            return Outcome.FAILED;
        }
    }
}
