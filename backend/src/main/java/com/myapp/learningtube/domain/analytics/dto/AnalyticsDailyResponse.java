package com.myapp.learningtube.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myapp.learningtube.domain.analytics.AnalyticsRangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "일별 추세(범위 메타 + 시계열)")
public class AnalyticsDailyResponse {

    @Schema(description = "요청한 범위 유형")
    private AnalyticsRangeType rangeType;

    @Schema(description = "포함 구간 시작(UTC)")
    private Instant rangeStartUtc;

    @Schema(description = "포함 구간 끝(UTC) — 해당 일 23:59:59.999 의 다음 순간(배타)")
    private Instant rangeEndExclusiveUtc;

    @Schema(description = "일자 오름차순 포인트(활동 없는 날도 0으로 포함)")
    private List<AnalyticsDailyPointDto> points = new ArrayList<>();

    public AnalyticsRangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(AnalyticsRangeType rangeType) {
        this.rangeType = rangeType;
    }

    public Instant getRangeStartUtc() {
        return rangeStartUtc;
    }

    public void setRangeStartUtc(Instant rangeStartUtc) {
        this.rangeStartUtc = rangeStartUtc;
    }

    public Instant getRangeEndExclusiveUtc() {
        return rangeEndExclusiveUtc;
    }

    public void setRangeEndExclusiveUtc(Instant rangeEndExclusiveUtc) {
        this.rangeEndExclusiveUtc = rangeEndExclusiveUtc;
    }

    public List<AnalyticsDailyPointDto> getPoints() {
        return points;
    }

    public void setPoints(List<AnalyticsDailyPointDto> points) {
        this.points = points;
    }
}
