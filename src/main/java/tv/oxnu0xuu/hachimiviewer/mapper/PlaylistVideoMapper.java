// src/main/java/tv/oxnu0xuu/hachimiviewer/mapper/PlaylistVideoMapper.java
package tv.oxnu0xuu.hachimiviewer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import tv.oxnu0xuu.hachimiviewer.model.PlaylistVideo;

import java.util.List;

public interface PlaylistVideoMapper extends BaseMapper<PlaylistVideo> {

    String VIDEO_WITH_OWNER_COLUMNS =
            "pv.id, pv.playlist_id, pv.bvid, pv.position, pv.added_at, " +
                    "v.title, v.cover_url, v.views, v.pub_date, v.owner_mid, v.rating, " +
                    "u.name as user_name, u.avatar_url as user_avatar_url";

    @Select("SELECT " + VIDEO_WITH_OWNER_COLUMNS + " " +
            "FROM playlist_videos pv " +
            "LEFT JOIN videos v ON pv.bvid = v.bvid " +
            "LEFT JOIN users u ON v.owner_mid = u.mid " +
            "WHERE pv.playlist_id = #{playlistId} " +
            "ORDER BY pv.position ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "playlistId", column = "playlist_id"),
            @Result(property = "bvid", column = "bvid"),
            @Result(property = "position", column = "position"),
            @Result(property = "addedAt", column = "added_at"),
            // 关联的 Video 对象的映射
            @Result(property = "video.bvid", column = "bvid"),
            @Result(property = "video.title", column = "title"),
            @Result(property = "video.coverUrl", column = "cover_url"),
            @Result(property = "video.views", column = "views"),
            @Result(property = "video.pubDate", column = "pub_date"),
            @Result(property = "video.rating", column = "rating"), // **修复：为 rating 字段添加映射**
            // 关联的 Owner (User) 对象的映射
            @Result(property = "video.owner.mid", column = "owner_mid"),
            @Result(property = "video.owner.name", column = "user_name"),
            @Result(property = "video.owner.avatarUrl", column = "user_avatar_url")
    })
    List<PlaylistVideo> findByPlaylistIdWithVideos(@Param("playlistId") Long playlistId);

    @Select("SELECT MAX(position) FROM playlist_videos WHERE playlist_id = #{playlistId}")
    Integer findMaxPosition(@Param("playlistId") Long playlistId);

    @Update("UPDATE playlist_videos SET position = position - 1 WHERE playlist_id = #{playlistId} AND position > #{position}")
    void decrementPositionsAfter(@Param("playlistId") Long playlistId, @Param("position") Integer position);

    @Update("UPDATE playlist_videos SET position = #{newPosition} WHERE id = #{id}")
    void updatePosition(@Param("id") Long id, @Param("newPosition") Integer newPosition);

    @Delete("DELETE FROM playlist_videos WHERE playlist_id = #{playlistId}")
    void deleteByPlaylistId(@Param("playlistId") Long playlistId);
}