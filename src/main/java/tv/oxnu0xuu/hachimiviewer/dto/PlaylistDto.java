// src/main/java/tv/oxnu0xuu/hachimiviewer/dto/PlaylistDto.java
package tv.oxnu0xuu.hachimiviewer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import tv.oxnu0xuu.hachimiviewer.model.Playlist;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlaylistDto {
    private Long id;
    private String shareCode;
    private String title;
    private String description;
    private Integer viewCount;
    private Integer videoCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<PlaylistVideoDto> videos;

    public static PlaylistDto fromEntity(Playlist playlist) {
        PlaylistDto dto = new PlaylistDto();
        dto.setId(playlist.getId());
        dto.setShareCode(playlist.getShareCode());
        dto.setTitle(playlist.getTitle());
        dto.setDescription(playlist.getDescription());
        dto.setViewCount(playlist.getViewCount());
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setUpdatedAt(playlist.getUpdatedAt());
        return dto;
    }
}