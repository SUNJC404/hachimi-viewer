package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.VideoDetailDto;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private VideoMapper videoMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getVideos(int page, int size, String search, Boolean isHachimi) {
        // 构建查询条件
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();

        if (search != null && !search.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("bvid", search)
                    .or()
                    .like("title", search)
                    .or()
                    .like("description", search));
        }

        if (isHachimi != null) {
            queryWrapper.eq("is_hachimi", isHachimi);
        }

        // 按发布时间倒序
        queryWrapper.orderByDesc("pub_date");

        // 分页查询
        Page<Video> videoPage = new Page<>(page + 1, size); // MyBatis-Plus 页码从1开始
        Page<Video> result = videoMapper.selectPage(videoPage, queryWrapper);

        // 获取视频列表
        List<Video> videos = result.getRecords();

        // 手动查询并设置 owner 信息
        List<VideoDetailDto> videoDtos = videos.stream().map(video -> {
            // 使用我们自定义的查询方法获取带有 owner 信息的视频
            List<Video> videosWithOwner = videoMapper.selectList(
                    new QueryWrapper<Video>().eq("bvid", video.getBvid())
            );
            if (!videosWithOwner.isEmpty()) {
                Video fullVideo = videosWithOwner.get(0);
                // 手动查询 owner
                fullVideo = getVideoWithOwner(fullVideo.getBvid());
                return VideoDetailDto.fromEntity(fullVideo);
            }
            return VideoDetailDto.fromEntity(video);
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", videoDtos);
        response.put("totalElements", result.getTotal());
        response.put("totalPages", result.getPages());
        response.put("currentPage", page);
        response.put("pageSize", size);

        return response;
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
        List<Video> videos = videoMapper.findHachimiVideosOrderByPubDateDesc(0, Integer.MAX_VALUE);
        return videos.stream()
                .filter(v -> v.getBvid().equals(bvid))
                .findFirst()
                .orElse(videoMapper.selectById(bvid));
    }
}