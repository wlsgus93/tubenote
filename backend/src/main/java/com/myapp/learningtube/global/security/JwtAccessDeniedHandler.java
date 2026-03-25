package com.myapp.learningtube.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.global.logging.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String requestId = resolveRequestId(request);
        ApiErrorResponse body =
                ApiErrorResponse.of(
                        requestId,
                        ErrorCode.ACCESS_FORBIDDEN.getCode(),
                        "이 리소스에 접근할 권한이 없습니다.",
                        null);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return java.util.UUID.randomUUID().toString();
    }
}
