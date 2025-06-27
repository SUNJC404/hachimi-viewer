package tv.oxnu0xuu.hachimiviewer.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

    // --- 修改查询逻辑 ---
    // 查找未审核、且（状态为空 或 租约已过期）的视频
    @Query("SELECT v FROM Video v WHERE v.isReviewed = false AND v.reviewStatus IS NULL ORDER BY v.pubDate DESC")
    List<Video> findAvailableForReview();

    List<Video> findByReviewerIdAndReviewStatus(String reviewerId, String reviewStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Video v SET v.isHachimi = :isHachimi, v.isReviewed = true, v.reviewedAt = :reviewedAt, v.reviewStatus = 'COMPLETED', v.leaseExpiresAt = null WHERE v.bvid = :bvid")
    void updateHachimiStatus(@Param("bvid") String bvid, @Param("isHachimi") boolean isHachimi, @Param("reviewedAt") LocalDateTime reviewedAt);

    @Modifying
    @Transactional
    @Query("UPDATE Video v SET v.reviewStatus = null, v.reviewerId = null, v.leaseExpiresAt = null WHERE v.reviewStatus = 'IN_PROGRESS' AND v.leaseExpiresAt < :now")
    int releaseExpiredLeases(@Param("now") LocalDateTime now);

    List<Video> findByReviewStatus(String status);
}