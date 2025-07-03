package tv.oxnu0xuu.hachimiviewer.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName("users")
@Getter
@Setter
public class User {

    @TableId
    private Long mid;

    private String name;

    private String avatarUrl;
}