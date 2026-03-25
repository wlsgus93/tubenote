package com.myapp.learningtube.domain.transcript;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptSegmentRepository extends JpaRepository<TranscriptSegment, Long> {

    List<TranscriptSegment> findByTranscriptTrack_IdOrderByLineIndexAsc(Long transcriptTrackId);

    long countByTranscriptTrack_Id(Long transcriptTrackId);

    void deleteByTranscriptTrack_Id(Long transcriptTrackId);
}
