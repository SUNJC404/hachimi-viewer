package tv.oxnu0xuu.hachimiviewer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tv.oxnu0xuu.hachimiviewer.dto.VideoDetailDto;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminSearchService {

    private static final Logger log = LoggerFactory.getLogger(AdminSearchService.class);

    @Autowired
    private Client meiliSearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Value("${meili.index.videos}")
    private String videoIndexName;

    private Index adminVideosIndex;

    @PostConstruct
    public void init() {
        try {
            adminVideosIndex = meiliSearchClient.index(videoIndexName);

            Settings settings = new Settings();
            // Change ownerName to owner.name for searchable attributes
            settings.setSearchableAttributes(new String[]{"bvid", "title", "description", "owner.name"});
            settings.setFilterableAttributes(new String[]{"is_hachimi", "is_available", "pubDate"});
            settings.setSortableAttributes(new String[]{"pubDate", "views", "updatedAt"});
            // Change ownerName and ownerMid to owner.name and owner.mid for displayed attributes
            settings.setDisplayedAttributes(new String[]{
                    "bvid", "title", "description", "owner.name", "owner.mid",
                    "pubDate", "views", "danmaku", "replies", "favorites",
                    "coins", "shares", "likes", "is_hachimi", "is_available",
                    "coverUrl", "categoryId", "reviewedAt", "updatedAt"
            });

            adminVideosIndex.updateSettings(settings);
            log.info("Admin search index configured successfully");
        } catch (Exception e) {
            log.error("Failed to initialize admin search index", e);
        }
    }

    public Map<String, Object> searchVideos(String query, int page, int size, Boolean isHachimi) {
        try {
            SearchRequest searchRequest = new SearchRequest(query != null ? query : "");

            StringBuilder filter = new StringBuilder();
            if (isHachimi != null) {
                filter.append("is_hachimi = ").append(isHachimi);
            }

            if (filter.length() > 0) {
                searchRequest.setFilter(new String[]{filter.toString()});
            }

            searchRequest.setPage(page + 1);
            searchRequest.setHitsPerPage(size);
            searchRequest.setSort(new String[]{"pubDate:desc"});

            Object searchResult = adminVideosIndex.search(searchRequest);

            Map<String, Object> resultMap = objectMapper.convertValue(searchResult, Map.class);
            List<Map<String, Object>> hits = (List<Map<String, Object>>) resultMap.get("hits");

            List<VideoDetailDto> videos = hits.stream()
                    .map(hit -> {
                        VideoDetailDto dto = new VideoDetailDto();
                        dto.setBvid((String) hit.get("bvid"));
                        dto.setTitle((String) hit.get("title"));
                        dto.setDescription((String) hit.get("description"));

                        // Correctly retrieve ownerName and ownerMid from the nested 'owner' object
                        Map<String, Object> ownerMap = (Map<String, Object>) hit.get("owner");
                        if (ownerMap != null) {
                            dto.setOwnerName((String) ownerMap.get("name"));
                            dto.setOwnerMid(ownerMap.get("mid") != null ? ((Number) ownerMap.get("mid")).longValue() : null);
                        }

                        dto.setViews(((Number) hit.get("views")).longValue());
                        dto.setDanmaku(hit.get("danmaku") != null ? ((Number) hit.get("danmaku")).longValue() : 0);
                        dto.setReplies(hit.get("replies") != null ? ((Number) hit.get("replies")).longValue() : 0);
                        dto.setFavorites(hit.get("favorites") != null ? ((Number) hit.get("favorites")).longValue() : 0);
                        dto.setCoins(hit.get("coins") != null ? ((Number) hit.get("coins")).longValue() : 0);
                        dto.setShares(hit.get("shares") != null ? ((Number) hit.get("shares")).longValue() : 0);
                        dto.setLikes(hit.get("likes") != null ? ((Number) hit.get("likes")).longValue() : 0);
                        dto.setHachimi((Boolean) hit.get("is_hachimi"));
                        dto.setAvailable(hit.get("is_available") != null ? (Boolean) hit.get("is_available") : true);
                        dto.setCoverUrl((String) hit.get("coverUrl"));
                        dto.setCategoryId(hit.get("categoryId") != null ? ((Number) hit.get("categoryId")).intValue() : null);

                        String pubDateStr = (String) hit.get("pubDate");
                        if (pubDateStr != null) {
                            dto.setPubDate(java.time.LocalDateTime.parse(pubDateStr));
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", videos);
            response.put("totalElements", resultMap.get("totalHits"));
            response.put("totalPages", resultMap.get("totalPages"));
            response.put("currentPage", page);
            response.put("pageSize", size);

            return response;
        } catch (Exception e) {
            log.error("Search failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("content", List.of());
            response.put("totalElements", 0);
            response.put("totalPages", 0);
            response.put("currentPage", page);
            response.put("pageSize", size);
            return response;
        }
    }
}