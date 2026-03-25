package com.myapp.learningtube.global.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 프론트 개발 서버 등 브라우저 origin 허용. 운영 도메인은 환경별 yml로 추가.
 */
@ConfigurationProperties(prefix = "learningtube.cors")
public class CorsProperties {

    private List<String> allowedOrigins =
            new ArrayList<>(
                    List.of(
                            "http://localhost:5173",
                            "http://localhost:3000",
                            "http://127.0.0.1:5173",
                            "http://127.0.0.1:3000",
                            "http://192.168.0.5:5173",
                            "http://192.168.0.5:3000"));

    private boolean allowCredentials = true;

    private List<String> allowedMethods =
            new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

    /** {@code *} 는 Authorization 등 프리플라이트에 필요한 헤더를 허용 */
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    private List<String> exposedHeaders = new ArrayList<>(List.of("X-Request-Id", "X-Trace-Id"));

    private long maxAgeSeconds = 3600;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins != null ? allowedOrigins : new ArrayList<>();
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods != null ? allowedMethods : new ArrayList<>();
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders != null ? allowedHeaders : new ArrayList<>();
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders != null ? exposedHeaders : new ArrayList<>();
    }

    public long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }
}
