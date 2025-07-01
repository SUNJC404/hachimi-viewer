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

    @GetMapping("/")
    public ResponseEntity<?> searchVideos(@RequestParam(name = "q") String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Search query 'q' cannot be empty.\"}");
        }
        try {
            logger.info("Performing search for query: '{}' with filter 'is_hachimi = true'", query);

            // 使用 SearchRequest 来构建带过滤条件的搜索
            SearchRequest searchRequest = new SearchRequest(query);
            searchRequest.setFilter(new String[]{"is_hachimi = true"});

            Object results = videosIndex.search(searchRequest);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error during Meilisearch search", e);
            return ResponseEntity.internalServerError().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}