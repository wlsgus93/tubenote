package com.myapp.learningtube.global.logging;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 로그 출력용 최소 마스킹 (본문 전량 로깅은 하지 않음).
 */
public final class LogMasking {

    private static final Pattern BEARER = Pattern.compile("(?i)Bearer\\s+\\S+");
    private static final Pattern JSON_PASSWORD =
            Pattern.compile("(\"(?:password|accessToken|refreshToken|secret|authorization)\"\\s*:\\s*\")([^\"]*)(\")");

    private LogMasking() {}

    public static String maskAuthorizationHeader(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return "Bearer ***";
        }
        return "***";
    }

    /** 쿼리 스트링에서 흔한 민감 파라미터 값만 마스킹. */
    public static String sanitizeQueryString(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return queryString;
        }
        String[] parts = queryString.split("&");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('&');
            }
            int eq = parts[i].indexOf('=');
            if (eq < 0) {
                sb.append(parts[i]);
                continue;
            }
            String name = parts[i].substring(0, eq);
            sb.append(name).append('=');
            if (isSensitiveQueryParam(name)) {
                sb.append("***");
            } else {
                String v = parts[i].length() > eq + 1 ? parts[i].substring(eq + 1) : "";
                sb.append(truncate(v, 64));
            }
        }
        return sb.toString();
    }

    public static boolean isSensitiveQueryParam(String name) {
        if (name == null) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("token")
                || n.contains("password")
                || n.contains("secret")
                || n.contains("authorization")
                || n.equals("code");
    }

    public static String truncate(String s, int maxLen) {
        if (s == null) {
            return null;
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "…(" + s.length() + " chars)";
    }

    /** 디버그용 JSON 일부에 대한 가벼운 마스킹(완전한 파서 아님). */
    public static String maskJsonLike(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            return fragment;
        }
        String m = BEARER.matcher(fragment).replaceAll("Bearer ***");
        return JSON_PASSWORD.matcher(m).replaceAll("$1***$3");
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);
        String prefix = local.length() <= 2 ? "*" : local.substring(0, 2) + "***";
        return prefix + "@" + domain;
    }
}
