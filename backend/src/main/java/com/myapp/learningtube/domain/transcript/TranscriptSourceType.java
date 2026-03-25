package com.myapp.learningtube.domain.transcript;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자막·스크립트 출처")
public enum TranscriptSourceType {
    @Schema(description = "YouTube 자동 생성 자막")
    YOUTUBE_AUTO,
    @Schema(description = "YouTube 업로더 제공 자막")
    YOUTUBE_MANUAL,
    @Schema(description = "파일·외부 임포트")
    IMPORTED,
    @Schema(description = "사용자 붙여넣기 등")
    USER_PASTE
}
