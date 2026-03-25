package com.myapp.learningtube.domain.video;

public enum VideoSourceType {
    /** YouTube watch URL 등으로 수집 */
    YOUTUBE,
    /** 수동 등록(메타만) */
    MANUAL,
    /** 향후 자체 업로드 등 */
    UPLOAD
}
