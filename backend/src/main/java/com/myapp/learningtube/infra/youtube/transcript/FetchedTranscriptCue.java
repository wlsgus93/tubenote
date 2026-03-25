package com.myapp.learningtube.infra.youtube.transcript;

/**
 * 외부(YouTube 등)에서 가져온 한 줄 자막. 본문은 로그에 대량 출력하지 않는다.
 */
public record FetchedTranscriptCue(double startSeconds, double endSeconds, String text) {}
