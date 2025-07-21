// src/main/java/tv/oxnu0xuu/hachimiviewer/dto/AddVideoToPlaylistDto.java
package tv.oxnu0xuu.hachimiviewer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddVideoToPlaylistDto {
    @NotBlank(message = "编辑码不能为空")
    private String editCode;

    @NotBlank(message = "视频BV号不能为空")
    private String bvid;
}