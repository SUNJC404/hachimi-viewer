package tv.oxnu0xuu.hachimiviewer.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tv.oxnu0xuu.hachimiviewer.dto.VideoDetailDto;
import tv.oxnu0xuu.hachimiviewer.service.AdminService;
import tv.oxnu0xuu.hachimiviewer.service.StatisticsService;
import tv.oxnu0xuu.hachimiviewer.dto.OwnerDto; // Import OwnerDto

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/videos")
    public ResponseEntity<?> getVideos(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isHachimi,
            @RequestParam(required = false) Boolean isReported, // Add this
            @RequestParam(defaultValue = "pubDate:desc") String sort) {

        // 检查管理员权限
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }

        return ResponseEntity.ok(adminService.getVideos(page, size, search, isHachimi, isReported, sort));
    }

    @PutMapping("/videos/{bvid}/hachimi")
    public ResponseEntity<?> toggleHachimiStatus(
            HttpSession session,
            @PathVariable String bvid) {

        // 检查管理员权限
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }

        try {
            adminService.toggleHachimiStatus(bvid);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/videos/{bvid}/resolve-report")
    public ResponseEntity<?> resolveReport(
            HttpSession session,
            @PathVariable String bvid) {

        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }

        try {
            adminService.resolveReport(bvid);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/videos/{bvid}")
    public ResponseEntity<?> getVideoDetail(
            HttpSession session,
            @PathVariable String bvid) {

        // 检查管理员权限
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }

        VideoDetailDto detail = adminService.getVideoDetail(bvid);
        if (detail == null) {
            return ResponseEntity.status(404).body(Map.of("error", "视频不存在"));
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getDashboardStatistics(HttpSession session) {
        // 检查管理员权限
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }

        return ResponseEntity.ok(statisticsService.getDashboardStatistics());
    }

    // New Endpoint for Hachimi Authors List
    @GetMapping("/statistics/authors")
    public ResponseEntity<List<OwnerDto>> getHachimiAuthors(HttpSession session) {
        // Check admin authentication
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return ResponseEntity.status(401).body(null); // Return 401 with no body for security
        }
        return ResponseEntity.ok(statisticsService.getDistinctHachimiAuthorsList());
    }
}