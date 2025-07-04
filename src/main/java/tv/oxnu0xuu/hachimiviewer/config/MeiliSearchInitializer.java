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
     */
    @EventListener(ApplicationReadyEvent.class)
    public void configureMeiliSearch() {
        log.info("Verifying Meilisearch index configuration on application startup...");
        try {
            Index index = meiliSearchClient.index(videoIndexName);

            Settings settings = new Settings();
            settings.setFilterableAttributes(new String[]{"is_hachimi", "is_available"});
            settings.setSortableAttributes(new String[]{"pubDate", "views"});

            index.updateSettings(settings);

            log.info("Successfully configured filterable and sortable attributes for index '{}'.", videoIndexName);

        } catch (MeilisearchException e) {
            log.error("Failed to configure Meilisearch index '{}'. Please ensure Meilisearch is running and accessible. Error: {}", videoIndexName, e.getMessage());
        }
    }
}