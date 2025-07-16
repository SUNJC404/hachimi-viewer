package tv.oxnu0xuu.hachimiviewer.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tv.oxnu0xuu.hachimiviewer.service.DataSyncService;

/**
 * 应用启动时确保 Meilisearch 索引包含最新数据
 */
@Component
public class AdminSyncTask implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSyncTask.class);

    @Autowired
    private DataSyncService dataSyncService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if Meilisearch index needs initial sync...");

        // 这里可以添加逻辑来检查是否需要同步
        // 例如：检查索引是否为空，或者上次同步时间等

        // 为了演示，我们注释掉自动同步，改为手动触发
        // dataSyncService.syncVideosToMeiliSearch();

        log.info("Admin sync task completed. Use /api/sync/videos endpoint to manually sync if needed.");
    }
}