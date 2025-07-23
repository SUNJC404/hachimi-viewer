package tv.oxnu0xuu.hachimiviewer.dto;

import com.fasterxml.jackson.annotation.JsonFormat; // Add this import
import com.fasterxml.jackson.annotation.JsonProperty;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime; // Add this import

@Getter
@Setter
public class VideoReviewDto {
    private String bvid;
    private String title;
    private Integer duration;

    @JsonProperty("is_hachimi")
    private boolean isHachimi;

    private OwnerDto owner;
    private long views;
    private String coverUrl;
    private String description;
    private Integer categoryId;

    @JsonProperty("is_available")
    private boolean isAvailable;

    @JsonProperty("is_reported")
    private boolean isReported;

    // Add pubDate field with JSON format annotation for consistency with VideoDetailDto
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pubDate;

    public static VideoReviewDto fromEntity(Video video) {
        VideoReviewDto dto = new VideoReviewDto();
        dto.setAvailable(video.isAvailable());
        dto.setBvid(video.getBvid());
        dto.setTitle(video.getTitle());
        dto.setHachimi(video.isHachimi());
        dto.setViews(video.getViews());
        dto.setCoverUrl(video.getCoverUrl());
        dto.setDescription(video.getDescription());
        dto.setCategoryId(video.getCategoryId());
        dto.setReported(video.isReported());
        // Populate the new pubDate field from the Video entity
        dto.setPubDate(video.getPubDate());
        if (video.getOwner() != null) {
            // Ensure owner's mid is also passed, as per previous fixes
            dto.setOwner(new OwnerDto(video.getOwner().getName(), video.getOwner().getAvatarUrl(), video.getOwner().getMid()));
        }
        return dto;
    }
}