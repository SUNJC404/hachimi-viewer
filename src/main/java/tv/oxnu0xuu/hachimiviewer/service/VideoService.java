package tv.oxnu0xuu.hachimiviewer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.repository.VideoRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Transactional(readOnly = true)
    public List<VideoReviewDto> getLatestHachimiVideos(int page, int size) {
        List<Video> videos = videoRepository.findHachimiVideosOrderByPubDateDesc(PageRequest.of(page, size));
        // 5. 将 Video 实体列表转换为 DTO 列表
        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoReviewDto> getRandomHachimiVideos(int limit) {
        List<Video> videos = videoRepository.findRandomHachimiVideos(limit);
        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 搜索功能
    @Transactional(readOnly = true)
    public List<VideoReviewDto> searchHachimiVideos(String query, int page, int size) {
        List<Video> videos = videoRepository.searchHachimiVideos(query, PageRequest.of(page, size));
        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }
}