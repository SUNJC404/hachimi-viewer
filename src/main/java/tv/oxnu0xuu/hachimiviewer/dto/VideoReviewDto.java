package tv.oxnu0xuu.hachimiviewer.dto;

import tv.oxnu0xuu.hachimiviewer.model.Video;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoReviewDto {
    private String bvid;
    private String title;
    private boolean isHachimi;
    private OwnerDto owner;
    private long views;
    private String coverUrl;
    private String description;
    private Integer categoryId;
    private boolean isAvailable;

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
        if (video.getOwner() != null) {
            dto.setOwner(new OwnerDto(video.getOwner().getName(), video.getOwner().getAvatarUrl()));
        }
        return dto;
    }
}