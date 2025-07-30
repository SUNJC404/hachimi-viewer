// src/main/java/tv/oxnu0xuu/hachimiviewer/model/PlaylistVideo.java
package tv.oxnu0xuu.hachimiviewer.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("playlist_videos")
public class PlaylistVideo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long playlistId;
    private String bvid;
    private Integer position;
    private LocalDateTime addedAt;

    @TableField(exist = false)
    private Video video;
}