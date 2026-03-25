package com.myapp.learningtube.domain.analytics;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 통계용 네이티브 집계(일·채널·컬렉션). 추후 {@code DailyLearningStat} 등으로 이전 시 이 클래스만 교체·축소.
 */
@Repository
public class AnalyticsAggregationRepository {

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public List<Object[]> aggregateCompletedVideosByDay(long userId, Instant start, Instant endExclusive) {
        return em.createNativeQuery(
                        "SELECT CAST(completed_at AS DATE), COUNT(*) FROM user_videos "
                                + "WHERE user_id = ? AND completed_at IS NOT NULL AND completed_at >= ? AND completed_at < ? "
                                + "GROUP BY CAST(completed_at AS DATE)")
                .setParameter(1, userId)
                .setParameter(2, Timestamp.from(start))
                .setParameter(3, Timestamp.from(endExclusive))
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> aggregateNotesCreatedByDay(long userId, Instant start, Instant endExclusive) {
        return em.createNativeQuery(
                        "SELECT CAST(n.created_at AS DATE), COUNT(*) FROM notes n "
                                + "INNER JOIN user_videos uv ON n.user_video_id = uv.id "
                                + "WHERE uv.user_id = ? AND n.created_at >= ? AND n.created_at < ? "
                                + "GROUP BY CAST(n.created_at AS DATE)")
                .setParameter(1, userId)
                .setParameter(2, Timestamp.from(start))
                .setParameter(3, Timestamp.from(endExclusive))
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> aggregateHighlightsCreatedByDay(long userId, Instant start, Instant endExclusive) {
        return em.createNativeQuery(
                        "SELECT CAST(h.created_at AS DATE), COUNT(*) FROM highlights h "
                                + "INNER JOIN user_videos uv ON h.user_video_id = uv.id "
                                + "WHERE uv.user_id = ? AND h.created_at >= ? AND h.created_at < ? "
                                + "GROUP BY CAST(h.created_at AS DATE)")
                .setParameter(1, userId)
                .setParameter(2, Timestamp.from(start))
                .setParameter(3, Timestamp.from(endExclusive))
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> aggregateChannelVideoStats(long userId) {
        return em.createNativeQuery(
                        "SELECT COALESCE(v.channel_youtube_id, ''), COALESCE(v.channel_title, ''), "
                                + "COUNT(uv.id), "
                                + "COALESCE(SUM(CASE WHEN uv.learning_status = 'COMPLETED' THEN 1 ELSE 0 END), 0) "
                                + "FROM user_videos uv "
                                + "INNER JOIN videos v ON uv.video_id = v.id "
                                + "WHERE uv.user_id = ? AND uv.archived = FALSE "
                                + "GROUP BY COALESCE(v.channel_youtube_id, ''), COALESCE(v.channel_title, '') "
                                + "ORDER BY COUNT(uv.id) DESC")
                .setParameter(1, userId)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> aggregateNoteCountByChannelYoutubeId(long userId) {
        return em.createNativeQuery(
                        "SELECT COALESCE(v.channel_youtube_id, ''), COUNT(n.id) FROM notes n "
                                + "INNER JOIN user_videos uv ON n.user_video_id = uv.id "
                                + "INNER JOIN videos v ON uv.video_id = v.id "
                                + "WHERE uv.user_id = ? AND uv.archived = FALSE "
                                + "GROUP BY COALESCE(v.channel_youtube_id, '')")
                .setParameter(1, userId)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> aggregateCollectionStats(long userId) {
        return em.createNativeQuery(
                        "SELECT c.id, c.name, COUNT(cv.id), "
                                + "COALESCE(SUM(CASE WHEN uv.learning_status = 'COMPLETED' THEN 1 ELSE 0 END), 0) "
                                + "FROM collections c "
                                + "LEFT JOIN collection_videos cv ON cv.collection_id = c.id "
                                + "LEFT JOIN user_videos uv ON cv.user_video_id = uv.id "
                                + "WHERE c.user_id = ? "
                                + "GROUP BY c.id, c.name, c.sort_order "
                                + "ORDER BY c.sort_order ASC, c.id ASC")
                .setParameter(1, userId)
                .getResultList();
    }
}
