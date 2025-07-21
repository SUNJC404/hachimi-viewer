// 在 HachimiViewerApplication.java 中，MapperScan 注解已经包含了整个 mapper 包
// 所以新的 PlaylistMapper 和 PlaylistVideoMapper 会自动被扫描到

// 如果需要添加定时清理任务，可以创建一个新的 Service
// src/main/java/tv/oxnu0xuu/hachimiviewer/service/PlaylistCleanupService.java
package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.mapper.PlaylistMapper;
import tv.oxnu0xuu.hachimiviewer.model.Playlist;

import java.time.LocalDateTime;

@Service
public class PlaylistCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistCleanupService.class);

    @Autowired
    private PlaylistMapper playlistMapper;

    /**
     * 每天凌晨2点清理超过7天未访问且没有视频的空播放列表
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupEmptyPlaylists() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 查询30天前创建且浏览次数为0的播放列表
        int deletedCount = playlistMapper.delete(
                new QueryWrapper<Playlist>()
                        .lt("created_at", sevenDaysAgo)
                        .eq("view_count", 0)
        );

        if (deletedCount > 0) {
            log.info("清理了 {} 个超过7天未访问的空播放列表", deletedCount);
        }
    }
}