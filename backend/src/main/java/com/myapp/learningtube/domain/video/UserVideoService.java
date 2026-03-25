package com.myapp.learningtube.domain.video;

import com.myapp.learningtube.domain.highlight.HighlightRepository;
import com.myapp.learningtube.domain.note.NoteRepository;
import com.myapp.learningtube.domain.transcript.TranscriptTrackRepository;
import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.domain.video.dto.ImportVideoUrlResponse;
import com.myapp.learningtube.domain.video.dto.UpdateArchiveRequest;
import com.myapp.learningtube.domain.video.dto.UpdateLearningStateRequest;
import com.myapp.learningtube.domain.video.dto.UpdatePinRequest;
import com.myapp.learningtube.domain.video.dto.UpdateProgressRequest;
import com.myapp.learningtube.domain.video.dto.UserVideoDetailResponse;
import com.myapp.learningtube.domain.video.support.VideoMetadataPort;
import com.myapp.learningtube.domain.video.support.VideoMetadataSnapshot;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.global.util.YoutubeUrlParser;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserVideoService {

    private static final Logger log = LoggerFactory.getLogger(UserVideoService.class);

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "updatedAt",
                    "createdAt",
                    "completedAt",
                    "lastPositionSec",
                    "pinned",
                    "learningStatus",
                    "priority",
                    "video.title");

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final UserVideoRepository userVideoRepository;
    private final VideoMetadataPort videoMetadataPort;
    private final NoteRepository noteRepository;
    private final HighlightRepository highlightRepository;
    private final TranscriptTrackRepository transcriptTrackRepository;

    public UserVideoService(
            UserRepository userRepository,
            VideoRepository videoRepository,
            UserVideoRepository userVideoRepository,
            VideoMetadataPort videoMetadataPort,
            NoteRepository noteRepository,
            HighlightRepository highlightRepository,
            TranscriptTrackRepository transcriptTrackRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.userVideoRepository = userVideoRepository;
        this.videoMetadataPort = videoMetadataPort;
        this.noteRepository = noteRepository;
        this.highlightRepository = highlightRepository;
        this.transcriptTrackRepository = transcriptTrackRepository;
    }

    @Transactional
    public ImportVideoUrlResponse importFromUrl(Long userId, String rawUrl) {
        String youtubeVideoId = YoutubeUrlParser.extractVideoId(rawUrl)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.VIDEO_INVALID_YOUTUBE_URL, "YouTube URL 형식을 인식할 수 없습니다."));
        log.info("video import-url userId={} youtubeVideoId={}", userId, youtubeVideoId);

        Video video = findOrCreateVideo(youtubeVideoId);
        if (userVideoRepository.existsByUser_IdAndVideo_Id(userId, video.getId())) {
            throw new BusinessException(ErrorCode.USER_VIDEO_DUPLICATE, "이미 내 목록에 등록된 영상입니다.");
        }

        User user = userRepository.getReferenceById(userId);
        UserVideo saved = userVideoRepository.save(new UserVideo(user, video));
        log.info("video import-url done userId={} userVideoId={}", userId, saved.getId());
        return UserVideoDtoMapper.toImportResponse(saved);
    }

    public Page<UserVideo> listForUser(
            Long userId,
            int pageOneBased,
            int size,
            LearningStatus learningStatus,
            Boolean archived,
            String titleQuery,
            String sortParam) {
        Specification<UserVideo> spec = Specification.where(UserVideoSpecs.ownedBy(userId));
        if (learningStatus != null) {
            spec = spec.and(UserVideoSpecs.learningStatusEq(learningStatus));
        }
        if (archived != null) {
            spec = spec.and(UserVideoSpecs.archivedEq(archived));
        }
        if (titleQuery != null && !titleQuery.isBlank()) {
            spec = spec.and(UserVideoSpecs.titleContainsIgnoreCase(titleQuery));
        }

        String sortExpression = normalizeSortExpression(sortParam);
        Sort sort = parseSort(sortExpression);
        int pageIndex = Math.max(0, pageOneBased - 1);
        PageRequest pageable = PageRequest.of(pageIndex, size, sort);

        Page<UserVideo> page = userVideoRepository.findAll(spec, pageable);
        log.debug(
                "user-videos list userId={} page={} size={} total={} learningStatus={} archived={}",
                userId,
                pageOneBased,
                size,
                page.getTotalElements(),
                learningStatus,
                archived);
        return page;
    }

    public UserVideoDetailResponse getDetail(Long userId, Long userVideoId) {
        UserVideo uv = userVideoRepository
                .findByIdAndUserIdWithVideo(userVideoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_VIDEO_NOT_FOUND, "영상을 찾을 수 없습니다."));
        log.debug("user-video detail userId={} userVideoId={}", userId, userVideoId);
        UserVideoDetailResponse dto = UserVideoDtoMapper.toDetail(uv);
        enrichDetailAggregates(dto, uv.getId(), uv.getVideo().getId());
        return dto;
    }

    @Transactional
    public UserVideoDetailResponse updateLearningState(Long userId, Long userVideoId, UpdateLearningStateRequest body) {
        UserVideo uv = loadOwned(userId, userVideoId);
        uv.changeLearningStatus(body.getLearningStatus());
        if (body.getPriority() != null) {
            uv.setPriority(body.getPriority());
        }
        log.info(
                "user-video learning-state userId={} userVideoId={} status={}",
                userId,
                userVideoId,
                body.getLearningStatus());
        UserVideoDetailResponse dto = UserVideoDtoMapper.toDetail(uv);
        enrichDetailAggregates(dto, uv.getId(), uv.getVideo().getId());
        return dto;
    }

    @Transactional
    public UserVideoDetailResponse updateProgress(Long userId, Long userVideoId, UpdateProgressRequest body) {
        if (body.getLastPositionSec() == null && body.getWatchPercent() == null) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED, "lastPositionSec 또는 watchPercent 중 하나 이상 입력해 주세요.");
        }
        UserVideo uv = userVideoRepository
                .findByIdAndUserIdWithVideo(userVideoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_VIDEO_NOT_FOUND, "영상을 찾을 수 없습니다."));
        Video video = uv.getVideo();

        if (body.getLastPositionSec() != null) {
            int pos = body.getLastPositionSec();
            Integer dur = video.getDurationSeconds();
            if (dur != null && dur > 0 && pos > dur) {
                throw new BusinessException(
                        ErrorCode.COMMON_VALIDATION_FAILED, "재생 위치가 영상 길이(" + dur + "초)를 초과할 수 없습니다.");
            }
            uv.setLastPositionSec(pos);
        }
        if (body.getWatchPercent() != null) {
            uv.setWatchPercent(body.getWatchPercent());
        }
        log.info("user-video progress userId={} userVideoId={}", userId, userVideoId);
        UserVideoDetailResponse dto = UserVideoDtoMapper.toDetail(uv);
        enrichDetailAggregates(dto, uv.getId(), uv.getVideo().getId());
        return dto;
    }

    @Transactional
    public UserVideoDetailResponse updatePin(Long userId, Long userVideoId, UpdatePinRequest body) {
        UserVideo uv = loadOwned(userId, userVideoId);
        uv.setPinned(Boolean.TRUE.equals(body.getPinned()));
        log.info("user-video pin userId={} userVideoId={} pinned={}", userId, userVideoId, uv.isPinned());
        UserVideoDetailResponse dto = UserVideoDtoMapper.toDetail(uv);
        enrichDetailAggregates(dto, uv.getId(), uv.getVideo().getId());
        return dto;
    }

    @Transactional
    public UserVideoDetailResponse updateArchive(Long userId, Long userVideoId, UpdateArchiveRequest body) {
        UserVideo uv = loadOwned(userId, userVideoId);
        uv.setArchived(Boolean.TRUE.equals(body.getArchived()));
        log.info("user-video archive userId={} userVideoId={} archived={}", userId, userVideoId, uv.isArchived());
        UserVideoDetailResponse dto = UserVideoDtoMapper.toDetail(uv);
        enrichDetailAggregates(dto, uv.getId(), uv.getVideo().getId());
        return dto;
    }

    private void enrichDetailAggregates(UserVideoDetailResponse dto, long userVideoPk, long videoPk) {
        dto.setNoteCount(noteRepository.countByUserVideo_Id(userVideoPk));
        dto.setHighlightCount(highlightRepository.countByUserVideo_Id(userVideoPk));
        dto.setReviewTargetCount(
                noteRepository.countByUserVideo_IdAndReviewTargetTrue(userVideoPk)
                        + highlightRepository.countByUserVideo_IdAndReviewTargetTrue(userVideoPk));
        dto.setTranscriptTracksAvailable(transcriptTrackRepository.countByVideoId(videoPk) > 0);
        dto.setTranscriptHasSelection(transcriptTrackRepository.findByVideo_IdAndSelectedTrue(videoPk).isPresent());
    }

    private UserVideo loadOwned(Long userId, Long userVideoId) {
        return userVideoRepository
                .findByIdAndUserIdWithVideo(userVideoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_VIDEO_NOT_FOUND, "영상을 찾을 수 없습니다."));
    }

    private Video findOrCreateVideo(String youtubeVideoId) {
        Optional<Video> existing = videoRepository.findByYoutubeVideoId(youtubeVideoId);
        if (existing.isPresent()) {
            return existing.get();
        }
        VideoMetadataSnapshot snap = videoMetadataPort.fetchByYoutubeVideoId(youtubeVideoId);
        Video created = new Video(
                snap.youtubeVideoId(),
                snap.title(),
                snap.description(),
                snap.durationSeconds(),
                snap.thumbnailUrl(),
                snap.channelTitle(),
                snap.channelYoutubeId(),
                VideoSourceType.YOUTUBE);
        return videoRepository.save(created);
    }

    private static String normalizeSortExpression(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return "updatedAt,desc";
        }
        return sortParam.trim();
    }

    private static Sort parseSort(String raw) {
        int lastComma = raw.lastIndexOf(',');
        String prop;
        String dirToken;
        if (lastComma <= 0) {
            prop = raw.trim();
            dirToken = "desc";
        } else {
            prop = raw.substring(0, lastComma).trim();
            dirToken = raw.substring(lastComma + 1).trim();
        }
        if (!ALLOWED_SORT_FIELDS.contains(prop)) {
            prop = "updatedAt";
            dirToken = "desc";
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(dirToken) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, prop);
    }
}
