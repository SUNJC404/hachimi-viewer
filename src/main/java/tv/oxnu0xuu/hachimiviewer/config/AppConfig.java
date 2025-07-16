package tv.oxnu0xuu.hachimiviewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class AppConfig {

    /**
     * 获取审核员密码
     * 优先从环境变量读取，如果没有则使用默认值
     */
    @Bean(name = "reviewerPassword")
    public String reviewerPassword() {
        String password = System.getenv("REVIEWER_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = "hachimi2024"; // 默认密码
        }
        return password;
    }

    /**
     * 获取管理员密码
     * 优先从环境变量读取，如果没有则使用默认值
     */
    @Bean(name = "adminPassword")
    public String adminPassword() {
        String password = System.getenv("ADMIN_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = "admin2024"; // 默认密码
        }
        System.out.println("Admin password configured: " + (password.isEmpty() ? "EMPTY" : "SET (length=" + password.length() + ")"));
        return password;
    }

    /**
     * Session 超时时间（分钟）
     */
    @Bean(name = "sessionTimeout")
    public Integer sessionTimeout() {
        String timeout = System.getenv("SESSION_TIMEOUT_MINUTES");
        if (timeout != null && !timeout.isEmpty()) {
            try {
                return Integer.parseInt(timeout);
            } catch (NumberFormatException e) {
                // 如果解析失败，使用默认值
            }
        }
        return 30; // 默认30分钟
    }

    /**
     * 审核批次大小
     */
    @Bean(name = "reviewBatchSize")
    public Integer reviewBatchSize() {
        String batchSize = System.getenv("REVIEW_BATCH_SIZE");
        if (batchSize != null && !batchSize.isEmpty()) {
            try {
                return Integer.parseInt(batchSize);
            } catch (NumberFormatException e) {
                // 如果解析失败，使用默认值
            }
        }
        return 20; // 默认20个
    }

    /**
     * 审核租约时间（分钟）
     */
    @Bean(name = "reviewLeaseMinutes")
    public Integer reviewLeaseMinutes() {
        String leaseMinutes = System.getenv("REVIEW_LEASE_MINUTES");
        if (leaseMinutes != null && !leaseMinutes.isEmpty()) {
            try {
                return Integer.parseInt(leaseMinutes);
            } catch (NumberFormatException e) {
                // 如果解析失败，使用默认值
            }
        }
        return 5; // 默认5分钟
    }

    /**
     * 管理页面每页显示数量
     */
    @Bean(name = "adminPageSize")
    public Integer adminPageSize() {
        String pageSize = System.getenv("ADMIN_PAGE_SIZE");
        if (pageSize != null && !pageSize.isEmpty()) {
            try {
                return Integer.parseInt(pageSize);
            } catch (NumberFormatException e) {
                // 如果解析失败，使用默认值
            }
        }
        return 20; // 默认20条
    }
}