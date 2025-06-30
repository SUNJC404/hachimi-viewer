package tv.oxnu0xuu.hachimiviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    private Long mid;

    @Column(nullable = false)
    private String name;

    @Column(name = "avatar_url", length = 512) // <-- UPDATE THIS ANNOTATION
    private String avatarUrl;
}