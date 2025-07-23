package tv.oxnu0xuu.hachimiviewer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoDetailDto {
    private String bvid;
    private String title;
    private Integer duration;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pubDate;

    private long views;
    private long danmaku;
    private long replies;
    private long favorites;
    private long coins;
    private long shares;
    private long likes;

    @JsonProperty("is_hachimi")
    private boolean isHachimi;

    @JsonProperty("is_available")
    private boolean isAvailable;

    @JsonProperty("is_reported")
    private boolean isReported;

    private String ownerName;
    private Long ownerMid;

    private String coverUrl;
    private Integer categoryId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static VideoDetailDto fromEntity(Video video) {
        VideoDetailDto dto = new VideoDetailDto();
        dto.setBvid(video.getBvid());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setPubDate(video.getPubDate());
        dto.setViews(video.getViews());
        dto.setDanmaku(video.getDanmaku());
        dto.setReplies(video.getReplies());
        dto.setFavorites(video.getFavorites());
        dto.setCoins(video.getCoins());
        dto.setShares(video.getShares());
        dto.setLikes(video.getLikes());
        dto.setHachimi(video.isHachimi());
        dto.setAvailable(video.isAvailable());
        dto.setCoverUrl(video.getCoverUrl());
        dto.setCategoryId(video.getCategoryId());
        dto.setReviewedAt(video.getReviewedAt());
        dto.setUpdatedAt(video.getUpdatedAt());

        if (video.getOwner() != null) {
            dto.setOwnerName(video.getOwner().getName());
            dto.setOwnerMid(video.getOwner().getMid());
        }

        return dto;
    }
}