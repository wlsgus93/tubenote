package com.myapp.learningtube.domain.collection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "컬렉션에 내 영상(UserVideo) 추가")
public class AddCollectionVideoRequest {

    @NotNull
    @Schema(description = "추가할 UserVideo PK", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userVideoId;

    public Long getUserVideoId() {
        return userVideoId;
    }

    public void setUserVideoId(Long userVideoId) {
        this.userVideoId = userVideoId;
    }
}
