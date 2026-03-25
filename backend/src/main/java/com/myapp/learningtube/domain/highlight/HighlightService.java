package com.myapp.learningtube.domain.highlight;

import com.myapp.learningtube.domain.highlight.dto.CreateHighlightRequest;
import com.myapp.learningtube.domain.highlight.dto.HighlightResponse;
import com.myapp.learningtube.domain.highlight.dto.UpdateHighlightRequest;
import com.myapp.learningtube.domain.video.UserVideo;
import com.myapp.learningtube.domain.video.UserVideoRepository;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HighlightService {

    private static final Logger log = LoggerFactory.getLogger(HighlightService.class);

    private final UserVideoRepository userVideoRepository;
    private final HighlightRepository highlightRepository;

    public HighlightService(UserVideoRepository userVideoRepository, HighlightRepository highlightRepository) {
        this.userVideoRepository = userVideoRepository;
        this.highlightRepository = highlightRepository;
    }

    @Transactional
    public HighlightResponse create(Long userId, Long userVideoId, CreateHighlightRequest body) {
        UserVideo uv = loadOwnedUserVideo(userId, userVideoId);
        int start = body.getStartSec();
        int end = body.getEndSec();
        validateHighlightRange(start, end, uv.getVideo().getDurationSeconds());
        Highlight saved =
                highlightRepository.save(
                        new Highlight(uv, start, end, body.getMemo(), body.isReviewTarget(), body.isPinned()));
        log.info("highlight created userId={} userVideoId={} highlightId={}", userId, userVideoId, saved.getId());
        return HighlightDtoMapper.toResponse(saved);
    }

    public Page<Highlight> listForUserVideo(Long userId, Long userVideoId, int pageOneBased, int size) {
        loadOwnedUserVideo(userId, userVideoId);
        int pageIndex = Math.max(0, pageOneBased - 1);
        PageRequest pageable =
                PageRequest.of(pageIndex, size, Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("updatedAt")));
        Page<Highlight> page =
                highlightRepository.findByUserVideo_IdOrderByPinnedDescUpdatedAtDesc(userVideoId, pageable);
        log.debug(
                "highlights list userId={} userVideoId={} page={} size={} total={}",
                userId,
                userVideoId,
                pageOneBased,
                size,
                page.getTotalElements());
        return page;
    }

    @Transactional
    public HighlightResponse update(Long userId, Long highlightId, UpdateHighlightRequest body) {
        if (body.getStartSec() == null
                && body.getEndSec() == null
                && body.getMemo() == null
                && body.getReviewTarget() == null
                && body.getPinned() == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "수정할 필드를 하나 이상 보내 주세요.");
        }
        Highlight h =
                highlightRepository
                        .findByIdAndUserVideo_User_Id(highlightId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.HIGHLIGHT_NOT_FOUND, "하이라이트를 찾을 수 없습니다."));

        if (body.getStartSec() != null) {
            h.setStartSec(body.getStartSec());
        }
        if (body.getEndSec() != null) {
            h.setEndSec(body.getEndSec());
        }
        if (body.getMemo() != null) {
            h.setMemo(body.getMemo());
        }
        if (body.getReviewTarget() != null) {
            h.setReviewTarget(body.getReviewTarget());
        }
        if (body.getPinned() != null) {
            h.setPinned(body.getPinned());
        }

        validateHighlightRange(h.getStartSec(), h.getEndSec(), h.getUserVideo().getVideo().getDurationSeconds());

        log.info(
                "highlight updated userId={} userVideoId={} highlightId={}",
                userId,
                h.getUserVideo().getId(),
                highlightId);
        return HighlightDtoMapper.toResponse(h);
    }

    @Transactional
    public void delete(Long userId, Long highlightId) {
        Highlight h =
                highlightRepository
                        .findByIdAndUserVideo_User_Id(highlightId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.HIGHLIGHT_NOT_FOUND, "하이라이트를 찾을 수 없습니다."));
        Long uvId = h.getUserVideo().getId();
        highlightRepository.delete(h);
        log.info("highlight deleted userId={} userVideoId={} highlightId={}", userId, uvId, highlightId);
    }

    private UserVideo loadOwnedUserVideo(Long userId, Long userVideoId) {
        return userVideoRepository
                .findByIdAndUser_Id(userVideoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_VIDEO_NOT_FOUND, "영상을 찾을 수 없습니다."));
    }

    static void validateHighlightRange(int startSec, int endSec, Integer videoDurationSec) {
        if (startSec < 0 || endSec < 0) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "구간(초)은 0 이상이어야 합니다.");
        }
        if (endSec < startSec) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "endSec는 startSec 이상이어야 합니다.");
        }
        if (videoDurationSec != null && videoDurationSec > 0 && endSec > videoDurationSec) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED,
                    "하이라이트 종료 시각이 영상 길이(" + videoDurationSec + "초)를 초과할 수 없습니다.");
        }
    }
}
