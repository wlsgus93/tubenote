package com.myapp.learningtube.domain.note;

import com.myapp.learningtube.domain.note.dto.NoteResponse;

public final class NoteDtoMapper {

    private NoteDtoMapper() {}

    public static NoteResponse toResponse(Note n) {
        NoteResponse r = new NoteResponse();
        r.setNoteId(n.getId());
        r.setUserVideoId(n.getUserVideo().getId());
        r.setNoteType(n.getNoteType());
        r.setBody(n.getBody());
        r.setPositionSec(n.getPositionSec());
        r.setReviewTarget(n.isReviewTarget());
        r.setPinned(n.isPinned());
        r.setCreatedAt(n.getCreatedAt());
        r.setUpdatedAt(n.getUpdatedAt());
        return r;
    }
}
