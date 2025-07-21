// src/main/java/tv/oxnu0xuu/hachimiviewer/mapper/PlaylistMapper.java
package tv.oxnu0xuu.hachimiviewer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import tv.oxnu0xuu.hachimiviewer.model.Playlist;
import tv.oxnu0xuu.hachimiviewer.model.PlaylistVideo;

import java.util.List;

public interface PlaylistMapper extends BaseMapper<Playlist> {

    @Update("UPDATE playlists SET view_count = view_count + 1 WHERE share_code = #{shareCode}")
    void incrementViewCount(@Param("shareCode") String shareCode);
}