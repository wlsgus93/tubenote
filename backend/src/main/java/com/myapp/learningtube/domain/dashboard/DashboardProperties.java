package com.myapp.learningtube.domain.dashboard;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "learningtube.dashboard")
public class DashboardProperties {

    private int todayPickLimit = 5;
    private int continueWatchingLimit = 10;
    private int recentNotesLimit = 8;
    private int incompleteVideosLimit = 10;
    private int favoriteSubscriptionsLimit = 5;
    private int favoriteChannelRecentVideosLimit = 3;
    /** todayPick 후보 풀(메모리 정렬 전) */
    private int todayPickCandidatePool = 40;

    public int getTodayPickLimit() {
        return todayPickLimit;
    }

    public void setTodayPickLimit(int todayPickLimit) {
        this.todayPickLimit = todayPickLimit;
    }

    public int getContinueWatchingLimit() {
        return continueWatchingLimit;
    }

    public void setContinueWatchingLimit(int continueWatchingLimit) {
        this.continueWatchingLimit = continueWatchingLimit;
    }

    public int getRecentNotesLimit() {
        return recentNotesLimit;
    }

    public void setRecentNotesLimit(int recentNotesLimit) {
        this.recentNotesLimit = recentNotesLimit;
    }

    public int getIncompleteVideosLimit() {
        return incompleteVideosLimit;
    }

    public void setIncompleteVideosLimit(int incompleteVideosLimit) {
        this.incompleteVideosLimit = incompleteVideosLimit;
    }

    public int getFavoriteSubscriptionsLimit() {
        return favoriteSubscriptionsLimit;
    }

    public void setFavoriteSubscriptionsLimit(int favoriteSubscriptionsLimit) {
        this.favoriteSubscriptionsLimit = favoriteSubscriptionsLimit;
    }

    public int getFavoriteChannelRecentVideosLimit() {
        return favoriteChannelRecentVideosLimit;
    }

    public void setFavoriteChannelRecentVideosLimit(int favoriteChannelRecentVideosLimit) {
        this.favoriteChannelRecentVideosLimit = favoriteChannelRecentVideosLimit;
    }

    public int getTodayPickCandidatePool() {
        return todayPickCandidatePool;
    }

    public void setTodayPickCandidatePool(int todayPickCandidatePool) {
        this.todayPickCandidatePool = todayPickCandidatePool;
    }
}
