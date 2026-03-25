package com.myapp.learningtube.domain.note;

/** 노트 종류: 일반 메모 vs 재생 시각(초) 기준 메모 */
public enum NoteType {
    /** 본문만; 재생 위치 없음 */
    GENERAL,
    /** {@code positionSec} 필수 */
    TIMESTAMP
}
