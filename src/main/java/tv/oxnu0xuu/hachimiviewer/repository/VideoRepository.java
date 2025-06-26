package tv.oxnu0xuu.hachimiviewer.repository;

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

    List<Video> findTop50ByIsReviewedFalseOrderByPubDateDesc();

    /**
     * 添加这个新的方法.
     * @Modifying 表示这是一个修改数据库的操作 (UPDATE, DELETE, INSERT).
     * @Query 定义了具体的 JPQL (Java Persistence Query Language) 语句.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Video v SET v.isHachimi = :isHachimi, v.isReviewed = true, v.reviewedAt = :reviewedAt WHERE v.bvid = :bvid")
    void updateHachimiStatus(@Param("bvid") String bvid, @Param("isHachimi") boolean isHachimi, @Param("reviewedAt") LocalDateTime reviewedAt);
}