package tv.oxnu0xuu.hachimiviewer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.dto.OwnerDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // Import Map

/**
 * Video 数据访问层
 * 继承 BaseMapper 以获得基础的 CRUD 功能
 * 使用注解方式定义所有自定义 SQL
 */
public interface VideoMapper extends BaseMapper<Video> {

    // --- 为了代码清晰和复用，定义 SQL 片段 ---
    // 【最终修正】: 包含了您指出的 aid, duration, danmaku 等所有在表结构中存在的列
    String VIDEO_WITH_OWNER_COLUMNS =
            "v.bvid, v.aid, v.title, v.description, v.duration, v.pub_date, v.create_date, " +
                    "v.cover_url, v.views, v.danmaku, v.replies, v.favorites, v.coins, v.shares, " +
                    "v.likes, v.copyright, v.is_hachimi, v.owner_mid, v.category_id, v.is_reviewed, " +
                    "v.reviewed_at, v.lease_expires_at, v.original_song, v.original_artist, " +
                    "v.review_status, v.reviewer_id, v.updated_at, v.is_available, " +
                    "u.name as user_name, u.avatar_url as user_avatar_url";

    String VIDEO_OWNER_JOIN = "FROM videos v LEFT JOIN users u ON v.owner_mid = u.mid";


    /**
     * 查找可供审核的视频 (is_reviewed = false AND review_status IS NULL)
     * 这个方法同时【定义】了可复用的 ResultMap，供其他查询使用
     *
     * @return 待审核的视频列表，包含作者信息
     */
    @Select("SELECT " + VIDEO_WITH_OWNER_COLUMNS + " " + VIDEO_OWNER_JOIN + " WHERE v.is_reviewed = false AND v.review_status IS NULL ORDER BY v.pub_date DESC")
    @Results(id = "videoWithOwnerResultMap", value = {
            @Result(property = "bvid", column = "bvid", id = true),
            // 将关联查询出的列，映射到嵌套的 'owner' User 对象上
            @Result(property = "owner.mid", column = "owner_mid"),
            @Result(property = "owner.name", column = "user_name"),
            @Result(property = "owner.avatarUrl", column = "user_avatar_url")
    })
    List<Video> findAvailableForReview();

    /**
     * 释放超时的审核租约
     *
     * @param now 当前时间，用于比较租约是否过期
     * @return 受影响的行数
     */
    @Update("UPDATE videos SET review_status = null, reviewer_id = null, lease_expires_at = null WHERE review_status = 'IN_PROGRESS' AND lease_expires_at < #{now}")
    int releaseExpiredLeases(@Param("now") LocalDateTime now);

    /**
     * 更新视频的 Hachimi 状态和审核信息
     *
     * @param bvid       视频 BVID
     * @param isHachimi  是否为 Hachimi
     * @param reviewedAt 审核时间
     */
    @Update("UPDATE videos SET is_hachimi = #{isHachimi}, is_reviewed = true, reviewed_at = #{reviewedAt}, review_status = 'COMPLETED', lease_expires_at = null, updated_at = NOW() WHERE bvid = #{bvid}")
    void updateHachimiStatus(@Param("bvid") String bvid, @Param("isHachimi") boolean isHachimi, @Param("reviewedAt") LocalDateTime reviewedAt);

    /**
     * 分页查询最新的 Hachimi 视频
     *
     * @param offset 分页偏移量
     * @param size   每页数量
     * @return Hachimi 视频列表，包含作者信息
     */
    @Select("SELECT " + VIDEO_WITH_OWNER_COLUMNS + " " + VIDEO_OWNER_JOIN + " WHERE v.is_hachimi = true ORDER BY v.pub_date DESC LIMIT #{offset}, #{size}")
    @ResultMap("videoWithOwnerResultMap")
    List<Video> findHachimiVideosOrderByPubDateDesc(@Param("offset") int offset, @Param("size") int size);

    /**
     * 查询指定数量的随机 Hachimi 视频
     *
     * @param limit 数量限制
     * @return Hachimi 视频列表，包含作者信息
     */
    @Select("SELECT " + VIDEO_WITH_OWNER_COLUMNS + " " + VIDEO_OWNER_JOIN + " WHERE is_hachimi = true ORDER BY RAND() LIMIT #{limit}")
    @ResultMap("videoWithOwnerResultMap")
    List<Video> findRandomHachimiVideos(@Param("limit") int limit);

    /**
     * 分页查询所有 Hachimi 视频，用于数据同步
     *
     * @param offset 分页偏移量
     * @param size   每页数量
     * @return Hachimi 视频列表，包含作者信息
     */
    @Select("SELECT " + VIDEO_WITH_OWNER_COLUMNS + " " + VIDEO_OWNER_JOIN + " WHERE v.is_hachimi = true ORDER BY v.pub_date DESC LIMIT #{offset}, #{size}")
    @ResultMap("videoWithOwnerResultMap")
    List<Video> findAllHachimiWithOwners(@Param("offset") int offset, @Param("size") int size);

    /**
     * 查询指定时间点之后更新过的 Hachimi 视频，用于增量同步
     *
     * @param since 时间点
     * @return 更新过的 Hachimi 视频列表，包含作者信息
     */
    @Select("SELECT " + VIDEO_WITH_OWNER_COLUMNS + " " + VIDEO_OWNER_JOIN + " WHERE v.is_hachimi = true AND v.updated_at > #{since}")
    @ResultMap("videoWithOwnerResultMap")
    List<Video> findHachimiVideosUpdatedSince(@Param("since") LocalDateTime since);

    /**
     * 查找所有哈基米视频的唯一作者，返回原始Map列表以便手动映射。
     * (Removed @Results and changed return type)
     *
     * @return 唯一作者的原始数据列表
     */
    @Select("SELECT DISTINCT v.owner_mid AS mid, u.name AS name, u.avatar_url AS face " +
            "FROM videos v " +
            "JOIN users u ON v.owner_mid = u.mid " +
            "WHERE v.is_hachimi = true AND v.owner_mid IS NOT NULL")
    List<Map<String, Object>> findDistinctHachimiAuthorsRaw(); // Changed method name and return type
}