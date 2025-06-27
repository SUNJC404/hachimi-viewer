package tv.oxnu0xuu.hachimiviewer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private Client meiliSearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${meili.index.videos}")
    private String videoIndexName;

    /**
     * 将MySQL中的所有视频数据同步到MeiliSearch
     */
    @Transactional(readOnly = true) // 【关键修正1】添加事务注解，保证在整个方法执行期间Session开启
    public void syncVideosToMeiliSearch() {
        try {
            log.info("开始将视频数据从MySQL同步到MeiliSearch...");

            // 【关键修正2】调用新创建的、可以一次性加载作者信息的方法
            List<Video> videos = videoRepository.findAllWithOwners();

            if (videos.isEmpty()) {
                log.warn("数据库中没有找到任何视频数据，同步任务结束。");
                return;
            }
            log.info("从数据库中查询到 {} 条视频数据。", videos.size());

            // 将实体转换为用于索引的DTO
            List<VideoReviewDto> videoDtos = videos.stream()
                    .map(VideoReviewDto::fromEntity)
                    .collect(Collectors.toList());

            // 获取MeiliSearch索引
            Index index = meiliSearchClient.index(videoIndexName);

            // 将DTO列表转换为JSON字符串
            String documents = objectMapper.writeValueAsString(videoDtos);

            // 将文档添加到索引，并设置主键
            log.info("正在将文档批量添加到MeiliSearch索引 '{}'...", videoIndexName);
            TaskInfo task = index.addDocuments(documents, "bvid");

            log.info("已创建MeiliSearch任务，Task UID: {}", task.getTaskUid());
            meiliSearchClient.waitForTask(task.getTaskUid());

            log.info("数据同步成功！");

        } catch (Exception e) {
            log.error("同步数据到MeiliSearch时发生严重错误", e);
        }
    }
}