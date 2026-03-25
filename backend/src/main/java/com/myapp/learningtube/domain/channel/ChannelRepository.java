package com.myapp.learningtube.domain.channel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByYoutubeChannelId(String youtubeChannelId);

    List<Channel> findByYoutubeChannelIdIn(Collection<String> youtubeChannelIds);
}
