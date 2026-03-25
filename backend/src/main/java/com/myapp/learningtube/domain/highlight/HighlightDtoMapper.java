package com.myapp.learningtube.domain.highlight;

import com.myapp.learningtube.domain.highlight.dto.HighlightResponse;

public final class HighlightDtoMapper {

    private HighlightDtoMapper() {}

    public static HighlightResponse toResponse(Highlight h) {
        HighlightResponse r = new HighlightResponse();
        r.setHighlightId(h.getId());
        r.setUserVideoId(h.getUserVideo().getId());
        r.setStartSec(h.getStartSec());
        r.setEndSec(h.getEndSec());
        r.setMemo(h.getMemo());
        r.setReviewTarget(h.isReviewTarget());
        r.setPinned(h.isPinned());
        r.setCreatedAt(h.getCreatedAt());
        r.setUpdatedAt(h.getUpdatedAt());
        return r;
    }
}
