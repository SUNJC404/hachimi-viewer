package tv.oxnu0xuu.hachimiviewer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.Settings;
import com.meilisearch.sdk.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.model.Video;
import tv.oxnu0xuu.hachimiviewer.repository.VideoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);
    private static final int BATCH_SIZE = 100;
    private static final long BATCH_SLEEP_MS = 2000;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private Client meiliSearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${meili.index.videos}")
    private String videoIndexName;

    @Transactional(readOnly = true)
    public void syncVideosToMeiliSearch() {
        log.info("启动哈基米视频同步任务...");
        int pageNumber = 0;
        Page<Video> videoPage;

        try {
            Index index = meiliSearchClient.index(videoIndexName);

            // --- 步骤 1: 清空所有现有文档 ---
//            log.info("正在清空 MeiliSearch 索引 '{}' 中的所有旧data数据...", videoIndexName);
//            TaskInfo clearTask = index.deleteAllDocuments();
//            // 建议等待清空任务完成，确保后续添加不会出错
//            index.waitForTask(clearTask.getTaskUid());
//            log.info("索引清空完成！");


            // --- 步骤 2: 配置索引的筛选和排序属性 (保留的优化) ---
            log.info("正在为索引 '{}' 配置可筛选和可排序的属性...", videoIndexName);
            Settings settings = new Settings();
            settings.setFilterableAttributes(new String[]{"is_hachimi", "is_available"});
            settings.setSortableAttributes(new String[]{"pubDate", "views"});
            index.updateSettings(settings);
            log.info("索引设置更新成功！");


            // --- 步骤 3: 分批从数据库读取并同步新数据 ---
            log.info("开始分批同步哈基米视频，每批处理 {} 条...", BATCH_SIZE);
            do {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                // 【核心修改】调用新的查询方法，只查找 is_hachimi=true 的视频
                videoPage = videoRepository.findAllHachimiWithOwners(pageable);

                if (!videoPage.hasContent()) {
                    log.info("所有哈基米视频数据页均已处理完毕。");
                    break;
                }

                List<Video> videos = videoPage.getContent();
                log.info("正在处理第 {} 页，包含 {} 条哈基米视频...", pageNumber + 1, videos.size());

                List<VideoReviewDto> videoDtos = videos.stream()
                        .map(VideoReviewDto::fromEntity)
                        .collect(Collectors.toList());

                if (videoDtos.isEmpty()) {
                    log.warn("当前页没有可转换的数据，跳过。");
                    pageNumber++;
                    continue;
                }

                String documents = objectMapper.writeValueAsString(videoDtos);
                TaskInfo task = index.addDocuments(documents, "bvid");
                log.info("  -> 已将批次 {} 的任务提交给 MeiliSearch，Task UID: {}", pageNumber + 1, task.getTaskUid());

                try {
                    Thread.sleep(BATCH_SLEEP_MS);
                } catch (InterruptedException e) {
                    log.warn("线程休眠被中断", e);
                    Thread.currentThread().interrupt();
                }

                pageNumber++;

            } while (videoPage.hasNext());

            log.info("所有哈基米视频数据批次均已提交给 MeiliSearch，同步任务完成！");

        } catch (Exception e) {
            log.error("同步数据到 MeiliSearch 时发生严重错误", e);
        }
    }

    @Scheduled(cron = "0 * * * * ?")
    @Transactional(readOnly = true)
    public void syncRecentUpdatesToMeiliSearch() {
        // 计算出一分钟前的时间点
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        log.info("【增量同步任务】启动，正在查找自 {} 以后更新的哈基米视频...", oneMinuteAgo);

        // 使用新的查询方法，找出最近更新的视频
        List<Video> recentlyUpdatedVideos = videoRepository.findHachimiVideosUpdatedSince(oneMinuteAgo);

        if (recentlyUpdatedVideos.isEmpty()) {
            log.info("【增量同步任务】没有找到最近更新的视频，任务结束。");
            return;
        }

        log.info("【增量同步任务】发现 {} 条更新的视频，正在同步到 Meilisearch...", recentlyUpdatedVideos.size());

        try {
            // 将实体转换为 DTO
            List<VideoReviewDto> dtosToSync = recentlyUpdatedVideos.stream()
                    .map(VideoReviewDto::fromEntity)
                    .collect(Collectors.toList());

            // 序列化为 JSON
            String documents = objectMapper.writeValueAsString(dtosToSync);

            Index index = meiliSearchClient.index(videoIndexName);

            // 使用 addDocuments 方法。它会智能地新增或替换已有文档（根据主键 bvid）
            TaskInfo task = index.addDocuments(documents, "bvid");

            log.info("【增量同步任务】成功提交 {} 条更新到 Meilisearch，Task UID: {}", dtosToSync.size(), task.getTaskUid());

        } catch (Exception e) {
            log.error("【增量同步任务】在同步最近更新时发生严重错误", e);
        }
    }
}