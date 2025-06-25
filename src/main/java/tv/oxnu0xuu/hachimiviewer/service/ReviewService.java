package tv.oxnu0xuu.hachimiviewer.service;

import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private VideoRepository videoRepository;

    @Transactional(readOnly = true)
    public List<VideoReviewDto> getReviewQueue() {
        // Fetch 50 unreviewed videos from the database
        List<Video> videos = videoRepository.findTop50ByIsReviewedFalseOrderByPubDateAsc();
        // Convert the list of Video entities to a list of DTOs for the frontend
        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateVideoStatus(String bvid, boolean isHachimi) {
        // Find the video by its BVID, or throw an exception if not found
        Video video = videoRepository.findById(bvid)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with BVID: " + bvid));

        // Update the status
        video.setHachimi(isHachimi);
        video.setReviewed(true);
        video.setReviewedAt(LocalDateTime.now());

        // Save the changes back to the database
        videoRepository.save(video);
    }
}