package com.myapp.learningtube.global.logging;

/**
 * SLF4J MDC 키 (backend-logging-spec.md 정합).
 */
public final class LogMdc {

    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";

    private LogMdc() {}
}
