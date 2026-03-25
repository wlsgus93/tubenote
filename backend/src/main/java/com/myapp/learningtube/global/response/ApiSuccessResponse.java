package com.myapp.learningtube.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 성공 응답 (envelope)")
public class ApiSuccessResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success = true;

    @Schema(description = "요청 추적 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String requestId;

    @Schema(description = "비즈니스 데이터")
    private T data;

    @Schema(description = "페이징 등 부가 정보")
    private Object meta;

    @Schema(description = "선택적 성공 메시지(대부분 API에서 생략)")
    private String message;

    public static <T> ApiSuccessResponse<T> ok(String requestId, T data) {
        return ok(requestId, data, null, null);
    }

    public static <T> ApiSuccessResponse<T> ok(String requestId, T data, Object meta) {
        return ok(requestId, data, meta, null);
    }

    public static <T> ApiSuccessResponse<T> ok(String requestId, T data, Object meta, String message) {
        ApiSuccessResponse<T> body = new ApiSuccessResponse<>();
        body.setRequestId(requestId);
        body.setData(data);
        body.setMeta(meta);
        body.setMessage(message);
        return body;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
