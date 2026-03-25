package com.myapp.learningtube.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 실패 응답 (envelope)")
public class ApiErrorResponse {

    @Schema(description = "성공 여부", example = "false")
    private boolean success = false;

    @Schema(description = "요청 추적 ID")
    private String requestId;

    @Schema(description = "에러 정보")
    private ApiErrorBody error;

    public static ApiErrorResponse of(String requestId, String code, String message, Object details) {
        ApiErrorResponse body = new ApiErrorResponse();
        body.setRequestId(requestId);
        body.setError(new ApiErrorBody(code, message, details));
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

    public ApiErrorBody getError() {
        return error;
    }

    public void setError(ApiErrorBody error) {
        this.error = error;
    }
}
