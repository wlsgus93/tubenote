package com.myapp.learningtube.domain.note;

import com.myapp.learningtube.domain.note.dto.CreateNoteRequest;
import com.myapp.learningtube.domain.note.dto.NoteResponse;
import com.myapp.learningtube.domain.note.dto.UpdateNoteRequest;
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
public class NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    private final UserVideoRepository userVideoRepository;
    private final NoteRepository noteRepository;

    public NoteService(UserVideoRepository userVideoRepository, NoteRepository noteRepository) {
        this.userVideoRepository = userVideoRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional
    public NoteResponse create(Long userId, Long userVideoId, CreateNoteRequest body) {
        UserVideo uv = loadOwnedUserVideo(userId, userVideoId);
        validateNoteShape(body.getNoteType(), body.getPositionSec());
        validateTimestampWithinDuration(body.getNoteType(), body.getPositionSec(), uv.getVideo().getDurationSeconds());
        Note saved =
                noteRepository.save(
                        new Note(
                                uv,
                                body.getNoteType(),
                                body.getBody(),
                                body.getNoteType() == NoteType.GENERAL ? null : body.getPositionSec(),
                                body.isReviewTarget(),
                                body.isPinned()));
        log.info("note created userId={} userVideoId={} noteId={}", userId, userVideoId, saved.getId());
        return NoteDtoMapper.toResponse(saved);
    }

    public Page<Note> listForUserVideo(Long userId, Long userVideoId, int pageOneBased, int size) {
        loadOwnedUserVideo(userId, userVideoId);
        int pageIndex = Math.max(0, pageOneBased - 1);
        PageRequest pageable =
                PageRequest.of(pageIndex, size, Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("updatedAt")));
        Page<Note> page = noteRepository.findByUserVideo_IdOrderByPinnedDescUpdatedAtDesc(userVideoId, pageable);
        log.debug(
                "notes list userId={} userVideoId={} page={} size={} total={}",
                userId,
                userVideoId,
                pageOneBased,
                size,
                page.getTotalElements());
        return page;
    }

    @Transactional
    public NoteResponse update(Long userId, Long noteId, UpdateNoteRequest body) {
        if (body.getNoteType() == null
                && body.getBody() == null
                && body.getPositionSec() == null
                && body.getReviewTarget() == null
                && body.getPinned() == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "수정할 필드를 하나 이상 보내 주세요.");
        }
        Note note =
                noteRepository
                        .findByIdAndUserVideo_User_Id(noteId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND, "노트를 찾을 수 없습니다."));

        if (body.getNoteType() != null) {
            note.setNoteType(body.getNoteType());
        }
        if (body.getBody() != null) {
            note.setBody(body.getBody());
        }
        if (body.getPositionSec() != null) {
            note.setPositionSec(body.getPositionSec());
        }
        if (body.getReviewTarget() != null) {
            note.setReviewTarget(body.getReviewTarget());
        }
        if (body.getPinned() != null) {
            note.setPinned(body.getPinned());
        }

        if (note.getNoteType() == NoteType.GENERAL) {
            note.setPositionSec(null);
        }

        validateNoteShape(
                note.getNoteType(),
                note.getNoteType() == NoteType.GENERAL ? null : note.getPositionSec());
        validateTimestampWithinDuration(
                note.getNoteType(), note.getNoteType() == NoteType.TIMESTAMP ? note.getPositionSec() : null, note.getUserVideo().getVideo().getDurationSeconds());

        log.info("note updated userId={} userVideoId={} noteId={}", userId, note.getUserVideo().getId(), noteId);
        return NoteDtoMapper.toResponse(note);
    }

    @Transactional
    public void delete(Long userId, Long noteId) {
        Note note =
                noteRepository
                        .findByIdAndUserVideo_User_Id(noteId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND, "노트를 찾을 수 없습니다."));
        Long uvId = note.getUserVideo().getId();
        noteRepository.delete(note);
        log.info("note deleted userId={} userVideoId={} noteId={}", userId, uvId, noteId);
    }

    private UserVideo loadOwnedUserVideo(Long userId, Long userVideoId) {
        return userVideoRepository
                .findByIdAndUser_Id(userVideoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_VIDEO_NOT_FOUND, "영상을 찾을 수 없습니다."));
    }

    static void validateNoteShape(NoteType type, Integer positionSec) {
        if (type == NoteType.TIMESTAMP) {
            if (positionSec == null || positionSec < 0) {
                throw new BusinessException(
                        ErrorCode.COMMON_VALIDATION_FAILED, "TIMESTAMP 노트는 positionSec(0 이상)이 필요합니다.");
            }
        } else {
            if (positionSec != null) {
                throw new BusinessException(
                        ErrorCode.COMMON_VALIDATION_FAILED, "GENERAL 노트에는 positionSec을 지정할 수 없습니다.");
            }
        }
    }

    private static void validateTimestampWithinDuration(NoteType type, Integer positionSec, Integer videoDurationSec) {
        if (type != NoteType.TIMESTAMP || positionSec == null) {
            return;
        }
        if (videoDurationSec != null && videoDurationSec > 0 && positionSec > videoDurationSec) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED,
                    "positionSec가 영상 길이(" + videoDurationSec + "초)를 초과할 수 없습니다.");
        }
    }
}
