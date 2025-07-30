package tv.oxnu0xuu.hachimiviewer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.oxnu0xuu.hachimiviewer.service.LeaderboardService;

import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardController.class);

    @Autowired
    private LeaderboardService leaderboardService;

    /**
     * Manually triggers the generation of the monthly leaderboard.
     * This is useful for testing or for immediate updates outside the scheduled time.
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateLeaderboardManually() {
        log.info("Manual trigger received for leaderboard generation.");

        // Run the task in a new thread to avoid blocking the HTTP response.
        new Thread(() -> {
            try {
                leaderboardService.generateMonthlyLeaderboard();
            } catch (Exception e) {
                log.error("Manual leaderboard generation failed.", e);
            }
        }).start();

        return ResponseEntity.ok(Map.of(
                "message", "排行榜手动生成任务已启动。请查看服务器日志确认完成情况。"
        ));
    }
}