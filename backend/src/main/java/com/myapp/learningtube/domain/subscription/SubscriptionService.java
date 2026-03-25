package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.subscription.dto.PatchSubscriptionRequest;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionResponse;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionSyncResponse;
import com.myapp.learningtube.domain.user.OAuthProvider;
import com.myapp.learningtube.domain.user.UserOAuthAccount;
import com.myapp.learningtube.domain.user.UserOAuthAccountRepository;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.infra.youtube.YoutubeSubscriptionListItem;
import com.myapp.learningtube.infra.youtube.YoutubeSubscriptionsPort;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final YoutubeSubscriptionsPort youtubeSubscriptionsPort;
    private final SubscriptionSyncItemService subscriptionSyncItemService;

    public SubscriptionService(
            UserSubscriptionRepository userSubscriptionRepository,
            UserOAuthAccountRepository userOAuthAccountRepository,
            YoutubeSubscriptionsPort youtubeSubscriptionsPort,
            SubscriptionSyncItemService subscriptionSyncItemService) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.youtubeSubscriptionsPort = youtubeSubscriptionsPort;
        this.subscriptionSyncItemService = subscriptionSyncItemService;
    }

    /**
     * YouTube Data API(또는 스텁)에서 구독 채널 목록을 가져와 공용 Channel / UserSubscription 에 반영.
     * 조회 전용 API와 달리 이 메서드만 외부 연동을 수행한다.
     */
    @Transactional
    public SubscriptionSyncResponse syncFromYoutube(Long userId) {
        String token = resolveGoogleAccessToken(userId);
        if (youtubeSubscriptionsPort.requiresOAuthAccessToken()
                && (token == null || token.isBlank())) {
            throw new BusinessException(
                    ErrorCode.YOUTUBE_ACCESS_TOKEN_MISSING,
                    "YouTube 연동을 위해 Google 계정 액세스 토큰이 필요합니다. OAuth 연동 후 다시 시도해 주세요.");
        }

        List<YoutubeSubscriptionListItem> items;
        try {
            items = youtubeSubscriptionsPort.fetchMineSubscribedChannels(token);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("youtube subscriptions fetch failed userId={}", userId, e);
            throw new BusinessException(
                    ErrorCode.YOUTUBE_UPSTREAM_ERROR, "YouTube 구독 목록을 가져오지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }

        Instant now = Instant.now();
        int created = 0;
        int updated = 0;
        int failed = 0;
        for (YoutubeSubscriptionListItem item : items) {
            switch (subscriptionSyncItemService.merge(userId, item, now)) {
                case CREATED -> created++;
                case UPDATED -> updated++;
                case FAILED -> failed++;
            }
        }
        int synced = created + updated;
        log.info(
                "subscription sync done userId={} syncedCount={} createdCount={} updatedCount={} failedCount={}",
                userId,
                synced,
                created,
                updated,
                failed);
        return new SubscriptionSyncResponse(synced, created, updated, failed);
    }

    public Page<SubscriptionResponse> listSubscriptions(Long userId, int pageOneBased, int size) {
        int pageIndex = Math.max(0, pageOneBased - 1);
        Page<UserSubscription> page =
                userSubscriptionRepository.findByUser_IdOrderByUpdatedAtDesc(
                        userId, PageRequest.of(pageIndex, size));
        log.debug(
                "subscriptions list userId={} page={} size={} total={}",
                userId,
                pageOneBased,
                size,
                page.getTotalElements());
        return page.map(SubscriptionDtoMapper::toResponse);
    }

    public SubscriptionResponse getSubscription(Long userId, Long subscriptionId) {
        UserSubscription us =
                userSubscriptionRepository
                        .findByIdAndUserIdWithChannel(subscriptionId, userId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.SUBSCRIPTION_NOT_FOUND, "구독 정보를 찾을 수 없습니다."));
        log.debug("subscription detail userId={} subscriptionId={}", userId, subscriptionId);
        return SubscriptionDtoMapper.toResponse(us);
    }

    @Transactional
    public SubscriptionResponse patchSubscription(Long userId, Long subscriptionId, PatchSubscriptionRequest body) {
        if (body.getCategory() == null
                && body.getFavorite() == null
                && body.getLearningChannel() == null
                && body.getNote() == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "수정할 필드를 하나 이상 보내 주세요.");
        }
        UserSubscription us =
                userSubscriptionRepository
                        .findByIdAndUserIdWithChannel(subscriptionId, userId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.SUBSCRIPTION_NOT_FOUND, "구독 정보를 찾을 수 없습니다."));
        if (body.getCategory() != null) {
            us.setCategory(body.getCategory().isBlank() ? null : body.getCategory());
        }
        if (body.getNote() != null) {
            us.setNote(body.getNote().isBlank() ? null : body.getNote());
        }
        if (body.getFavorite() != null) {
            us.setFavorite(body.getFavorite());
        }
        if (body.getLearningChannel() != null) {
            us.setLearningChannel(body.getLearningChannel());
        }
        log.info(
                "subscription patched userId={} subscriptionId={} channelId={}",
                userId,
                subscriptionId,
                us.getChannel().getId());
        return SubscriptionDtoMapper.toResponse(us);
    }

    private String resolveGoogleAccessToken(Long userId) {
        return userOAuthAccountRepository
                .findFirstByUser_IdAndProviderAndAccessTokenIsNotNullOrderByUpdatedAtDesc(
                        userId, OAuthProvider.GOOGLE)
                .map(UserOAuthAccount::getAccessToken)
                .filter(t -> t != null && !t.isBlank())
                .orElse(null);
    }
}
