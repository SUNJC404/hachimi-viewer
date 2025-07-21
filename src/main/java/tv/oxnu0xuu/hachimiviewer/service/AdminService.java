package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper
import com.meilisearch.sdk.Client; // Import MeiliSearch Client
import com.meilisearch.sdk.Index; // Import MeiliSearch Index
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value for videoIndexName
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.VideoDetailDto;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto; // Import VideoReviewDto, as it's used for indexing
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private AdminSearchService adminSearchService;

    // Inject MeiliSearch Client, ObjectMapper, and videoIndexName
    @Autowired
    private Client meiliSearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${meili.index.videos}")
    private String videoIndexName;

    @Transactional(readOnly = true)
    public Map<String, Object> getVideos(int page, int size, String search, Boolean isHachimi, String sort) {
        // This method fetches data from MeiliSearch
        return adminSearchService.searchVideos(search, page, size, isHachimi, sort);
    }

    @Transactional
    public void toggleHachimiStatus(String bvid) {
        Video video = videoMapper.selectById(bvid);
        if (video != null) {
            boolean newStatus = !video.isHachimi(); // Invert the current hachimi status
            video.setHachimi(newStatus);
            video.setReviewed(true); // Mark as reviewed
            video.setReviewedAt(LocalDateTime.now()); // Set review timestamp
            video.setUpdatedAt(LocalDateTime.now()); // Update timestamp for general tracking
            videoMapper.updateById(video); // Update the video in the primary database

            // --- IMMEDIATE MEILISEARCH UPDATE START ---
            // After updating the database, immediately update MeiliSearch
            try {
                Index index = meiliSearchClient.index(videoIndexName);
                // Convert the updated Video entity to VideoReviewDto, which is the structure MeiliSearch expects for indexing
                VideoReviewDto dtoToUpdate = VideoReviewDto.fromEntity(video);
                String document = objectMapper.writeValueAsString(dtoToUpdate);
                // Use addDocuments; if a document with this bvid (primary key) already exists, it will be updated.
                index.addDocuments(document, "bvid");
                System.out.println("MeiliSearch: Video " + bvid + " updated immediately with hachimi status " + newStatus);
            } catch (Exception e) {
                // Log the error but do not re-throw, as the primary database update was successful.
                System.err.println("Error updating MeiliSearch for video " + bvid + ": " + e.getMessage());
            }
            // --- IMMEDIATE MEILISEARCH UPDATE END ---
        }
    }

    @Transactional(readOnly = true)
    public VideoDetailDto getVideoDetail(String bvid) {
        Video video = getVideoWithOwner(bvid);
        return video != null ? VideoDetailDto.fromEntity(video) : null;
    }

    private Video getVideoWithOwner(String bvid) {
        // This helper method is for retrieving video details, not directly related to the toggle issue.
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("bvid", bvid);
        Video video = videoMapper.selectOne(wrapper);

        if (video != null) {
            return video;
        }
        return null;
    }
}