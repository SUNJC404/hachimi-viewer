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
        // 只使用最基础的构造函数，这是完全正确的
        return new Client(new Config(meiliHost, meiliApiKey, new JacksonJsonHandler()));
    }
}