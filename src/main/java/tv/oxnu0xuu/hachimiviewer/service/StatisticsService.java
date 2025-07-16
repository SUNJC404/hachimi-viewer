package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private VideoMapper videoMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 全部哈基米视频数量
        Long totalHachimiVideos = videoMapper.selectCount(
                new QueryWrapper<Video>().eq("is_hachimi", true)
        );
        stats.put("totalHachimiVideos", totalHachimiVideos);

        // 2. 全部作者数量（通过 distinct owner_mid 统计）
        Long totalAuthors = countDistinctAuthors();
        stats.put("totalAuthors", totalAuthors);

        // 3. 今日审核视频数量
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Long todayReviewedVideos = videoMapper.selectCount(
                new QueryWrapper<Video>()
                        .eq("is_reviewed", true)
                        .between("reviewed_at", todayStart, todayEnd)
        );
        stats.put("todayReviewedVideos", todayReviewedVideos);

        // 4. 今日哈基米视频数量（今天审核通过的哈基米视频）
        Long todayHachimiVideos = videoMapper.selectCount(
                new QueryWrapper<Video>()
                        .eq("is_hachimi", true)
                        .eq("is_reviewed", true)
                        .between("reviewed_at", todayStart, todayEnd)
        );
        stats.put("todayHachimiVideos", todayHachimiVideos);

        // 额外统计：总视频数量
        Long totalVideos = videoMapper.selectCount(null);
        stats.put("totalVideos", totalVideos);

        // 额外统计：待审核视频数量
        Long pendingVideos = videoMapper.selectCount(
                new QueryWrapper<Video>()
                        .eq("is_reviewed", false)
                        .and(wrapper -> wrapper.isNull("review_status").or().ne("review_status", "IN_PROGRESS"))
        );
        stats.put("pendingVideos", pendingVideos);

        return stats;
    }

    /**
     * 统计不同的作者数量
     * 由于 MyBatis-Plus 的 selectCount 不支持 DISTINCT，我们需要自定义查询
     */
    private Long countDistinctAuthors() {
        // 使用自定义 SQL 查询
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(DISTINCT owner_mid) as count")
                .isNotNull("owner_mid");

        Map<String, Object> result = videoMapper.selectMaps(wrapper).get(0);
        return ((Number) result.get("count")).longValue();
    }
}