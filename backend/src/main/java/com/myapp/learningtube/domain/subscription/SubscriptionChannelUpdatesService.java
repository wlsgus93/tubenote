package com.myapp.learningtube.domain.subscription;

import com.myapp.learningtube.domain.subscription.dto.ChannelUpdatesSyncResponse;
import com.myapp.learningtube.domain.subscription.dto.SubscriptionRecentVideoResponse;
import com.myapp.learningtube.domain.auth.google.GoogleOAuthAccessTokenService;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.infra.youtube.YoutubeApiProperties;
import com.myapp.learningtube.infra.youtube.YoutubeChannelUploadsPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubscriptionChannelUpdatesService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionChannelUpdatesService.class);

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final GoogleOAuthAccessTokenService googleOAuthAccessTokenService;
    private final YoutubeChannelUploadsPort youtubeChannelUploadsPort;
    private final YoutubeApiProperties youtubeApiProperties;
    private final SubscriptionChannelUpdatesSyncItemService subscriptionChannelUpdatesSyncItemService;
    private final SubscriptionRecentVideoRepository subscriptionRecentVideoRepository;
    private final UserVideoRepository userVideoRepository;

    public SubscriptionChannelUpdatesService(
            UserSubscriptionRepository userSubscriptionRepository,
            GoogleOAuthAccessTokenService googleOAuthAccessTokenService,
            YoutubeChannelUploadsPort youtubeChannelUploadsPort,
            YoutubeApiProperties youtubeApiProperties,
            SubscriptionChannelUpdatesSyncItemService subscriptionChannelUpdatesSyncItemService,
            SubscriptionRecentVideoRepository subscriptionRecentVideoRepository,
            UserVideoRepository userVideoRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.googleOAuthAccessTokenService = googleOAuthAccessTokenService;
        this.youtubeChannelUploadsPort = youtubeChannelUploadsPort;
        this.youtubeApiProperties = youtubeApiProperties;
        this.subscriptionChannelUpdatesSyncItemService = subscriptionChannelUpdatesSyncItemService;
        this.subscriptionRecentVideoRepository = subscriptionRecentVideoRepository;
        this.userVideoRepository = userVideoRepository;
    }

    @Transactional
    public ChannelUpdatesSyncResponse syncAllSubscribedChannels(Long userId) {
        String token = googleOAuthAccessTokenService.resolveValidAccessToken(userId).orElse(null);
        if (youtubeChannelUploadsPort.requiresOAuthAccessToken()
                && (token == null || token.isBlank())) {
            throw new BusinessException(
                    ErrorCode.YOUTUBE_ACCESS_TOKEN_MISSING,
                    "채널 최신 영상 동기화에는 Google 액세스 토큰이 필요합니다.");
        }

        int max = youtubeApiProperties.getChannelUpdatesMaxVideosPerChannel();
        List<UserSubscription> subs = userSubscriptionRepository.findAllByUserIdWithChannel(userId);

        int processedChannels = 0;
        int failedChannels = 0;
        int createdVideos = 0;
        int updatedVideos = 0;

        for (UserSubscription sub : subs) {
            try {
                SubscriptionChannelUpdatesSyncItemService.SyncOutcome outcome =
                        subscriptionChannelUpdatesSyncItemService.syncOne(userId, sub.getId(), token, max);
                processedChannels++;
                createdVideos += outcome.createdVideos();
                updatedVideos += outcome.updatedVideos();
            } catch (BusinessException e) {
                log.warn(
                        "channel updates sync business error userId={} subscriptionId={} code={} message={}",
                        userId,
                        sub.getId(),
                        e.getErrorCode().getCode(),
                        e.getMessage());
                failedChannels++;
            } catch (Exception e) {
                log.warn(
                        "channel updates sync failed userId={} subscriptionId={} message={}",
                        userId,
                        sub.getId(),
                        e.getMessage());
                failedChannels++;
            }
        }

        log.info(
                "channel updates sync summary userId={} processedChannels={} createdVideos={} updatedVideos={} failedChannels={}",
                userId,
                processedChannels,
                createdVideos,
                updatedVideos,
                failedChannels);

        return new ChannelUpdatesSyncResponse(processedChannels, createdVideos, updatedVideos, failedChannels);
    }

    public Page<SubscriptionRecentVideoResponse> listRecentForSubscription(
            Long userId, Long subscriptionId, int pageOneBased, int size) {
        userSubscriptionRepository
                .findByIdAndUser_Id(subscriptionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "구독 정보를 찾을 수 없습니다."));
        int pageIndex = Math.max(0, pageOneBased - 1);
        Page<SubscriptionRecentVideo> page =
                subscriptionRecentVideoRepository.findForOwnedSubscription(
                        userId, subscriptionId, PageRequest.of(pageIndex, size));
        log.debug(
                "subscription recent videos userId={} subscriptionId={} page={} total={}",
                userId,
                subscriptionId,
                pageOneBased,
                page.getTotalElements());
        return page.map(srv -> SubscriptionRecentVideoMapper.toResponse(srv, userId, userVideoRepository));
    }

    public Page<SubscriptionRecentVideoResponse> listRecentAll(Long userId, int pageOneBased, int size) {
        int pageIndex = Math.max(0, pageOneBased - 1);
        Page<SubscriptionRecentVideo> page =
                subscriptionRecentVideoRepository.findAllForUser(userId, PageRequest.of(pageIndex, size));
        log.debug(
                "all subscription recent videos userId={} page={} total={}",
                userId,
                pageOneBased,
                page.getTotalElements());
        return page.map(srv -> SubscriptionRecentVideoMapper.toResponse(srv, userId, userVideoRepository));
    }

}
