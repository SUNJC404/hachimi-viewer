package tv.oxnu0xuu.hachimiviewer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Transactional(readOnly = true)
    public List<VideoReviewDto> getLatestHachimiVideos(int page, int size) {
        int offset = page * size;
        List<Video> videos = videoMapper.findHachimiVideosOrderByPubDateDesc(offset, size);
        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoReviewDto> getRandomHachimiVideos(int limit) {
        List<Video> videos = videoMapper.findRandomHachimiVideos(limit);
        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }
}