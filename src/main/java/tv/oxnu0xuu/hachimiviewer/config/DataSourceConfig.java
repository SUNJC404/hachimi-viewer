package tv.oxnu0xuu.hachimiviewer.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        // 从环境变量中获取数据库连接信息
        // 这些 key 应该与你的 docker-compose.yml 或运行环境中的变量名匹配
        String url = System.getenv("SPRING_DATASOURCE_URL");
        String username = System.getenv("SPRING_DATASOURCE_USERNAME");
        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");

        // 如果环境变量不存在，可以提供一个默认值，便于本地开发
        if (url == null || url.isEmpty()) {
            url = "jdbc:mysql://localhost:3306/video_db";
        }
        if (username == null || username.isEmpty()) {
            username = "root";
        }
        if (password == null) { // 密码可能为空
            password = "password";
        }

        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}