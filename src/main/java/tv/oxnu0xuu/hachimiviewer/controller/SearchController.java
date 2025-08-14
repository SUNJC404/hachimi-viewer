package tv.oxnu0xuu.hachimiviewer.controller;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final Index videosIndex;

    @Autowired
    public SearchController(Client client, @Value("${meili.index.videos}") String indexName) {
        this.videosIndex = client.index(indexName);
    }

    @GetMapping
    public ResponseEntity<?> searchVideos(
            @RequestParam(name = "q") String query,
            // Meilisearch 的分页参数，page 从 1 开始
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int hitsPerPage,
            @RequestParam(defaultValue = "pubDate:desc") String sort) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Search query 'q' cannot be empty.\"}");
        }
        try {
            logger.info("Performing search for query: '{}', page: {}, hitsPerPage: {}, sort: {}", query, page, hitsPerPage, sort);

            SearchRequest searchRequest = new SearchRequest(query);
            searchRequest.setFilter(new String[]{"is_hachimi = true"});
            searchRequest.setPage(page);
            searchRequest.setHitsPerPage(hitsPerPage);
            searchRequest.setSort(new String[]{sort});

            Object results = videosIndex.search(searchRequest);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error during Meilisearch search", e);
            return ResponseEntity.internalServerError().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}