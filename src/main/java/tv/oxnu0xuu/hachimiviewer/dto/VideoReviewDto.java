package tv.oxnu0xuu.hachimiviewer.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // 导入注解
import tv.oxnu0xuu.hachimiviewer.model.Video;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoReviewDto {
    private String bvid;
    private String title;

    // --- 修改点 1: 添加 @JsonProperty 注解 ---
    @JsonProperty("is_hachimi")
    private boolean isHachimi;

    private OwnerDto owner;
    private long views;
    private String coverUrl;
    private String description;
    private Integer categoryId;

    // --- 修改点 2: 为 isAvailable 也添加注解，保持一致性 ---
    @JsonProperty("is_available")
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
            // 注意这里保持 'face' 字段以兼容前端
            dto.setOwner(new OwnerDto(video.getOwner().getName(), video.getOwner().getAvatarUrl()));
        }
        return dto;
    }
}