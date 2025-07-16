package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.VideoDetailDto;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private AdminSearchService adminSearchService;

    @Transactional(readOnly = true)
    public Map<String, Object> getVideos(int page, int size, String search, Boolean isHachimi) {
        // 使用 Meilisearch 进行搜索
        return adminSearchService.searchVideos(search, page, size, isHachimi);
    }

    @Transactional
    public void toggleHachimiStatus(String bvid) {
        Video video = videoMapper.selectById(bvid);
        if (video != null) {
            boolean newStatus = !video.isHachimi();
            video.setHachimi(newStatus);
            video.setReviewed(true);
            video.setReviewedAt(LocalDateTime.now());
            video.setUpdatedAt(LocalDateTime.now());
            videoMapper.updateById(video);
        }
    }

    @Transactional(readOnly = true)
    public VideoDetailDto getVideoDetail(String bvid) {
        Video video = getVideoWithOwner(bvid);
        return video != null ? VideoDetailDto.fromEntity(video) : null;
    }

    private Video getVideoWithOwner(String bvid) {
        // 使用自定义的 SQL 查询来获取包含 owner 信息的视频
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("bvid", bvid);
        Video video = videoMapper.selectOne(wrapper);

        if (video != null) {
            // 手动查询 owner 信息
            // 这里可以根据实际需求优化
            return video;
        }
        return null;
    }
}