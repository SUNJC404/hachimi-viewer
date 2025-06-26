package tv.oxnu0xuu.hachimiviewer.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.json.JacksonJsonHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeiliSearchConfig {

    @Value("${meili.host}")
    private String meiliHost;

    @Value("${meili.apiKey}")
    private String meiliApiKey;

    @Bean
    public Client meiliSearchClient() {
        return new Client(new Config(meiliHost, meiliApiKey, new JacksonJsonHandler()));
    }
}