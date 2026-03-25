package com.myapp.learningtube.domain.transcript;

import com.myapp.learningtube.domain.transcript.dto.SelectTranscriptTrackRequest;
import com.myapp.learningtube.domain.transcript.dto.TranscriptSegmentResponse;
import com.myapp.learningtube.domain.transcript.dto.TranscriptSyncResponse;
import com.myapp.learningtube.domain.transcript.dto.TranscriptTrackSummaryResponse;
import com.myapp.learningtube.domain.transcript.dto.TranscriptViewResponse;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import com.myapp.learningtube.domain.video.Video;
import com.myapp.learningtube.domain.video.VideoRepository;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.infra.youtube.transcript.FetchedTranscriptCue;
import com.myapp.learningtube.infra.youtube.transcript.FetchedTranscriptTrack;
import com.myapp.learningtube.infra.youtube.transcript.YoutubeTranscriptPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TranscriptService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptService.class);

    private final VideoRepository videoRepository;
    private final UserVideoRepository userVideoRepository;
    private final TranscriptTrackRepository transcriptTrackRepository;
    private final TranscriptSegmentRepository transcriptSegmentRepository;
    private final YoutubeTranscriptPort youtubeTranscriptPort;

    public TranscriptService(
            VideoRepository videoRepository,
            UserVideoRepository userVideoRepository,
            TranscriptTrackRepository transcriptTrackRepository,
            TranscriptSegmentRepository transcriptSegmentRepository,
            YoutubeTranscriptPort youtubeTranscriptPort) {
        this.videoRepository = videoRepository;
        this.userVideoRepository = userVideoRepository;
        this.transcriptTrackRepository = transcriptTrackRepository;
        this.transcriptSegmentRepository = transcriptSegmentRepository;
        this.youtubeTranscriptPort = youtubeTranscriptPort;
    }

    public TranscriptViewResponse getTranscriptView(Long userId, Long videoId) {
        assertLibraryAccess(userId, videoId);
        TranscriptViewResponse view = new TranscriptViewResponse();
        view.setVideoId(videoId);
        long trackCount = transcriptTrackRepository.countByVideoId(videoId);
        view.setTracksAvailable(trackCount > 0);
        Optional<TranscriptTrack> selected = transcriptTrackRepository.findByVideo_IdAndSelectedTrue(videoId);
        if (selected.isEmpty()) {
            view.setHasSelectedTrack(false);
            view.setSelectedTrack(null);
            view.setSegments(List.of());
            log.debug(
                    "transcript view userId={} videoId={} tracksAvailable={} hasSelectedTrack=false",
                    userId,
                    videoId,
                    trackCount > 0);
            return view;
        }
        TranscriptTrack t = selected.get();
        List<TranscriptSegment> rows =
                transcriptSegmentRepository.findByTranscriptTrack_IdOrderByLineIndexAsc(t.getId());
        view.setHasSelectedTrack(true);
        view.setSelectedTrack(TranscriptDtoMapper.toTrackSummary(t, (long) rows.size()));
        List<TranscriptSegmentResponse> segDtos = rows.stream().map(TranscriptDtoMapper::toSegment).toList();
        view.setSegments(segDtos);
        log.info(
                "transcript view userId={} videoId={} trackId={} languageCode={} segmentCount={}",
                userId,
                videoId,
                t.getId(),
                t.getLanguageCode(),
                rows.size());
        return view;
    }

    public List<TranscriptTrackSummaryResponse> listTracks(Long userId, Long videoId) {
        assertLibraryAccess(userId, videoId);
        List<TranscriptTrack> tracks =
                transcriptTrackRepository.findByVideo_IdOrderBySelectedDescLanguageCodeAscIdAsc(videoId);
        List<TranscriptTrackSummaryResponse> out = new ArrayList<>();
        for (TranscriptTrack t : tracks) {
            long cnt = transcriptSegmentRepository.countByTranscriptTrack_Id(t.getId());
            out.add(TranscriptDtoMapper.toTrackSummary(t, cnt));
        }
        log.debug("transcript tracks list userId={} videoId={} size={}", userId, videoId, out.size());
        return out;
    }

    @Transactional
    public TranscriptSyncResponse syncFromUpstream(Long userId, Long videoId) {
        Video video = loadVideoForSync(userId, videoId);
        List<FetchedTranscriptTrack> fetched =
                youtubeTranscriptPort.fetchTracks(video.getYoutubeVideoId());
        TranscriptSyncResponse res = new TranscriptSyncResponse();
        if (fetched.isEmpty()) {
            res.setUpstreamEmpty(true);
            res.setTracksUpserted(0);
            res.setSegmentsWritten(0);
            res.setMessage("업스트림에서 가져온 트랙이 없습니다(스텁 또는 미제공).");
            log.info("transcript sync userId={} videoId={} upstreamEmpty=true", userId, videoId);
            return res;
        }
        int tracksUpserted = 0;
        int segmentsWritten = 0;
        for (FetchedTranscriptTrack ft : fetched) {
            if (ft.cues().isEmpty()) {
                continue;
            }
            String lang = ft.languageCode().trim();
            TranscriptTrack track =
                    transcriptTrackRepository
                            .findByVideo_IdAndLanguageCodeAndAutoGenerated(
                                    videoId, lang, ft.autoGenerated())
                            .map(
                                    existing -> {
                                        existing.setSource(ft.source());
                                        return existing;
                                    })
                            .orElseGet(
                                    () ->
                                            new TranscriptTrack(
                                                    video, lang, ft.autoGenerated(), ft.source(), false));
            track = transcriptTrackRepository.save(track);
            transcriptSegmentRepository.deleteByTranscriptTrack_Id(track.getId());
            List<TranscriptSegment> batch = new ArrayList<>();
            int line = 0;
            for (FetchedTranscriptCue cue : ft.cues()) {
                validateCue(cue);
                String txt = cue.text() == null ? "" : cue.text().trim();
                batch.add(
                        new TranscriptSegment(
                                track, line++, cue.startSeconds(), cue.endSeconds(), txt));
            }
            transcriptSegmentRepository.saveAll(batch);
            tracksUpserted++;
            segmentsWritten += batch.size();
            log.info(
                    "transcript sync upsert userId={} videoId={} trackId={} languageCode={} segmentCount={}",
                    userId,
                    videoId,
                    track.getId(),
                    lang,
                    batch.size());
        }
        ensureDefaultSelection(videoId);
        res.setUpstreamEmpty(tracksUpserted == 0);
        res.setTracksUpserted(tracksUpserted);
        res.setSegmentsWritten(segmentsWritten);
        res.setMessage(
                tracksUpserted == 0
                        ? "수집된 트랙에 유효한 큐가 없었습니다."
                        : "동기화가 완료되었습니다.");
        log.info(
                "transcript sync done userId={} videoId={} tracksUpserted={} segmentsWritten={}",
                userId,
                videoId,
                tracksUpserted,
                segmentsWritten);
        return res;
    }

    @Transactional
    public TranscriptTrackSummaryResponse selectTrack(Long userId, Long videoId, SelectTranscriptTrackRequest body) {
        assertLibraryAccess(userId, videoId);
        TranscriptTrack track =
                transcriptTrackRepository
                        .findByIdAndVideo_Id(body.getTrackId(), videoId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.TRANSCRIPT_TRACK_NOT_FOUND,
                                                "해당 영상의 자막 트랙을 찾을 수 없습니다."));
        clearSelectedForVideo(videoId);
        track.setSelected(true);
        transcriptTrackRepository.save(track);
        long cnt = transcriptSegmentRepository.countByTranscriptTrack_Id(track.getId());
        log.info(
                "transcript select userId={} videoId={} trackId={} languageCode={} segmentCount={}",
                userId,
                videoId,
                track.getId(),
                track.getLanguageCode(),
                cnt);
        return TranscriptDtoMapper.toTrackSummary(track, cnt);
    }

    private void validateCue(FetchedTranscriptCue cue) {
        if (cue.endSeconds() < cue.startSeconds()) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED, "자막 큐의 endSeconds는 startSeconds 이상이어야 합니다.");
        }
    }

    private Video loadVideoForSync(Long userId, Long videoId) {
        assertLibraryAccess(userId, videoId);
        return videoRepository
                .findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND, "영상을 찾을 수 없습니다."));
    }

    private void assertLibraryAccess(Long userId, Long videoId) {
        if (!videoRepository.existsById(videoId)) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND, "영상을 찾을 수 없습니다.");
        }
        if (!userVideoRepository.existsByUser_IdAndVideo_Id(userId, videoId)) {
            throw new BusinessException(
                    ErrorCode.TRANSCRIPT_ACCESS_DENIED, "내 학습 목록에 등록된 영상만 자막 API를 사용할 수 있습니다.");
        }
    }

    /** 선택된 트랙이 없고 트랙이 존재하면 수동 트랙 우선, 없으면 첫 트랙을 선택. */
    private void ensureDefaultSelection(Long videoId) {
        if (transcriptTrackRepository.findByVideo_IdAndSelectedTrue(videoId).isPresent()) {
            return;
        }
        List<TranscriptTrack> tracks =
                transcriptTrackRepository.findByVideo_IdOrderBySelectedDescLanguageCodeAscIdAsc(videoId);
        if (tracks.isEmpty()) {
            return;
        }
        clearSelectedForVideo(videoId);
        Optional<TranscriptTrack> manual =
                tracks.stream().filter(t -> !t.isAutoGenerated()).findFirst();
        TranscriptTrack pick = manual.orElseGet(() -> tracks.get(0));
        pick.setSelected(true);
        transcriptTrackRepository.save(pick);
    }

    private void clearSelectedForVideo(Long videoId) {
        List<TranscriptTrack> tracks =
                transcriptTrackRepository.findByVideo_IdOrderBySelectedDescLanguageCodeAscIdAsc(videoId);
        boolean dirty = false;
        for (TranscriptTrack t : tracks) {
            if (t.isSelected()) {
                t.setSelected(false);
                dirty = true;
            }
        }
        if (dirty) {
            transcriptTrackRepository.saveAll(tracks);
        }
    }
}
