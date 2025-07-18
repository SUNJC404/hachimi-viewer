package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.dto.OwnerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Import Collectors

@Service
public class StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);

    @Autowired
    private VideoMapper videoMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Long totalHachimiVideos = videoMapper.selectCount(
                new QueryWrapper<Video>().eq("is_hachimi", true)
        );
        stats.put("totalHachimiVideos", totalHachimiVideos);

        Long totalAuthors = countDistinctAuthors();
        stats.put("totalAuthors", totalAuthors);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Long todayReviewedVideos = videoMapper.selectCount(
                new QueryWrapper<Video>()
                        .eq("is_reviewed", true)
                        .between("reviewed_at", todayStart, todayEnd)
        );
        stats.put("todayReviewedVideos", todayReviewedVideos);

        Long todayHachimiVideos = videoMapper.selectCount(
                new QueryWrapper<Video>()
                        .eq("is_hachimi", true)
                        .eq("is_reviewed", true)
                        .between("reviewed_at", todayStart, todayEnd)
        );
        stats.put("todayHachimiVideos", todayHachimiVideos);

        Long totalVideos = videoMapper.selectCount(null);
        stats.put("totalVideos", totalVideos);

        Long pendingVideos = videoMapper.selectCount(
                new QueryWrapper<Video>()
                        .eq("is_reviewed", false)
                        .and(wrapper -> wrapper.isNull("review_status").or().ne("review_status", "IN_PROGRESS"))
        );
        stats.put("pendingVideos", pendingVideos);

        return stats;
    }

    private Long countDistinctAuthors() {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(DISTINCT owner_mid) as count")
                .isNotNull("owner_mid")
                .eq("is_hachimi", true);

        Map<String, Object> result = videoMapper.selectMaps(wrapper).get(0);
        return ((Number) result.get("count")).longValue();
    }

    /**
     * Retrieves a list of distinct authors for Hachimi videos, mapping manually from raw database results.
     * @return List of OwnerDto containing author details.
     */
    @Transactional(readOnly = true)
    public List<OwnerDto> getDistinctHachimiAuthorsList() {
        try {
            // Call the raw mapper method that returns List<Map<String, Object>>
            List<Map<String, Object>> rawAuthors = videoMapper.findDistinctHachimiAuthorsRaw();

            // Manually map to OwnerDto, ensuring correct type casting
            List<OwnerDto> authors = rawAuthors.stream().map(rawAuthor -> {
                // Safely cast mid to Long
                Long mid = null;
                Object midObj = rawAuthor.get("mid");
                if (midObj instanceof Number) {
                    mid = ((Number) midObj).longValue();
                } else if (midObj != null) {
                    log.warn("Unexpected type for mid in raw author map: Expected Number, got {}", midObj.getClass().getName());
                }

                String name = (String) rawAuthor.get("name");
                String face = (String) rawAuthor.get("face"); // This should now work correctly as it's a String

                return new OwnerDto(name, face, mid);
            }).collect(Collectors.toList());

            log.info("Successfully retrieved {} distinct hachimi authors.", authors.size());
            return authors;
        } catch (Exception e) {
            log.error("Failed to retrieve distinct hachimi authors list from database. Check database connection, query, and data integrity.", e);
            throw new RuntimeException("Error fetching distinct hachimi authors", e);
        }
    }
}