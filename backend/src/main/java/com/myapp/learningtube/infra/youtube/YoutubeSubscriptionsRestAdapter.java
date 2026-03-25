package com.myapp.learningtube.infra.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
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

/**
 * YouTube Data API v3 {@code subscriptions.list} (mine=true). 토큰은 Google OAuth(YouTube 범위) 기준.
 */
@Component
@ConditionalOnProperty(name = "learningtube.youtube.stub", havingValue = "false")
public class YoutubeSubscriptionsRestAdapter implements YoutubeSubscriptionsPort {

    private static final Logger log = LoggerFactory.getLogger(YoutubeSubscriptionsRestAdapter.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YoutubeSubscriptionsRestAdapter(YoutubeApiProperties properties, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(properties.getApiBaseUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean requiresOAuthAccessToken() {
        return true;
    }

    @Override
    public List<YoutubeSubscriptionListItem> fetchMineSubscribedChannels(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.YOUTUBE_ACCESS_TOKEN_MISSING, "YouTube API용 액세스 토큰이 없습니다.");
        }
        List<YoutubeSubscriptionListItem> all = new ArrayList<>();
        String pageToken = null;
        do {
            UriComponentsBuilder ub =
                    UriComponentsBuilder.fromPath("/subscriptions")
                            .queryParam("part", "snippet")
                            .queryParam("mine", "true")
                            .queryParam("maxResults", 50);
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
                                    (req, res) ->
                                            mapYoutubeError(res.getStatusCode().value(), res.getBody()))
                            .body(String.class);
            try {
                JsonNode root = objectMapper.readTree(body);
                JsonNode items = root.path("items");
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String subId = item.path("id").asText(null);
                        JsonNode sn = item.path("snippet");
                        String channelId = sn.path("resourceId").path("channelId").asText(null);
                        String title = sn.path("title").asText("");
                        String desc = sn.path("description").asText("");
                        String thumb = "";
                        JsonNode thumbs = sn.path("thumbnails");
                        if (thumbs.has("high")) {
                            thumb = thumbs.path("high").path("url").asText("");
                        } else if (thumbs.has("medium")) {
                            thumb = thumbs.path("medium").path("url").asText("");
                        } else if (thumbs.has("default")) {
                            thumb = thumbs.path("default").path("url").asText("");
                        }
                        if (channelId != null && !channelId.isBlank()) {
                            all.add(new YoutubeSubscriptionListItem(subId, channelId, title, desc, thumb));
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
                log.warn("YouTube subscriptions parse failed: {}", e.getMessage());
                throw new BusinessException(ErrorCode.YOUTUBE_UPSTREAM_ERROR, "YouTube 응답을 해석할 수 없습니다.");
            }
        } while (pageToken != null);
        return all;
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
