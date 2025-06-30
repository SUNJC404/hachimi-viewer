package tv.oxnu0xuu.hachimiviewer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.oxnu0xuu.hachimiviewer.service.DataSyncService;

@RestController
@RequestMapping("/api/sync")
public class DataSyncController {

    @Autowired
    private DataSyncService dataSyncService;

    @PostMapping("/videos")
    public ResponseEntity<String> syncVideos() {
        // 在新的线程中运行，避免HTTP请求超时
        new Thread(() -> dataSyncService.syncVideosToMeiliSearch()).start();
        return ResponseEntity.ok("视频数据同步任务已启动。请稍后查看服务器日志确认结果。");
    }
}