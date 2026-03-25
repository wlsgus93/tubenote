package com.myapp.learningtube.global.util;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTube 공유/시청 URL에서 video id 추출 (최소 지원: watch, youtu.be, shorts, embed, live).
 */
public final class YoutubeUrlParser {

    private static final Pattern ID_11 = Pattern.compile("^([a-zA-Z0-9_-]{11})$");
    private static final Pattern PATH_SHORTS = Pattern.compile("/shorts/([a-zA-Z0-9_-]{11})");
    private static final Pattern PATH_EMBED = Pattern.compile("/embed/([a-zA-Z0-9_-]{11})");
    private static final Pattern PATH_LIVE = Pattern.compile("/live/([a-zA-Z0-9_-]{11})");
    private static final Pattern PATH_YOUTU_BE = Pattern.compile("^/([a-zA-Z0-9_-]{11})(?:/.*)?$");
    private static final Pattern QUERY_V = Pattern.compile("(?:^|&)v=([a-zA-Z0-9_-]{11})");

    private YoutubeUrlParser() {}

    public static Optional<String> extractVideoId(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return Optional.empty();
        }
        String trimmed = rawUrl.trim();
        Matcher direct = ID_11.matcher(trimmed);
        if (direct.matches()) {
            return Optional.of(trimmed);
        }
        try {
            String withScheme = trimmed.startsWith("http") ? trimmed : "https://" + trimmed;
            URI uri = URI.create(withScheme);
            String host = uri.getHost();
            if (host == null) {
                return Optional.empty();
            }
            String hostLower = host.toLowerCase();
            String path = uri.getPath() != null ? uri.getPath() : "";

            Matcher shorts = PATH_SHORTS.matcher(path);
            if (shorts.find()) {
                return Optional.of(shorts.group(1));
            }
            Matcher embed = PATH_EMBED.matcher(path);
            if (embed.find()) {
                return Optional.of(embed.group(1));
            }
            Matcher live = PATH_LIVE.matcher(path);
            if (live.find()) {
                return Optional.of(live.group(1));
            }

            if (hostLower.equals("youtu.be") || hostLower.endsWith(".youtu.be")) {
                Matcher be = PATH_YOUTU_BE.matcher(path);
                if (be.matches()) {
                    return Optional.of(be.group(1));
                }
            }

            if (hostLower.endsWith("youtube.com") || hostLower.endsWith("youtube-nocookie.com")) {
                String q = uri.getRawQuery();
                if (q != null) {
                    Matcher v = QUERY_V.matcher(q);
                    if (v.find()) {
                        return Optional.of(v.group(1));
                    }
                }
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
