package com.myapp.learningtube.domain.dashboard;

import com.myapp.learningtube.domain.dashboard.dto.DashboardFavoriteChannelDto;
import com.myapp.learningtube.domain.dashboard.dto.DashboardResponse;
import com.myapp.learningtube.domain.dashboard.dto.DashboardWeeklySummaryDto;
import com.myapp.learningtube.domain.highlight.HighlightRepository;
import com.myapp.learningtube.domain.note.NoteRepository;
import com.myapp.learningtube.domain.subscription.SubscriptionRecentVideo;
import com.myapp.learningtube.domain.subscription.SubscriptionRecentVideoRepository;
import com.myapp.learningtube.domain.subscription.UserSubscription;
import com.myapp.learningtube.domain.subscription.UserSubscriptionRepository;
import com.myapp.learningtube.domain.video.UserVideo;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final DashboardProperties dashboardProperties;
    private final UserVideoRepository userVideoRepository;
    private final NoteRepository noteRepository;
    private final HighlightRepository highlightRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRecentVideoRepository subscriptionRecentVideoRepository;

    public DashboardService(
            DashboardProperties dashboardProperties,
            UserVideoRepository userVideoRepository,
            NoteRepository noteRepository,
            HighlightRepository highlightRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionRecentVideoRepository subscriptionRecentVideoRepository) {
        this.dashboardProperties = dashboardProperties;
        this.userVideoRepository = userVideoRepository;
        this.noteRepository = noteRepository;
        this.highlightRepository = highlightRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionRecentVideoRepository = subscriptionRecentVideoRepository;
    }

    public DashboardResponse load(Long userId) {
        DashboardResponse res = new DashboardResponse();

        int pool = Math.max(dashboardProperties.getTodayPickCandidatePool(), dashboardProperties.getTodayPickLimit());
        List<UserVideo> candidates =
                userVideoRepository.findTodayPickCandidates(userId, PageRequest.of(0, pool));
        List<UserVideo> todayPick =
                candidates.stream()
                        .sorted(
                                Comparator.comparingInt((UserVideo uv) -> DashboardMapper.priorityRank(uv.getPriority()))
                                        .thenComparing(UserVideo::getUpdatedAt, Comparator.reverseOrder()))
                        .limit(dashboardProperties.getTodayPickLimit())
                        .collect(Collectors.toList());
        res.setTodayPick(
                todayPick.stream().map(DashboardMapper::toVideoCard).collect(Collectors.toList()));

        List<UserVideo> continueList =
                userVideoRepository.findContinueWatching(
                        userId, PageRequest.of(0, dashboardProperties.getContinueWatchingLimit()));
        res.setContinueWatching(
                continueList.stream().map(DashboardMapper::toVideoCard).collect(Collectors.toList()));

        res.setRecentNotes(
                noteRepository
                        .findRecentForDashboard(userId, PageRequest.of(0, dashboardProperties.getRecentNotesLimit()))
                        .stream()
                        .map(DashboardMapper::toNoteCard)
                        .collect(Collectors.toList()));

        List<UserVideo> incomplete =
                userVideoRepository.findIncompleteVideos(
                        userId, PageRequest.of(0, dashboardProperties.getIncompleteVideosLimit()));
        res.setIncompleteVideos(
                incomplete.stream().map(DashboardMapper::toVideoCard).collect(Collectors.toList()));

        res.setFavoriteChannelUpdates(buildFavoriteChannels(userId));

        Instant weekStart = startOfWeekUtc();
        DashboardWeeklySummaryDto weekly = new DashboardWeeklySummaryDto();
        weekly.setWeekStartUtc(weekStart);
        weekly.setInProgressCount(userVideoRepository.countInProgressUpdatedSince(userId, weekStart));
        weekly.setCompletedCount(userVideoRepository.countCompletedSince(userId, weekStart));
        weekly.setNoteCount(noteRepository.countCreatedSince(userId, weekStart));
        weekly.setHighlightCount(highlightRepository.countCreatedSince(userId, weekStart));
        res.setWeeklySummary(weekly);

        log.info(
                "dashboard loaded userId={} todayPick={} continueWatching={} recentNotes={} incompleteVideos={} favoriteChannels={} weeklyNotes={} weeklyHighlights={}",
                userId,
                res.getTodayPick().size(),
                res.getContinueWatching().size(),
                res.getRecentNotes().size(),
                res.getIncompleteVideos().size(),
                res.getFavoriteChannelUpdates().size(),
                weekly.getNoteCount(),
                weekly.getHighlightCount());

        return res;
    }

    private List<DashboardFavoriteChannelDto> buildFavoriteChannels(Long userId) {
        List<UserSubscription> favorites =
                userSubscriptionRepository.findByUser_IdAndFavoriteTrueOrderByUnreadNewVideoCountDescUpdatedAtDesc(
                        userId, PageRequest.of(0, dashboardProperties.getFavoriteSubscriptionsLimit()));
        List<DashboardFavoriteChannelDto> out = new ArrayList<>();
        int feedLimit = dashboardProperties.getFavoriteChannelRecentVideosLimit();
        for (UserSubscription us : favorites) {
            DashboardFavoriteChannelDto dto = new DashboardFavoriteChannelDto();
            dto.setSubscriptionId(us.getId());
            dto.setChannelId(us.getChannel().getId());
            dto.setChannelTitle(us.getChannel().getTitle());
            dto.setUnreadNewVideoCount(us.getUnreadNewVideoCount());
            List<SubscriptionRecentVideo> feed =
                    subscriptionRecentVideoRepository
                            .findForOwnedSubscription(userId, us.getId(), PageRequest.of(0, feedLimit))
                            .getContent();
            dto.setRecentVideos(
                    feed.stream()
                            .map(srv -> DashboardMapper.toFeedMini(srv, userId, userVideoRepository))
                            .collect(Collectors.toList()));
            out.add(dto);
        }
        return out;
    }

    /** ISO 주차 기준 월요일 00:00 UTC. */
    public static Instant startOfWeekUtc() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        return monday.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
