package com.myapp.learningtube.global.error;

import org.springframework.http.HttpStatus;

/**
 * API error.code 문자열 (backend-api-spec.md 와 동기화).
 */
public enum ErrorCode {

    COMMON_VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    COMMON_INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    /** 일반 충돌(유니크 등) */
    COMMON_CONFLICT(HttpStatus.CONFLICT),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

    USER_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN),

    VIDEO_INVALID_YOUTUBE_URL(HttpStatus.BAD_REQUEST),
    /** 동시 요청 등으로 공용 Video 유니크(youtube_video_id) 충돌 시 */
    VIDEO_YOUTUBE_ID_DUPLICATE(HttpStatus.CONFLICT),
    /** 공용 Video(id) 없음 */
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_VIDEO_DUPLICATE(HttpStatus.CONFLICT),

    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND),
    HIGHLIGHT_NOT_FOUND(HttpStatus.NOT_FOUND),

    COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND),
    COLLECTION_NAME_DUPLICATE(HttpStatus.CONFLICT),
    COLLECTION_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND),
    COLLECTION_VIDEO_DUPLICATE(HttpStatus.CONFLICT),
    COLLECTION_VIDEO_ORDER_INVALID(HttpStatus.BAD_REQUEST),

    /** 내 구독 행 없음 또는 소유 아님 */
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND),
    /** 동일 사용자·채널 구독 중복(동시성 등) */
    SUBSCRIPTION_DUPLICATE(HttpStatus.CONFLICT),

    /** Google OAuth 행에 YouTube용 액세스 토큰 없음 */
    YOUTUBE_ACCESS_TOKEN_MISSING(HttpStatus.BAD_REQUEST),
    YOUTUBE_AUTH_FAILED(HttpStatus.UNAUTHORIZED),
    YOUTUBE_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    YOUTUBE_UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY),

    /** LearningQueueItem 없음 또는 타인 소유(404 통일) */
    QUEUE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND),
    /** 동일 UserVideo는 큐 전체에서 1행만 허용 */
    QUEUE_USER_VIDEO_ALREADY_IN_QUEUE(HttpStatus.CONFLICT),
    /** 순서 변경 요청 집합이 현재 큐 멤버와 불일치 */
    QUEUE_ORDER_INVALID(HttpStatus.BAD_REQUEST),

    /** 내 목록에 없는 영상에 대한 Transcript API 호출 */
    TRANSCRIPT_ACCESS_DENIED(HttpStatus.FORBIDDEN),
    /** TranscriptTrack 없음 또는 해당 videoId에 속하지 않음 */
    TRANSCRIPT_TRACK_NOT_FOUND(HttpStatus.NOT_FOUND);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return name();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
