
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

    private Long aid;

    private String title;

    private String description;

    private Integer duration;

    private LocalDateTime pubDate;

    private LocalDateTime createDate;

    private int views;

    private int danmaku;

    private int replies;

    private int favorites;

    private int coins;

    private int shares;

    private int likes;

    private Integer copyright;

    private String coverUrl;

    @TableField(exist = false)
    private User owner;

    @TableField(exist = false)
    private String user_name;

    @TableField(exist = false)
    private String user_avatar_url;

    private Long ownerMid;

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

    private boolean isReported;

    public User getOwner() {
        if (owner == null) owner = new User();
        return owner;
    }
}