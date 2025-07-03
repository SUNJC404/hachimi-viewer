package tv.oxnu0xuu.hachimiviewer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Video 数据访问层
 * 继承 BaseMapper 以获得基础的 CRUD 功能
 */
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 查找可供审核的视频 (is_reviewed = false AND review_status IS NULL)
     *
     * @return 待审核的视频列表，包含作者信息
     */
    List<Video> findAvailableForReview();

    /**
     * 释放超时的审核租约
     *
     * @param now 当前时间，用于比较租约是否过期
     * @return 受影响的行数
     */
    int releaseExpiredLeases(@Param("now") LocalDateTime now);

    /**
     * 更新视频的 Hachimi 状态和审核信息
     *
     * @param bvid       视频 BVID
     * @param isHachimi  是否为 Hachimi
     * @param reviewedAt 审核时间
     */
    void updateHachimiStatus(@Param("bvid") String bvid, @Param("isHachimi") boolean isHachimi, @Param("reviewedAt") LocalDateTime reviewedAt);

    /**
     * 分页查询最新的 Hachimi 视频
     *
     * @param offset 分页偏移量
     * @param size   每页数量
     * @return Hachimi 视频列表，包含作者信息
     */
    List<Video> findHachimiVideosOrderByPubDateDesc(@Param("offset") int offset, @Param("size") int size);

    /**
     * 查询指定数量的随机 Hachimi 视频
     *
     * @param limit 数量限制
     * @return Hachimi 视频列表，包含作者信息
     */
    List<Video> findRandomHachimiVideos(@Param("limit") int limit);

    /**
     * 分页查询所有 Hachimi 视频，用于数据同步
     *
     * @param offset 分页偏移量
     * @param size   每页数量
     * @return Hachimi 视频列表，包含作者信息
     */
    List<Video> findAllHachimiWithOwners(@Param("offset") int offset, @Param("size") int size);

    /**
     * 查询指定时间点之后更新过的 Hachimi 视频，用于增量同步
     *
     * @param since 时间点
     * @return 更新过的 Hachimi 视频列表，包含作者信息
     */
    List<Video> findHachimiVideosUpdatedSince(@Param("since") LocalDateTime since);
}