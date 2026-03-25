package com.myapp.learningtube.global.error;

import com.myapp.learningtube.global.logging.RequestIdFilter;
import com.myapp.learningtube.global.logging.LogMasking;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import com.myapp.learningtube.global.response.ValidationFieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.warn(
                "Constraint violation requestId={} code={} message={}",
                requestId,
                ErrorCode.COMMON_VALIDATION_FAILED.getCode(),
                LogMasking.truncate(ex.getMessage(), 300));
        ApiErrorResponse body = ApiErrorResponse.of(
                requestId,
                ErrorCode.COMMON_VALIDATION_FAILED.getCode(),
                "요청 파라미터가 올바르지 않습니다.",
                null);
        return ResponseEntity.status(ErrorCode.COMMON_VALIDATION_FAILED.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationFieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ValidationFieldError(
                        err.getField(),
                        err.getDefaultMessage() != null ? err.getDefaultMessage() : "유효하지 않은 값입니다.",
                        maskRejectedIfSensitive(err.getField(), err.getRejectedValue())))
                .collect(Collectors.toList());
        String requestId = resolveRequestId(request);
        List<String> fieldNames =
                details.stream().map(ValidationFieldError::getField).collect(Collectors.toList());
        log.warn(
                "Validation failed requestId={} code={} fields={}",
                requestId,
                ErrorCode.COMMON_VALIDATION_FAILED.getCode(),
                fieldNames);
        ApiErrorResponse body = ApiErrorResponse.of(
                requestId,
                ErrorCode.COMMON_VALIDATION_FAILED.getCode(),
                "입력값이 올바르지 않습니다.",
                details);
        return ResponseEntity.status(ErrorCode.COMMON_VALIDATION_FAILED.getHttpStatus()).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        if (ex.getErrorCode() == ErrorCode.USER_INVALID_CREDENTIALS) {
            log.info(
                    "Business requestId={} code={} message={}",
                    requestId,
                    ex.getErrorCode().getCode(),
                    ex.getMessage());
        } else {
            log.warn(
                    "Business requestId={} code={} message={}",
                    requestId,
                    ex.getErrorCode().getCode(),
                    ex.getMessage());
        }
        ApiErrorResponse body =
                ApiErrorResponse.of(
                        requestId, ex.getErrorCode().getCode(), ex.getMessage(), null);
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.warn(
                "Bad request body requestId={} code={} detail={}",
                requestId,
                ErrorCode.COMMON_INVALID_REQUEST.getCode(),
                LogMasking.maskJsonLike(LogMasking.truncate(String.valueOf(ex.getMostSpecificCause().getMessage()), 200)));
        ApiErrorResponse body = ApiErrorResponse.of(
                requestId,
                ErrorCode.COMMON_INVALID_REQUEST.getCode(),
                "요청 본문 형식이 올바르지 않습니다.",
                null);
        return ResponseEntity.status(ErrorCode.COMMON_INVALID_REQUEST.getHttpStatus()).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        String chain = collectThrowableMessages(ex);
        String hint = chain.toUpperCase(Locale.ROOT);
        ErrorCode code;
        String userMessage;
        if (hint.contains("UK_USER_VIDEOS_USER_VIDEO")) {
            code = ErrorCode.USER_VIDEO_DUPLICATE;
            userMessage = "이미 내 목록에 등록된 영상입니다.";
        } else if (hint.contains("UK_VIDEOS_YOUTUBE_VIDEO_ID")) {
            code = ErrorCode.VIDEO_YOUTUBE_ID_DUPLICATE;
            userMessage = "동일 영상이 다른 요청과 충돌했습니다. 잠시 후 다시 시도해 주세요.";
        } else if (hint.contains("UK_COLLECTION_VIDEOS_COLLECTION_USER_VIDEO")) {
            code = ErrorCode.COLLECTION_VIDEO_DUPLICATE;
            userMessage = "이미 이 컬렉션에 포함된 영상입니다.";
        } else if (hint.contains("UK_USER_SUBSCRIPTIONS_USER_CHANNEL")) {
            code = ErrorCode.SUBSCRIPTION_DUPLICATE;
            userMessage = "이미 구독으로 등록된 채널입니다.";
        } else if (hint.contains("UK_LEARNING_QUEUE_ITEMS_USER_USER_VIDEO")) {
            code = ErrorCode.QUEUE_USER_VIDEO_ALREADY_IN_QUEUE;
            userMessage = "이미 학습 큐에 포함된 영상입니다.";
        } else if (hint.contains("UK_TRANSCRIPT_TRACKS_VIDEO_LANG_AUTO")) {
            code = ErrorCode.COMMON_CONFLICT;
            userMessage = "동일 영상·언어·자동생성 조합의 자막 트랙이 이미 있습니다.";
        } else if (hint.contains("UK_TRANSCRIPT_SEGMENTS_TRACK_LINE")) {
            code = ErrorCode.COMMON_CONFLICT;
            userMessage = "자막 세그먼트 순번이 충돌했습니다. 잠시 후 다시 시도해 주세요.";
        } else {
            log.warn(
                    "Data integrity violation requestId={} detail={}",
                    requestId,
                    LogMasking.truncate(chain, 500));
            code = ErrorCode.COMMON_INTERNAL_ERROR;
            userMessage = "데이터 무결성 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        }
        if (code != ErrorCode.COMMON_INTERNAL_ERROR) {
            log.warn(
                    "Data integrity (mapped) requestId={} code={} detail={}",
                    requestId,
                    code.getCode(),
                    LogMasking.truncate(chain, 300));
        }
        ApiErrorResponse body = ApiErrorResponse.of(requestId, code.getCode(), userMessage, null);
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.error(
                "Unhandled exception requestId={} type={} message={}",
                requestId,
                ex.getClass().getSimpleName(),
                LogMasking.truncate(ex.getMessage(), 500),
                ex);
        ApiErrorResponse body = ApiErrorResponse.of(
                requestId,
                ErrorCode.COMMON_INTERNAL_ERROR.getCode(),
                "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                null);
        return ResponseEntity.status(ErrorCode.COMMON_INTERNAL_ERROR.getHttpStatus()).body(body);
    }

    private static String collectThrowableMessages(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable t = ex;
        while (t != null) {
            if (t.getMessage() != null) {
                sb.append(t.getMessage()).append(' ');
            }
            t = t.getCause();
        }
        return sb.toString();
    }

    private static Object maskRejectedIfSensitive(String field, Object rejected) {
        if (rejected == null) {
            return null;
        }
        if (field == null) {
            return rejected;
        }
        String f = field.toLowerCase();
        if (f.contains("password")
                || f.contains("token")
                || f.contains("secret")
                || f.contains("authorization")) {
            return null;
        }
        return rejected;
    }

    private static String resolveRequestId(HttpServletRequest request) {
        if (request != null) {
            Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            Object v = sra.getRequest().getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return java.util.UUID.randomUUID().toString();
    }
}
