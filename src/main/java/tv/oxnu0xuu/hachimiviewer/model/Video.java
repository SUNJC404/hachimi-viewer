package tv.oxnu0xuu.hachimiviewer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
@Getter
@Setter
public class Video {

    @Id
    @Column(length = 15)
    private String bvid;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "pub_date")
    private LocalDateTime pubDate;

    private int views;

    @Column(name = "cover_url")
    private String coverUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_mid", referencedColumnName = "mid")
    private User owner;

    @Column(name = "is_hachimi", nullable = false)
    private boolean isHachimi;

    @Column(name = "is_reviewed", nullable = false)
    private boolean isReviewed;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "review_status")
    private String reviewStatus; // 例如: "IN_PROGRESS", "COMPLETED"

    @Column(name = "reviewer_id")
    private String reviewerId;

    @Column(name = "lease_expires_at")
    private LocalDateTime leaseExpiresAt;

    @Column(name = "original_song")
    private String originalSong;

    @Column(name = "original_artist")
    private String originalArtist;

    @Column(name = "is_available", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isAvailable;
}