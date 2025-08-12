package tv.oxnu0xuu.hachimiviewer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowedOriginPatterns(
                        "https://hachimifm.com",
                        "https://*.hachimifm.com",
                        "http://localhost:[*]" // 匹配本地开发的所有端口
                )
                .allowCredentials(true)
                .maxAge(3600);
    }
}