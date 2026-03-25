package com.myapp.learningtube.domain.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "YouTube URL 또는 11자 video id로 공용 Video + 내 UserVideo 등록")
public class ImportVideoUrlRequest {

    @NotBlank
    @Size(max = 2048)
    @Schema(description = "YouTube 시청/공유 URL 또는 11자 video id", example = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
