package tv.oxnu0xuu.hachimiviewer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);

    // 【新增配置】定义每批处理的数据量
    private static final int BATCH_SIZE = 100; // 每次处理100条，您可以根据服务器性能调整这个值

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
        log.info("启动分批数据同步任务，每批处理 {} 条数据...", BATCH_SIZE);
        int pageNumber = 0;
        Page<Video> videoPage;

        try {
            // 获取MeiliSearch索引
            Index index = meiliSearchClient.index(videoIndexName);

            do {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                videoPage = videoRepository.findAllWithOwners(pageable);

                if (!videoPage.hasContent()) {
                    log.info("所有数据页均已处理完毕。");
                    break;
                }

                List<Video> videos = videoPage.getContent();
                log.info("正在处理第 {} 页，包含 {} 条数据...", pageNumber + 1, videos.size());

                // 转换DTO
                List<VideoReviewDto> videoDtos = videos.stream()
                        .map(VideoReviewDto::fromEntity)
                        .collect(Collectors.toList());

                if (videoDtos.isEmpty()) {
                    log.warn("当前页没有可转换的数据，跳过。");
                    continue;
                }

                // 发送到MeiliSearch
                String documents = objectMapper.writeValueAsString(videoDtos);
                TaskInfo task = index.addDocuments(documents, "bvid");
                log.info("  -> 已创建MeiliSearch添加任务，Task UID: {}", task.getTaskUid());

                // 等待当前批次任务完成（可选，但可以更清楚地看到每一步的结果）
                meiliSearchClient.waitForTask(task.getTaskUid());
                log.info("  -> 批次 {} 已成功同步。", pageNumber + 1);

                // 【关键】在处理完一个批次后，进行短暂休眠，给服务器喘息时间
                try {
                    Thread.sleep(500); // 休眠500毫秒
                } catch (InterruptedException e) {
                    log.warn("线程休眠被中断", e);
                    Thread.currentThread().interrupt();
                }

                pageNumber++;

            } while (videoPage.hasNext());

            log.info("数据同步任务成功完成！");

        } catch (Exception e) {
            log.error("分批同步数据到MeiliSearch时发生严重错误", e);
        }
    }
}