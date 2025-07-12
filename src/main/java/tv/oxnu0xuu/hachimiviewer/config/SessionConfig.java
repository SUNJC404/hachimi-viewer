package tv.oxnu0xuu.hachimiviewer.config;

import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SessionConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("sessionTimeout")
    private Integer sessionTimeoutMinutes;

    /**
     * 配置 Session 监听器，设置 Session 超时时间
     */
    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> sessionListener() {
        ServletListenerRegistrationBean<HttpSessionListener> listenerBean =
                new ServletListenerRegistrationBean<>();

        listenerBean.setListener(new HttpSessionListener() {
            @Override
            public void sessionCreated(jakarta.servlet.http.HttpSessionEvent se) {
                // 设置 session 超时时间（秒）
                se.getSession().setMaxInactiveInterval(sessionTimeoutMinutes * 60);
            }
        });

        return listenerBean;
    }
}