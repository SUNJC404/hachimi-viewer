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
     * Cron expression: "0 0 12 * * ?"
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void generateMonthlyLeaderboard() {
        YearMonth targetMonth = YearMonth.of(2025, 7);
        LocalDate today = LocalDate.now();
        log.info("Starting scheduled task: Generate daily leaderboard for {}", today);

        try {
            updateLeaderboardForDay(targetMonth, today);
        } catch (Exception e) {
            log.error("Failed to generate leaderboard for {}", today, e);
        }
    }

    private void updateLeaderboardForDay(YearMonth month, LocalDate day) {
        String dayString = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String shareCode = "leaderboard-" + dayString;

        // 1. Check if a leaderboard for this day already exists.
        Playlist leaderboardPlaylist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>().eq("share_code", shareCode)
        );

        if (leaderboardPlaylist != null) {
            // If it exists, clear its old video entries to overwrite them.
            log.info("Leaderboard for {} already exists. Clearing old entries for update.", dayString);
            playlistVideoMapper.deleteByPlaylistId(leaderboardPlaylist.getId());
        } else {
            // If it does not exist, create a new one.
            log.info("Creating a new daily leaderboard playlist with share code: {}", shareCode);
            String monthString = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String title = month.getMonthValue() + "月" + day.getDayOfMonth() + "日 哈基米音乐排行榜";

            leaderboardPlaylist = new Playlist();
            leaderboardPlaylist.setShareCode(shareCode);
            leaderboardPlaylist.setEditCode("");
            leaderboardPlaylist.setTitle(title);
            leaderboardPlaylist.setDescription(monthString + "月最受欢迎的 " + LEADERBOARD_SIZE + " 首哈基米音乐。");
            leaderboardPlaylist.setPlaylistType("LEADERBOARD");
            leaderboardPlaylist.setViewCount(0);
            leaderboardPlaylist.setCreatedAt(LocalDateTime.now());
            playlistMapper.insert(leaderboardPlaylist);
        }

        // Update the timestamp
        leaderboardPlaylist.setUpdatedAt(LocalDateTime.now());
        playlistMapper.updateById(leaderboardPlaylist);

        // 2. Fetch the top-rated videos for the specified month
        LocalDateTime startDate = month.atDay(1).atStartOfDay();
        LocalDateTime endDate = month.plusMonths(1).atDay(1).atStartOfDay();

        log.info("Fetching top {} videos between {} and {}", LEADERBOARD_SIZE, startDate, endDate);
        List<Video> topVideos = videoMapper.findTopRatedVideosForMonth(startDate, endDate, LEADERBOARD_SIZE);

        if (topVideos.isEmpty()) {
            log.warn("No rated videos found for {}. The leaderboard will be empty.", month.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            return;
        }
        log.info("Found {} videos to populate the daily leaderboard.", topVideos.size());

        // 3. Populate the playlist with the new top videos
        for (int i = 0; i < topVideos.size(); i++) {
            Video video = topVideos.get(i);
            PlaylistVideo playlistVideo = new PlaylistVideo();
            playlistVideo.setPlaylistId(leaderboardPlaylist.getId());
            playlistVideo.setBvid(video.getBvid());
            playlistVideo.setPosition(i + 1); // Use position as the rank
            playlistVideo.setAddedAt(LocalDateTime.now());
            playlistVideoMapper.insert(playlistVideo);
        }

        log.info("Successfully created/updated and populated leaderboard for {}.", dayString);
    }
}