package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.mapper.PlaylistMapper;
import tv.oxnu0xuu.hachimiviewer.mapper.PlaylistVideoMapper;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Playlist;
import tv.oxnu0xuu.hachimiviewer.model.PlaylistVideo;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);
    private static final int LEADERBOARD_SIZE = 50;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private PlaylistVideoMapper playlistVideoMapper;

    /**
     * Runs daily at noon to generate the monthly leaderboard.
     * <p>
     * Cron expression: "0 0 12 * * ?"
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void generateMonthlyLeaderboard() {
// 定义排行榜功能的起始年月
        final YearMonth startMonth = YearMonth.of(2025, 7);
        YearMonth currentMonth = YearMonth.now();

// 检查当前月份是否在起始月份之后（或等于起始月份）
        if (currentMonth.isBefore(startMonth)) {
            log.info("当前月份 {} 早于预定的排行榜开始月份 {}，本次任务跳过。", currentMonth, startMonth);
            return; // 如果时间未到，则不执行任何操作
        }

// 确认执行后，将目标月份设为当前月
        YearMonth targetMonth = currentMonth;
        LocalDate today = LocalDate.now();
        log.info("开始执行定时任务：为 {} 生成 {} 年 {} 月的每日排行榜。", today, targetMonth.getYear(), targetMonth.getMonthValue());

        try {
            updateLeaderboardForDay(targetMonth, today);
        } catch (Exception e) {
            log.error("为 {} 生成排行榜时发生错误", today, e);
        }
    }

    private void updateLeaderboardForDay(YearMonth month, LocalDate day) {
        String dayString = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String shareCode = "leaderboard-" + dayString;
        // 1. 检查当天的排行榜是否已存在
        Playlist leaderboardPlaylist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>().eq("share_code", shareCode)
        );

        if (leaderboardPlaylist != null) {
            // 如果存在，则清空旧的视频条目以便更新
            log.info("日期 {} 的排行榜已存在。正在清空旧条目以进行更新。", dayString);
            playlistVideoMapper.deleteByPlaylistId(leaderboardPlaylist.getId());
        } else {
            // 如果不存在，则创建一个新的
            log.info("正在为日期 {} 创建一个新的每日排行榜播放列表，分享码为：{}", dayString, shareCode);
            String monthString = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String title = month.getMonthValue() + "月" + day.getDayOfMonth() + "日 哈基米音乐排行榜";

            leaderboardPlaylist = new Playlist();
            leaderboardPlaylist.setShareCode(shareCode);
            leaderboardPlaylist.setEditCode(""); // 编辑码为空，因为这是系统生成的
            leaderboardPlaylist.setTitle(title);
            leaderboardPlaylist.setDescription(monthString + "月最受欢迎的 " + LEADERBOARD_SIZE + " 首哈基米音乐。");
            leaderboardPlaylist.setPlaylistType("LEADERBOARD");
            leaderboardPlaylist.setViewCount(0);
            leaderboardPlaylist.setCreatedAt(LocalDateTime.now());
            playlistMapper.insert(leaderboardPlaylist);
        }

        // 更新时间戳
        leaderboardPlaylist.setUpdatedAt(LocalDateTime.now());
        playlistMapper.updateById(leaderboardPlaylist);

        // 2. 获取指定月份的评分最高的视频
        LocalDateTime startDate = month.atDay(1).atStartOfDay();
        LocalDateTime endDate = month.plusMonths(1).atDay(1).atStartOfDay();

        log.info("正在获取 {} 和 {} 之间评分最高的 {} 个视频", startDate, endDate, LEADERBOARD_SIZE);
        List<Video> topVideos = videoMapper.findTopRatedVideosForMonth(startDate, endDate, LEADERBOARD_SIZE);

        if (topVideos.isEmpty()) {
            log.warn("在 {} 未找到任何评分视频。排行榜将为空。", month.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            return;
        }
        log.info("找到 {} 个视频用于填充每日排行榜。", topVideos.size());

        // 3. 将新的热门视频填充到播放列表中
        for (int i = 0; i < topVideos.size(); i++) {
            Video video = topVideos.get(i);
            PlaylistVideo playlistVideo = new PlaylistVideo();
            playlistVideo.setPlaylistId(leaderboardPlaylist.getId());
            playlistVideo.setBvid(video.getBvid());
            playlistVideo.setPosition(i + 1); // 使用位置作为排名
            playlistVideo.setAddedAt(LocalDateTime.now());
            playlistVideoMapper.insert(playlistVideo);
        }

        log.info("已成功为日期 {} 创建/更新并填充了排行榜。", dayString);
    }
}