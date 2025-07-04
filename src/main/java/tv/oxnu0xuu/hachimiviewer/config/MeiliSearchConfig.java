package tv.oxnu0xuu.hachimiviewer.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.json.JacksonJsonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeiliSearchConfig {

    @Bean
    public Client meiliSearchClient() {
        // 直接从环境变量读取 MeiliSearch 配置
        String meiliHost = System.getenv("MEILI_HOST");
        String meiliApiKey = System.getenv("MEILI_API_KEY");

        return new Client(new Config(meiliHost, meiliApiKey, new JacksonJsonHandler()));
    }

    @Bean(name = "videoIndexName")
    public String videoIndexName() {
        String indexName = System.getenv("MEILI_INDEX_VIDEOS");
        if (indexName == null || indexName.isEmpty()) {
            return "videos"; // 默认值
        }
        return indexName;
    }
}