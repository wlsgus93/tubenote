package com.myapp.learningtube.domain.analytics;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일별 추세 조회 범위")
public enum AnalyticsRangeType {
    @Schema(description = "최근 7일(UTC)")
    WEEK,
    @Schema(description = "최근 30일(UTC)")
    MONTH,
    @Schema(description = "전체(활동 시작일~오늘, 일자 버킷 상한은 설정값)")
    ALL
}
