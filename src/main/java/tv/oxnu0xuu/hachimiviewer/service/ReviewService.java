package tv.oxnu0xuu.hachimiviewer.service;

import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.repository.VideoRepository;

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
        List<Video> videos = videoRepository.findTop50ByIsReviewedFalseOrderByPubDateDesc();
        // Convert the list of Video entities to a list of DTOs for the frontend
        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateVideoStatus(String bvid, boolean isHachimi) {
        try {
            videoRepository.updateHachimiStatus(bvid, isHachimi, LocalDateTime.now());
        } catch (Exception e) {
            throw e;
        }
    }
}