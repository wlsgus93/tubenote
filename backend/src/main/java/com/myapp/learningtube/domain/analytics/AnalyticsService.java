package com.myapp.learningtube.domain.analytics;

import com.myapp.learningtube.domain.analytics.dto.AnalyticsChannelStatDto;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsCollectionStatDto;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsDailyPointDto;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsDailyResponse;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsStatusBucketDto;
import com.myapp.learningtube.domain.analytics.dto.AnalyticsSummaryResponse;
import com.myapp.learningtube.domain.channel.Channel;
import com.myapp.learningtube.domain.channel.ChannelRepository;
import com.myapp.learningtube.domain.highlight.HighlightRepository;
import com.myapp.learningtube.domain.note.NoteRepository;
import com.myapp.learningtube.domain.video.LearningStatus;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsProperties analyticsProperties;
    private final UserVideoRepository userVideoRepository;
    private final NoteRepository noteRepository;
    private final HighlightRepository highlightRepository;
    private final ChannelRepository channelRepository;
    private final AnalyticsAggregationRepository analyticsAggregationRepository;

    public AnalyticsService(
            AnalyticsProperties analyticsProperties,
            UserVideoRepository userVideoRepository,
            NoteRepository noteRepository,
            HighlightRepository highlightRepository,
            ChannelRepository channelRepository,
            AnalyticsAggregationRepository analyticsAggregationRepository) {
        this.analyticsProperties = analyticsProperties;
        this.userVideoRepository = userVideoRepository;
        this.noteRepository = noteRepository;
        this.highlightRepository = highlightRepository;
        this.channelRepository = channelRepository;
        this.analyticsAggregationRepository = analyticsAggregationRepository;
    }

    public AnalyticsSummaryResponse summary(Long userId) {
        AnalyticsSummaryResponse r = new AnalyticsSummaryResponse();
        r.setTotalSavedVideos(userVideoRepository.countByUser_Id(userId));
        r.setInProgressCount(
                userVideoRepository.countByUser_IdAndArchivedFalseAndLearningStatus(
                        userId, LearningStatus.IN_PROGRESS));
        r.setCompletedCount(
                userVideoRepository.countByUser_IdAndArchivedFalseAndLearningStatus(
                        userId, LearningStatus.COMPLETED));
        r.setOnHoldCount(userVideoRepository.countOnHoldExcludedFromActive(userId));
        r.setTotalNoteCount(noteRepository.countAllForUser(userId));
        r.setTotalHighlightCount(highlightRepository.countAllForUser(userId));
        Long sumSec = userVideoRepository.sumLastPositionSecForUser(userId);
        r.setEstimatedLearningSeconds(sumSec != null ? sumSec : 0L);

        log.info(
                "analytics summary userId={} saved={} inProgress={} completed={} onHold={} notes={} highlights={} estSec={}",
                userId,
                r.getTotalSavedVideos(),
                r.getInProgressCount(),
                r.getCompletedCount(),
                r.getOnHoldCount(),
                r.getTotalNoteCount(),
                r.getTotalHighlightCount(),
                r.getEstimatedLearningSeconds());
        return r;
    }

    public AnalyticsDailyResponse daily(Long userId, AnalyticsRangeType rangeType) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant rangeEndExclusive = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        LocalDate startDate =
                switch (rangeType) {
                    case WEEK -> today.minusDays(Math.max(1, analyticsProperties.getWeekDays()) - 1);
                    case MONTH -> today.minusDays(Math.max(1, analyticsProperties.getMonthDays()) - 1);
                    case ALL -> resolveAllRangeStart(userId, today);
                };

        Instant rangeStart = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<LocalDate, long[]> buckets = new java.util.TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            buckets.put(d, new long[3]);
        }

        applyDayCounts(
                buckets,
                analyticsAggregationRepository.aggregateCompletedVideosByDay(userId, rangeStart, rangeEndExclusive),
                0);
        applyDayCounts(
                buckets,
                analyticsAggregationRepository.aggregateNotesCreatedByDay(userId, rangeStart, rangeEndExclusive),
                1);
        applyDayCounts(
                buckets,
                analyticsAggregationRepository.aggregateHighlightsCreatedByDay(userId, rangeStart, rangeEndExclusive),
                2);

        AnalyticsDailyResponse res = new AnalyticsDailyResponse();
        res.setRangeType(rangeType);
        res.setRangeStartUtc(rangeStart);
        res.setRangeEndExclusiveUtc(rangeEndExclusive);
        List<AnalyticsDailyPointDto> points = new ArrayList<>();
        for (Map.Entry<LocalDate, long[]> e : buckets.entrySet()) {
            AnalyticsDailyPointDto p = new AnalyticsDailyPointDto();
            p.setDate(e.getKey().toString());
            long[] a = e.getValue();
            p.setCompletedVideoCount(a[0]);
            p.setCreatedNoteCount(a[1]);
            p.setCreatedHighlightCount(a[2]);
            points.add(p);
        }
        res.setPoints(points);

        log.info(
                "analytics daily userId={} range={} points={} completedSum={} notesSum={} highlightsSum={}",
                userId,
                rangeType,
                points.size(),
                points.stream().mapToLong(AnalyticsDailyPointDto::getCompletedVideoCount).sum(),
                points.stream().mapToLong(AnalyticsDailyPointDto::getCreatedNoteCount).sum(),
                points.stream().mapToLong(AnalyticsDailyPointDto::getCreatedHighlightCount).sum());
        return res;
    }

    private LocalDate resolveAllRangeStart(Long userId, LocalDate today) {
        Instant minUv = userVideoRepository.findMinCreatedAt(userId);
        Instant minNote = noteRepository.findMinCreatedAtForUser(userId);
        Instant minHl = highlightRepository.findMinCreatedAtForUser(userId);
        Instant earliest = minNonNull(minUv, minNote, minHl);
        LocalDate startDate =
                earliest != null
                        ? LocalDate.ofInstant(earliest, ZoneOffset.UTC)
                        : today;
        if (startDate.isAfter(today)) {
            startDate = today;
        }
        long spanDays = ChronoUnit.DAYS.between(startDate, today) + 1;
        int cap = Math.max(1, analyticsProperties.getMaxDailyBuckets());
        if (spanDays > cap) {
            startDate = today.minusDays(cap - 1L);
        }
        return startDate;
    }

    private static void applyDayCounts(
            Map<LocalDate, long[]> buckets, List<Object[]> rows, int index) {
        for (Object[] row : rows) {
            LocalDate d = toLocalDate(row[0]);
            if (d == null || !buckets.containsKey(d)) {
                continue;
            }
            long n = row[1] instanceof Number num ? num.longValue() : 0L;
            buckets.get(d)[index] += n;
        }
    }

    private static LocalDate toLocalDate(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof LocalDate ld) {
            return ld;
        }
        if (o instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        if (o instanceof java.util.Date ud) {
            return Instant.ofEpochMilli(ud.getTime()).atZone(ZoneOffset.UTC).toLocalDate();
        }
        return null;
    }

    public List<AnalyticsStatusBucketDto> statusDistribution(Long userId) {
        List<AnalyticsStatusBucketDto> list = new ArrayList<>();
        for (Object[] row : userVideoRepository.countGroupedByLearningStatus(userId)) {
            AnalyticsStatusBucketDto b = new AnalyticsStatusBucketDto();
            LearningStatus st = (LearningStatus) row[0];
            b.setLearningStatus(st != null ? st.name() : "UNKNOWN");
            b.setCount(row[1] instanceof Number n ? n.longValue() : 0L);
            list.add(b);
        }
        log.info("analytics status-distribution userId={} buckets={}", userId, list.size());
        return list;
    }

    public List<AnalyticsChannelStatDto> channelStats(Long userId) {
        List<Object[]> videoRows = analyticsAggregationRepository.aggregateChannelVideoStats(userId);
        Map<String, Long> noteByYt = new HashMap<>();
        for (Object[] row : analyticsAggregationRepository.aggregateNoteCountByChannelYoutubeId(userId)) {
            String yt = row[0] != null ? String.valueOf(row[0]) : "";
            long c = row[1] instanceof Number n ? n.longValue() : 0L;
            noteByYt.merge(yt, c, Long::sum);
        }

        Set<String> ytIds =
                videoRows.stream()
                        .map(r -> r[0] != null ? String.valueOf(r[0]) : "")
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toSet());
        Map<String, Long> channelIdByYt = new HashMap<>();
        if (!ytIds.isEmpty()) {
            for (Channel ch : channelRepository.findByYoutubeChannelIdIn(ytIds)) {
                channelIdByYt.put(ch.getYoutubeChannelId(), ch.getId());
            }
        }

        List<AnalyticsChannelStatDto> out = new ArrayList<>();
        for (Object[] row : videoRows) {
            String yt = row[0] != null ? String.valueOf(row[0]) : "";
            String title = row[1] != null ? String.valueOf(row[1]) : "";
            long saved = row[2] instanceof Number n ? n.longValue() : 0L;
            long completed = row[3] instanceof Number n ? n.longValue() : 0L;
            AnalyticsChannelStatDto dto = new AnalyticsChannelStatDto();
            dto.setYoutubeChannelId(yt);
            dto.setChannelTitle(title);
            dto.setChannelId(channelIdByYt.get(yt));
            dto.setSavedVideoCount(saved);
            dto.setCompletedVideoCount(completed);
            dto.setNoteCount(noteByYt.getOrDefault(yt, 0L));
            out.add(dto);
        }
        log.info("analytics channels userId={} rows={}", userId, out.size());
        return out;
    }

    public List<AnalyticsCollectionStatDto> collectionStats(Long userId) {
        List<AnalyticsCollectionStatDto> out = new ArrayList<>();
        for (Object[] row : analyticsAggregationRepository.aggregateCollectionStats(userId)) {
            AnalyticsCollectionStatDto dto = new AnalyticsCollectionStatDto();
            dto.setCollectionId(row[0] instanceof Number n ? n.longValue() : null);
            dto.setCollectionName(row[1] != null ? String.valueOf(row[1]) : "");
            dto.setVideoCount(row[2] instanceof Number n ? n.longValue() : 0L);
            dto.setCompletedVideoCount(row[3] instanceof Number n ? n.longValue() : 0L);
            out.add(dto);
        }
        log.info("analytics collections userId={} rows={}", userId, out.size());
        return out;
    }

    private static Instant minNonNull(Instant a, Instant b, Instant c) {
        return java.util.stream.Stream.of(a, b, c)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
    }
}
