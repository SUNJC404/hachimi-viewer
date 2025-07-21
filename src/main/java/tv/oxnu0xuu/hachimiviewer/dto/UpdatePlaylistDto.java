// src/main/java/tv/oxnu0xuu/hachimiviewer/dto/UpdatePlaylistDto.java
package tv.oxnu0xuu.hachimiviewer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePlaylistDto {
    @NotBlank(message = "编辑码不能为空")
    private String editCode;

    @Size(max = 100, message = "标题最多100个字符")
    private String title;

    @Size(max = 500, message = "描述最多500个字符")
    private String description;
}