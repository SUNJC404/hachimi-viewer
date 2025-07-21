// src/main/java/tv/oxnu0xuu/hachimiviewer/model/Playlist.java
package tv.oxnu0xuu.hachimiviewer.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@TableName("playlists")
@Getter
@Setter
public class Playlist {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String shareCode;
    private String editCode;
    private String title;
    private String description;
    private String creatorIp;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}