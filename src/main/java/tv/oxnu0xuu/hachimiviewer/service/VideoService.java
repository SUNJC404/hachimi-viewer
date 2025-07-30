package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
        // **FIX: Replaced inefficient ORDER BY RAND() with a more stable and performant approach.**
        Long count = videoMapper.countHachimiVideos();
        if (count == null || count == 0) {
            return Collections.emptyList();
        }

        List<String> bvids;
        if (count <= limit) {
            // If we have fewer videos than the limit, just get all of them.
            bvids = videoMapper.findHachimiBvidsWithOffset(0, count.intValue());
        } else {
            // Calculate a random starting point (offset) and fetch a slice.
            long offset = ThreadLocalRandom.current().nextLong(count - limit);
            bvids = videoMapper.findHachimiBvidsWithOffset(offset, limit);
        }

        if (bvids == null || bvids.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch the full data for the randomly selected BVIDs.
        List<Video> videos = videoMapper.findVideosWithOwnersByBvids(bvids);

        // Shuffle the final list to ensure the order is random.
        Collections.shuffle(videos);

        return videos.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reportVideo(String bvid) {
        Video video = videoMapper.selectById(bvid);
        if (video != null) {
            video.setReported(true);
            videoMapper.updateById(video);
        }
    }
}