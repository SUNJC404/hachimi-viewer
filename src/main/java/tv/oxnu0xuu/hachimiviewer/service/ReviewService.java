//package tv.oxnu0xuu.hachimiviewer.service;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import tv.oxnu0xuu.hachimiviewer.dto.ReviewQueueResponseDto;
//import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
//import tv.oxnu0xuu.hachimiviewer.model.Video;
//import tv.oxnu0xuu.hachimiviewer.repository.VideoRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//public class ReviewService {
//
//    private static final int BATCH_SIZE = 10;
//    private static final int LEASE_MINUTES = 5;
//    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
//
//    @Autowired
//    private VideoRepository videoRepository;
//
//    @Transactional
//    public ReviewQueueResponseDto getReviewQueue() {
//        int releasedCount = videoRepository.releaseExpiredLeases(LocalDateTime.now());
//        if (releasedCount > 0) {
//            log.info("Successfully released {} videos with expired leases.", releasedCount);
//        }
//
//        // 查找可供审核的视频
//        List<Video> availableVideos = videoRepository.findAvailableForReview(LocalDateTime.now());
//
//        // 取前 BATCH_SIZE 个
//        List<Video> videosToReview = availableVideos.stream().limit(BATCH_SIZE).collect(Collectors.toList());
//
//        if (videosToReview.isEmpty()) {
//            return new ReviewQueueResponseDto(null, Collections.emptyList());
//        }
//
//        String currentReviewerId = UUID.randomUUID().toString();
//        LocalDateTime leaseExpiresAt = LocalDateTime.now().plusMinutes(LEASE_MINUTES);
//
//        for (Video video : videosToReview) {
//            video.setReviewStatus("IN_PROGRESS");
//            video.setReviewerId(currentReviewerId);
//            video.setLeaseExpiresAt(leaseExpiresAt);
//        }
//
//        videoRepository.saveAll(videosToReview);
//
//        List<VideoReviewDto> dtos = videosToReview.stream()
//                .map(VideoReviewDto::fromEntity)
//                .collect(Collectors.toList());
//
//        return new ReviewQueueResponseDto(currentReviewerId, dtos);
//    }
//
//    @Transactional
//    public void extendLease(String reviewerId) {
//        if (reviewerId == null || reviewerId.isEmpty()) {
//            return;
//        }
//
//        List<Video> videos = videoRepository.findByReviewerIdAndReviewStatus(reviewerId, "IN_PROGRESS");
//        LocalDateTime newLeaseTime = LocalDateTime.now().plusMinutes(LEASE_MINUTES);
//
//        for (Video video : videos) {
//            video.setLeaseExpiresAt(newLeaseTime);
//        }
//
//        videoRepository.saveAll(videos);
//    }
//
//
//    @Transactional
//    public void updateVideoStatus(String bvid, boolean isHachimi) {
//        try {
//            videoRepository.updateHachimiStatus(bvid, isHachimi, LocalDateTime.now());
//        } catch (Exception e) {
//            // 这里应该有更详细的日志记录
//            throw e;
//        }
//    }
//}

package tv.oxnu0xuu.hachimiviewer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.ReviewQueueResponseDto;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.repository.VideoRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final int BATCH_SIZE = 10;
    private static final int LEASE_MINUTES = 5;
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private VideoRepository videoRepository;

    @Transactional
    public ReviewQueueResponseDto getReviewQueue() {

        // ======================== 【强力诊断日志】 ========================
        log.info("==================== DIAGNOSING LEASE EXPIRATION ====================");
        LocalDateTime serverNow = LocalDateTime.now();
        log.info("Step 1: Java App's Current Time (used for comparison): {}", serverNow);

        List<Video> inProgressVideos = videoRepository.findByReviewStatus("IN_PROGRESS");
        if (inProgressVideos.isEmpty()) {
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
        int releasedCount = videoRepository.releaseExpiredLeases(serverNow);
        if (releasedCount > 0) {
            log.info(">>> SUCCESS: The query released {} videos with expired leases.", releasedCount);
        } else {
            log.warn(">>> INFO: The cleanup query ran, but released 0 videos.");
        }
        log.info("==================== DIAGNOSIS END ==================================");
        // ======================== 【日志结束】 ========================


        // 4. 后续逻辑保持不变...
        List<Video> availableVideos = videoRepository.findAvailableForReview();

        List<Video> videosToReview = availableVideos.stream().limit(BATCH_SIZE).collect(Collectors.toList());

        if (videosToReview.isEmpty()) {
            return new ReviewQueueResponseDto(null, Collections.emptyList());
        }

        String currentReviewerId = UUID.randomUUID().toString();
        LocalDateTime leaseExpiresAt = LocalDateTime.now().plusMinutes(LEASE_MINUTES);

        for (Video video : videosToReview) {
            video.setReviewStatus("IN_PROGRESS");
            video.setReviewerId(currentReviewerId);
            video.setLeaseExpiresAt(leaseExpiresAt);
        }

        videoRepository.saveAll(videosToReview);

        List<VideoReviewDto> dtos = videosToReview.stream()
                .map(VideoReviewDto::fromEntity)
                .collect(Collectors.toList());

        return new ReviewQueueResponseDto(currentReviewerId, dtos);
    }

    // ... 其他方法 (extendLease, updateVideoStatus) 保持不变 ...
    @Transactional
    public void extendLease(String reviewerId) {
        if (reviewerId == null || reviewerId.isEmpty()) {
            return;
        }

        List<Video> videos = videoRepository.findByReviewerIdAndReviewStatus(reviewerId, "IN_PROGRESS");
        LocalDateTime newLeaseTime = LocalDateTime.now().plusMinutes(LEASE_MINUTES);

        for (Video video : videos) {
            video.setLeaseExpiresAt(newLeaseTime);
        }

        videoRepository.saveAll(videos);
    }

    @Transactional
    public void updateVideoStatus(String bvid, boolean isHachimi) {
        try {
            videoRepository.updateHachimiStatus(bvid, isHachimi, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to update video status for bvid: {}", bvid, e);
            throw e;
        }
    }
}