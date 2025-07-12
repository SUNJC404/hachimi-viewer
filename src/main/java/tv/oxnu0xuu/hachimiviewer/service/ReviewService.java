package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.ReviewQueueResponseDto;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    @Qualifier("reviewBatchSize")
    private Integer batchSize;

    @Autowired
    @Qualifier("reviewLeaseMinutes")
    private Integer leaseMinutes;

    @Autowired
    private VideoMapper videoMapper;

    @Transactional
    public ReviewQueueResponseDto getReviewQueue() {

        // ======================== 【强力诊断日志】 ========================
        LocalDateTime serverNow = LocalDateTime.now();
        log.info("Step 1: Java App's Current Time (used for comparison): {}", serverNow);

        List<Video> inProgressVideos = videoMapper.selectList(
                new QueryWrapper<Video>().eq("review_status", "IN_PROGRESS")
        );        if (inProgressVideos.isEmpty()) {
            log.info("Step 2: No videos found with 'IN_PROGRESS' status. Skipping cleanup check.");
        } else {
            log.info("Step 2: Found {} videos with 'IN_PROGRESS' status. Checking each lease:", inProgressVideos.size());
            for (Video video : inProgressVideos) {
                if (video.getLeaseExpiresAt() == null) {
                    log.info("  - Video BVID: {}. Lease Time is NULL. Cannot be expired.", video.getBvid());
                    continue;
                }
                boolean isExpired = video.getLeaseExpiresAt().isBefore(serverNow);
                log.info("  - Video BVID: {}. Lease Expires At: [{}]. Is Expired? [{}].",
                        video.getBvid(), video.getLeaseExpiresAt(), isExpired);
            }
        }
        log.info("---------------------------------------------------------------------");

        // 3. 执行实际的清理操作
        log.info("Step 3: Executing the cleanup query with current time: {}", serverNow);
        int releasedCount = videoMapper.releaseExpiredLeases(serverNow);
        if (releasedCount > 0) {
            log.info(">>> SUCCESS: The query released {} videos with expired leases.", releasedCount);
        } else {
            log.warn(">>> INFO: The cleanup query ran, but released 0 videos.");
        }
        log.info("==================== DIAGNOSIS END ==================================");

        List<Video> availableVideos = videoMapper.findAvailableForReview();
        List<Video> videosToReview = availableVideos.stream().limit(batchSize).collect(Collectors.toList());

        if (videosToReview.isEmpty()) {
            return new ReviewQueueResponseDto(null, Collections.emptyList());
        }

        String currentReviewerId = UUID.randomUUID().toString();
        LocalDateTime leaseExpiresAt = LocalDateTime.now().plusMinutes(leaseMinutes);

        for (Video video : videosToReview) {
            video.setReviewStatus("IN_PROGRESS");
            video.setReviewerId(currentReviewerId);
            video.setLeaseExpiresAt(leaseExpiresAt);
            videoMapper.updateById(video);
        }

        List<VideoReviewDto> dtos = videosToReview.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());

        return new ReviewQueueResponseDto(currentReviewerId, dtos);
    }

    @Transactional
    public void extendLease(String reviewerId) {
        if (reviewerId == null || reviewerId.isEmpty()) {
            return;
        }
        List<Video> videos = videoMapper.selectList(
                new QueryWrapper<Video>()
                        .eq("reviewer_id", reviewerId)
                        .eq("review_status", "IN_PROGRESS")
        );
        LocalDateTime newLeaseTime = LocalDateTime.now().plusMinutes(leaseMinutes);

        for (Video video : videos) {
            video.setLeaseExpiresAt(newLeaseTime);
            videoMapper.updateById(video);
        }
    }

    @Transactional
    public void updateVideoStatus(String bvid, boolean isHachimi) {
        try {
            videoMapper.updateHachimiStatus(bvid, isHachimi, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to update video status for bvid: {}", bvid, e);
            throw e;
        }
    }
}