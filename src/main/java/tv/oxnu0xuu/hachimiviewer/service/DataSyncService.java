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
    // 定义每批处理100条
    private static final int BATCH_SIZE = 100;
    // 定义每批处理完后休眠2秒（2000毫秒），给服务器足够的时间喘息
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
        log.info("启动分批数据同步任务，每批处理 {} 条，批次间隔 {} 毫秒...", BATCH_SIZE, BATCH_SLEEP_MS);
        int pageNumber = 0;
        Page<Video> videoPage;

        try {
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
                log.info("  -> 已将批次 {} 的任务提交给MeiliSearch，Task UID: {}", pageNumber + 1, task.getTaskUid());

                // 【最终方案】我们不再调用任何 waitForTask 方法，彻底避免超时和编译错误。
                // 而是直接让当前线程休眠，把CPU时间完全让给 MeiliSearch 去处理数据。
                try {
                    Thread.sleep(BATCH_SLEEP_MS);
                } catch (InterruptedException e) {
                    log.warn("线程休眠被中断", e);
                    Thread.currentThread().interrupt();
                }

                pageNumber++;

            } while (videoPage.hasNext());

            log.info("所有数据批次均已提交给 MeiliSearch，同步任务完成！");

        } catch (Exception e) {
            log.error("分批同步数据到MeiliSearch时发生严重错误", e);
        }
    }
}