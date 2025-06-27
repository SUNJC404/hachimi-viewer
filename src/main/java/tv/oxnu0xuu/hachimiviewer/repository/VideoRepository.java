package tv.oxnu0xuu.hachimiviewer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

    @Query("SELECT v FROM Video v JOIN FETCH v.owner")
    List<Video> findAllWithOwners();

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

    // 获取最新发现
    @Query("SELECT v FROM Video v WHERE v.isHachimi = true ORDER BY v.pubDate DESC")
    List<Video> findHachimiVideosOrderByPubDateDesc(Pageable pageable);

    // 获取随机推荐
    @Query(value = "SELECT * FROM videos WHERE is_hachimi = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Video> findRandomHachimiVideos(@Param("limit") int limit);
}