package com.myapp.learningtube.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.global.logging.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String requestId = resolveRequestId(request);
        ApiErrorResponse body =
                ApiErrorResponse.of(
                        requestId,
                        ErrorCode.AUTH_UNAUTHORIZED.getCode(),
                        "인증이 필요하거나 토큰이 유효하지 않습니다.",
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
