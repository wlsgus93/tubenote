package com.myapp.learningtube.infra.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(name = "learningtube.youtube.stub", havingValue = "false")
public class YoutubeChannelUploadsRestAdapter implements YoutubeChannelUploadsPort {

    private static final Logger log = LoggerFactory.getLogger(YoutubeChannelUploadsRestAdapter.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YoutubeChannelUploadsRestAdapter(YoutubeApiProperties properties, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(properties.getApiBaseUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean requiresOAuthAccessToken() {
        return true;
    }

    @Override
    public List<YoutubeUploadVideoItem> fetchRecentUploads(
            String accessToken, String channelYoutubeId, int maxResults) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.YOUTUBE_ACCESS_TOKEN_MISSING, "YouTube API용 액세스 토큰이 없습니다.");
        }
        if (channelYoutubeId == null || channelYoutubeId.isBlank()) {
            return List.of();
        }
        int cap = Math.min(Math.max(maxResults, 1), 50);
        String uploadsPlaylistId = fetchUploadsPlaylistId(accessToken, channelYoutubeId);
        if (uploadsPlaylistId == null || uploadsPlaylistId.isBlank()) {
            log.warn("No uploads playlist for channelYoutubeId={}", channelYoutubeId);
            return List.of();
        }
        return fetchPlaylistVideos(accessToken, uploadsPlaylistId, cap);
    }

    private String fetchUploadsPlaylistId(String accessToken, String channelYoutubeId) {
        String uri =
                UriComponentsBuilder.fromPath("/channels")
                        .queryParam("part", "contentDetails")
                        .queryParam("id", channelYoutubeId)
                        .build()
                        .toUriString();
        String body =
                restClient
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                (req, res) -> mapYoutubeError(res.getStatusCode().value(), res.getBody()))
                        .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.path("items");
            if (!items.isArray() || items.isEmpty()) {
                return null;
            }
            return items
                    .get(0)
                    .path("contentDetails")
                    .path("relatedPlaylists")
                    .path("uploads")
                    .asText(null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("channels.list parse failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.YOUTUBE_UPSTREAM_ERROR, "YouTube 채널 정보를 해석할 수 없습니다.");
        }
    }

    private List<YoutubeUploadVideoItem> fetchPlaylistVideos(
            String accessToken, String playlistId, int maxResults) {
        List<YoutubeUploadVideoItem> out = new ArrayList<>();
        String pageToken = null;
        do {
            UriComponentsBuilder ub =
                    UriComponentsBuilder.fromPath("/playlistItems")
                            .queryParam("part", "snippet,contentDetails")
                            .queryParam("playlistId", playlistId)
                            .queryParam("maxResults", Math.min(50, maxResults - out.size()));
            if (pageToken != null) {
                ub.queryParam("pageToken", pageToken);
            }
            String uri = ub.build().toUriString();
            String body =
                    restClient
                            .get()
                            .uri(uri)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .retrieve()
                            .onStatus(
                                    HttpStatusCode::isError,
                                    (req, res) -> mapYoutubeError(res.getStatusCode().value(), res.getBody()))
                            .body(String.class);
            try {
                JsonNode root = objectMapper.readTree(body);
                JsonNode items = root.path("items");
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String videoId =
                                item.path("snippet").path("resourceId").path("videoId").asText(null);
                        if (videoId == null || videoId.isBlank()) {
                            videoId = item.path("contentDetails").path("videoId").asText(null);
                        }
                        if (videoId == null || videoId.isBlank()) {
                            continue;
                        }
                        JsonNode sn = item.path("snippet");
                        String title = sn.path("title").asText("");
                        String desc = sn.path("description").asText("");
                        String published = sn.path("publishedAt").asText(null);
                        Instant publishedAt = parseInstant(published);
                        String thumb = pickThumbnail(sn.path("thumbnails"));
                        out.add(new YoutubeUploadVideoItem(videoId, title, desc, publishedAt, thumb));
                        if (out.size() >= maxResults) {
                            return out;
                        }
                    }
                }
                pageToken = root.path("nextPageToken").asText(null);
                if (pageToken != null && pageToken.isBlank()) {
                    pageToken = null;
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("playlistItems parse failed: {}", e.getMessage());
                throw new BusinessException(ErrorCode.YOUTUBE_UPSTREAM_ERROR, "YouTube 업로드 목록을 해석할 수 없습니다.");
            }
        } while (pageToken != null && out.size() < maxResults);
        return out;
    }

    private static Instant parseInstant(String published) {
        if (published == null || published.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(published).toInstant();
        } catch (Exception e) {
            try {
                return Instant.parse(published);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private static String pickThumbnail(JsonNode thumbs) {
        if (thumbs == null || thumbs.isMissingNode()) {
            return "";
        }
        if (thumbs.has("high")) {
            return thumbs.path("high").path("url").asText("");
        }
        if (thumbs.has("medium")) {
            return thumbs.path("medium").path("url").asText("");
        }
        if (thumbs.has("default")) {
            return thumbs.path("default").path("url").asText("");
        }
        return "";
    }

    private static void mapYoutubeError(int status, java.io.InputStream bodyStream) {
        String body = "";
        try {
            if (bodyStream != null) {
                body = new String(bodyStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }
        String lower = body.toLowerCase();
        if (status == 401) {
            throw new BusinessException(ErrorCode.YOUTUBE_AUTH_FAILED, "YouTube 인증이 유효하지 않습니다. 토큰을 갱신해 주세요.");
        }
        if (status == 403 && (lower.contains("quota") || lower.contains("quotaexceeded"))) {
            throw new BusinessException(ErrorCode.YOUTUBE_QUOTA_EXCEEDED, "YouTube API 할당량이 초과되었습니다.");
        }
        throw new BusinessException(
                ErrorCode.YOUTUBE_UPSTREAM_ERROR,
                "YouTube API 오류(HTTP " + status + "). 잠시 후 다시 시도해 주세요.");
    }
}
