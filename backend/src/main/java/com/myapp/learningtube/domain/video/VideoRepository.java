package com.myapp.learningtube.domain.video;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByYoutubeVideoId(String youtubeVideoId);

    boolean existsByYoutubeVideoId(String youtubeVideoId);
}
