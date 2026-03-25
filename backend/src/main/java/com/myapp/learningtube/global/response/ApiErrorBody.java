package com.myapp.learningtube.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "에러 본문")
public class ApiErrorBody {

    @Schema(description = "기계 판독 코드", example = "COMMON_VALIDATION_FAILED")
    private String code;

    @Schema(description = "사용자 표시용 메시지")
    private String message;

    @Schema(description = "검증 상세 등")
    private Object details;

    public ApiErrorBody() {
    }

    public ApiErrorBody(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}
