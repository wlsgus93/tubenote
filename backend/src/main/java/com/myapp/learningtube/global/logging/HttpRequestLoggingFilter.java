package com.myapp.learningtube.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 서블릿 필터: 요청 1건이 끝날 때 메서드·경로·상태·소요 ms를 한 줄로 남긴다(본문은 기록하지 않음).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.equals("/swagger-ui.html")
                || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long ms = System.currentTimeMillis() - start;
            if (!shouldNotFilter(request)) {
                String path = request.getRequestURI();
                String qs = request.getQueryString();
                if (qs != null && !qs.isBlank()) {
                    path = path + "?" + LogMasking.sanitizeQueryString(qs);
                }
                int status = response.getStatus();
                if (status >= 500) {
                    log.warn("HTTP {} {} -> {} {}ms", request.getMethod(), path, status, ms);
                } else if (status >= 400) {
                    log.info("HTTP {} {} -> {} {}ms", request.getMethod(), path, status, ms);
                } else if (status >= 300) {
                    log.info("HTTP {} {} -> {} {}ms", request.getMethod(), path, status, ms);
                } else {
                    log.debug("HTTP {} {} -> {} {}ms", request.getMethod(), path, status, ms);
                }
            }
        }
    }
}
