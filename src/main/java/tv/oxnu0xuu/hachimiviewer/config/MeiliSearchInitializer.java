// src/main/java/tv/oxnu0xuu/hachimiviewer/config/MeiliSearchInitializer.java
package tv.oxnu0xuu.hachimiviewer.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.Settings;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MeiliSearchInitializer {

    private static final Logger log = LoggerFactory.getLogger(MeiliSearchInitializer.class);

    @Autowired
    private Client meiliSearchClient;

    @Value("${meili.index.videos}")
    private String videoIndexName;

    /**
     * This method runs once the application is fully started.
     * It ensures the Meilisearch index and its settings are configured.
     * This is the single source of truth for the index settings.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void configureMeiliSearch() {
        log.info("Verifying and configuring Meilisearch index '{}' on application startup...", videoIndexName);
        try {
            Index index = meiliSearchClient.index(videoIndexName);

            Settings settings = new Settings();

            // 可搜索的属性
            settings.setSearchableAttributes(new String[]{"bvid","owner.name", "title", "description"});

            // 可筛选的属性
            settings.setFilterableAttributes(new String[]{"is_hachimi", "is_available", "pubDate"});

            // 可排序的属性
            settings.setSortableAttributes(new String[]{"pubDate", "views"});

            // 在搜索结果中返回的属性 (关键修改)
            settings.setDisplayedAttributes(new String[]{
                    "bvid", "title", "description",
                    "owner", // <-- 直接返回完整的 owner 对象，包含 name, face, mid
                    "pubDate", "views", "duration", // duration 在 index.html 中也用到了
                    "danmaku", "replies", "favorites",
                    "coins", "shares", "likes",
                    "is_hachimi", "is_available",
                    "coverUrl", "categoryId", "reviewedAt", "updatedAt"
            });

            // 更新 MeiliSearch 中的配置
            index.updateSettings(settings);

            log.info("Successfully configured settings for index '{}'.", videoIndexName);

        } catch (MeilisearchException e) {
            log.error("Failed to configure Meilisearch index '{}'. Please ensure Meilisearch is running and accessible. Error: {}", videoIndexName, e.getMessage());
        }
    }
}