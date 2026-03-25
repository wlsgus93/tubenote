package com.myapp.learningtube.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "필드 검증 오류 1건")
public class ValidationFieldError {

    @Schema(description = "DTO 필드명 (camelCase)", example = "email")
    private String field;

    @Schema(description = "거절 사유")
    private String reason;

    @Schema(description = "거절된 값 (민감정보는 null)")
    private Object rejectedValue;

    public ValidationFieldError() {
    }

    public ValidationFieldError(String field, String reason, Object rejectedValue) {
        this.field = field;
        this.reason = reason;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }
}
