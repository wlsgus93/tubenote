package com.myapp.learningtube.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 서블릿 필터: 요청마다 {@code X-Request-Id}(없으면 생성)·선택 {@code X-Trace-Id}를 MDC·응답 헤더에 넣어 로그·에러 응답을 한 요청으로 묶는다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_ATTRIBUTE = "com.myapp.learningtube.requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final int MAX_ID_LENGTH = 128;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = truncateId(request.getHeader(REQUEST_ID_HEADER));
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        MDC.put(LogMdc.REQUEST_ID, requestId);

        String traceId = truncateId(request.getHeader(TRACE_ID_HEADER));
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(LogMdc.TRACE_ID, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private static String truncateId(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.length() > MAX_ID_LENGTH) {
            return t.substring(0, MAX_ID_LENGTH);
        }
        return t;
    }
}
