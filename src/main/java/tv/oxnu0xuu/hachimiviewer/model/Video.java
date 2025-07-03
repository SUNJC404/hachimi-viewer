package tv.oxnu0xuu.hachimiviewer.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("videos")
public class Video {
    @TableId
    private String bvid;

    private String title;

    private String description;

    private LocalDateTime pubDate;

    private int views;

    private String coverUrl;

    @TableField(exist = false)
    private User owner;

    private boolean isHachimi;

    private boolean isReviewed;

    private LocalDateTime reviewedAt;

    private Integer categoryId;

    private String reviewStatus;

    private String reviewerId;

    private LocalDateTime leaseExpiresAt;

    private String originalSong;

    private String originalArtist;

    private boolean isAvailable;

    private LocalDateTime updatedAt;

}