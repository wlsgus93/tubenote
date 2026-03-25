package com.myapp.learningtube.domain.analytics;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "learningtube.analytics")
public class AnalyticsProperties {

    /** rangeType=ALL 일 때 일자 버킷 최대 개수(과다 응답 방지). */
    private int maxDailyBuckets = 366;

    /** WEEK 범위 일수(기본 7). */
    private int weekDays = 7;

    /** MONTH 범위 일수(기본 30). */
    private int monthDays = 30;

    public int getMaxDailyBuckets() {
        return maxDailyBuckets;
    }

    public void setMaxDailyBuckets(int maxDailyBuckets) {
        this.maxDailyBuckets = maxDailyBuckets;
    }

    public int getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(int weekDays) {
        this.weekDays = weekDays;
    }

    public int getMonthDays() {
        return monthDays;
    }

    public void setMonthDays(int monthDays) {
        this.monthDays = monthDays;
    }
}
