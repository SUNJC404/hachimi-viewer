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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // <-- Add this import
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminSearchService {

    private static final Logger log = LoggerFactory.getLogger(AdminSearchService.class);

    // Define the DateTimeFormatter to match your expected string format
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // <-- Add this line

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
            log.info("AdminSearchService initialized and connected to index '{}'.", videoIndexName);
        } catch (Exception e) {
            log.error("Failed to initialize admin search index", e);
        }
    }

    public Map<String, Object> searchVideos(String query, int page, int size, Boolean isHachimi, Boolean isReported, String sort) {
        try {
            SearchRequest searchRequest = new SearchRequest(query != null ? query : "");

            StringBuilder filter = new StringBuilder();
            if (isHachimi != null) {
                filter.append("is_hachimi = ").append(isHachimi);
            }

            if (isReported != null && isReported) {
                if (filter.length() > 0) {
                    filter.append(" AND ");
                }
                filter.append("isReported = true");
            }

            if (filter.length() > 0) {
                searchRequest.setFilter(new String[]{filter.toString()});
            }

            searchRequest.setPage(page + 1);
            searchRequest.setHitsPerPage(size);
            searchRequest.setSort(new String[]{sort}); // 使用传入的 sort 参数

            Object searchResult = adminVideosIndex.search(searchRequest);

            Map<String, Object> resultMap = objectMapper.convertValue(searchResult, Map.class);
            List<Map<String, Object>> hits = (List<Map<String, Object>>) resultMap.get("hits");

            List<VideoDetailDto> videos = hits.stream()
                    .map(hit -> {
                        VideoDetailDto dto = new VideoDetailDto();
                        dto.setBvid((String) hit.get("bvid"));
                        dto.setTitle((String) hit.get("title"));
                        dto.setDescription((String) hit.get("description"));

                        Map<String, Object> ownerMap = (Map<String, Object>) hit.get("owner");
                        if (ownerMap != null) {
                            dto.setOwnerName((String) ownerMap.get("name"));

                            Object ownerMidObj = ownerMap.get("mid");
                            if (ownerMidObj instanceof Number) {
                                dto.setOwnerMid(((Number) ownerMidObj).longValue());
                            } else if (ownerMidObj != null) {
                                log.warn("Unexpected type for owner.mid for bvid {}: Expected Number, got {}", hit.get("bvid"), ownerMidObj.getClass().getName());
                            }
                        }

                        // Robust parsing for pubDate
                        Object pubDateObj = hit.get("pubDate");
                        if (pubDateObj instanceof String) {
                            try {
                                // Use the defined formatter for parsing
                                dto.setPubDate(LocalDateTime.parse((String) pubDateObj, DATE_TIME_FORMATTER)); // <-- Change here
                            } catch (DateTimeParseException e) {
                                log.warn("Failed to parse pubDate string '{}' for bvid {}: {}", pubDateObj, hit.get("bvid"), e.getMessage());
                                // Leave pubDate as null or set a default
                            }
                        } else if (pubDateObj != null) {
                            log.warn("Unexpected type for pubDate for bvid {}: Expected String, got {}", hit.get("bvid"), pubDateObj.getClass().getName());
                        }

                        // Robust parsing for numeric fields
                        Object viewsObj = hit.get("views");
                        if (viewsObj instanceof Number) {
                            dto.setViews(((Number) viewsObj).longValue());
                        } else {
                            dto.setViews(0);
                            if (viewsObj != null) log.warn("Unexpected type for views for bvid {}: Expected Number, got {}", hit.get("bvid"), viewsObj.getClass().getName());
                        }

                        Object danmakuObj = hit.get("danmaku");
                        if (danmakuObj instanceof Number) {
                            dto.setDanmaku(((Number) danmakuObj).longValue());
                        } else {
                            dto.setDanmaku(0);
                            if (danmakuObj != null) log.warn("Unexpected type for danmaku for bvid {}: Expected Number, got {}", hit.get("bvid"), danmakuObj.getClass().getName());
                        }

                        Object repliesObj = hit.get("replies");
                        if (repliesObj instanceof Number) {
                            dto.setReplies(((Number) repliesObj).longValue());
                        } else {
                            dto.setReplies(0);
                            if (repliesObj != null) log.warn("Unexpected type for replies for bvid {}: Expected Number, got {}", hit.get("bvid"), repliesObj.getClass().getName());
                        }

                        Object favoritesObj = hit.get("favorites");
                        if (favoritesObj instanceof Number) {
                            dto.setFavorites(((Number) favoritesObj).longValue());
                        } else {
                            dto.setFavorites(0);
                            if (favoritesObj != null) log.warn("Unexpected type for favorites for bvid {}: Expected Number, got {}", hit.get("bvid"), favoritesObj.getClass().getName());
                        }

                        Object coinsObj = hit.get("coins");
                        if (coinsObj instanceof Number) {
                            dto.setCoins(((Number) coinsObj).longValue());
                        } else {
                            dto.setCoins(0);
                            if (coinsObj != null) log.warn("Unexpected type for coins for bvid {}: Expected Number, got {}", hit.get("bvid"), coinsObj.getClass().getName());
                        }

                        Object sharesObj = hit.get("shares");
                        if (sharesObj instanceof Number) {
                            dto.setShares(((Number) sharesObj).longValue());
                        } else {
                            dto.setShares(0);
                            if (sharesObj != null) log.warn("Unexpected type for shares for bvid {}: Expected Number, got {}", hit.get("bvid"), sharesObj.getClass().getName());
                        }

                        Object likesObj = hit.get("likes");
                        if (likesObj instanceof Number) {
                            dto.setLikes(((Number) likesObj).longValue());
                        } else {
                            dto.setLikes(0);
                            if (likesObj != null) log.warn("Unexpected type for likes for bvid {}: Expected Number, got {}", hit.get("bvid"), likesObj.getClass().getName());
                        }

                        dto.setHachimi((Boolean) hit.get("is_hachimi"));
                        dto.setAvailable(hit.get("is_available") != null ? (Boolean) hit.get("is_available") : true);
                        dto.setCoverUrl((String) hit.get("coverUrl"));

                        Object categoryIdObj = hit.get("categoryId");
                        if (categoryIdObj instanceof Number) {
                            dto.setCategoryId(((Number) categoryIdObj).intValue());
                        } else {
                            dto.setCategoryId(null);
                            if (categoryIdObj != null) log.warn("Unexpected type for categoryId for bvid {}: Expected Number, got {}", hit.get("bvid"), categoryIdObj.getClass().getName());
                        }

                        String reviewedAtStr = (String) hit.get("reviewedAt");
                        if (reviewedAtStr != null) {
                            try {
                                dto.setReviewedAt(LocalDateTime.parse(reviewedAtStr, DATE_TIME_FORMATTER)); // <-- Change here as well if using same format
                            } catch (DateTimeParseException e) {
                                log.warn("Failed to parse reviewedAt string '{}' for bvid {}: {}", reviewedAtStr, hit.get("bvid"), e.getMessage());
                            }
                        }

                        String updatedAtStr = (String) hit.get("updatedAt");
                        if (updatedAtStr != null) {
                            try {
                                dto.setUpdatedAt(LocalDateTime.parse(updatedAtStr, DATE_TIME_FORMATTER)); // <-- Change here as well if using same format
                            } catch (DateTimeParseException e) {
                                log.warn("Failed to parse updatedAt string '{}' for bvid {}: {}", updatedAtStr, hit.get("bvid"), e.getMessage());
                            }
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
            log.error("Search failed in AdminSearchService", e);
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