package tv.oxnu0xuu.hachimiviewer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import tv.oxnu0xuu.hachimiviewer.model.PlaylistVideo;

import java.time.LocalDateTime;

@Data
public class PlaylistVideoDto {
    private Long id;
    private String bvid;
    private Integer position;
    private String title;
    private String coverUrl;
    private Integer views;
    private Double rating;
    private OwnerDto owner;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pubDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedAt;

    public static PlaylistVideoDto fromEntity(PlaylistVideo playlistVideo) {
        PlaylistVideoDto dto = new PlaylistVideoDto();
        dto.setId(playlistVideo.getId());
        dto.setBvid(playlistVideo.getBvid());
        dto.setPosition(playlistVideo.getPosition());
        dto.setAddedAt(playlistVideo.getAddedAt());

        if (playlistVideo.getVideo() != null) {
            dto.setTitle(playlistVideo.getVideo().getTitle());
            dto.setCoverUrl(playlistVideo.getVideo().getCoverUrl());
            dto.setViews(playlistVideo.getVideo().getViews());
            dto.setPubDate(playlistVideo.getVideo().getPubDate());
            dto.setRating(playlistVideo.getVideo().getRating());

            if (playlistVideo.getVideo().getOwner() != null) {
                dto.setOwner(new OwnerDto(
                        playlistVideo.getVideo().getOwner().getName(),
                        playlistVideo.getVideo().getOwner().getAvatarUrl(),
                        playlistVideo.getVideo().getOwner().getMid()
                ));
            }
        }

        return dto;
    }
}