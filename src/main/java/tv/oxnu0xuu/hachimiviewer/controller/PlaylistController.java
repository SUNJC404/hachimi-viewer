// src/main/java/tv/oxnu0xuu/hachimiviewer/controller/PlaylistController.java
package tv.oxnu0xuu.hachimiviewer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tv.oxnu0xuu.hachimiviewer.dto.*;
import tv.oxnu0xuu.hachimiviewer.service.PlaylistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    /**
     * 创建新的播放列表
     */
    @PostMapping
    public ResponseEntity<?> createPlaylist(
            @Valid @RequestBody CreatePlaylistDto dto,
            HttpServletRequest request) {
        try {
            String creatorIp = getClientIp(request);
            PlaylistDto playlist = playlistService.createPlaylist(dto, creatorIp);

            Map<String, Object> response = new HashMap<>();
            response.put("shareCode", playlist.getShareCode());
            response.put("editCode", playlist.getEditCode()); // 仅在创建时返回编辑码
            response.put("title", playlist.getTitle());
            response.put("description", playlist.getDescription());
            response.put("createdAt", playlist.getCreatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "创建播放列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取播放列表详情
     */
    @GetMapping("/{shareCode}")
    public ResponseEntity<?> getPlaylist(@PathVariable String shareCode) {
        try {
            PlaylistDto playlist = playlistService.getPlaylistByShareCode(shareCode);
            if (playlist == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "播放列表不存在"));
            }
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取播放列表失败: " + e.getMessage()));
        }
    }

    /**
     * 验证编辑码
     */
    @PostMapping("/{shareCode}/validate")
    public ResponseEntity<?> validateEditCode(
            @PathVariable String shareCode,
            @RequestBody Map<String, String> body) {
        String editCode = body.get("editCode");
        if (editCode == null || editCode.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "编辑码不能为空"));
        }

        boolean isValid = playlistService.validateEditCode(shareCode, editCode);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    /**
     * 更新播放列表信息
     */
    @PutMapping("/{shareCode}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable String shareCode,
            @Valid @RequestBody UpdatePlaylistDto dto) {
        try {
            PlaylistDto playlist = playlistService.updatePlaylist(shareCode, dto);
            return ResponseEntity.ok(playlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "更新播放列表失败: " + e.getMessage()));
        }
    }

    /**
     * 添加视频到播放列表
     */
    @PostMapping("/{shareCode}/videos")
    public ResponseEntity<?> addVideoToPlaylist(
            @PathVariable String shareCode,
            @Valid @RequestBody AddVideoToPlaylistDto dto) {
        try {
            PlaylistVideoDto video = playlistService.addVideoToPlaylist(shareCode, dto);
            return ResponseEntity.ok(video);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "添加视频失败: " + e.getMessage()));
        }
    }

    /**
     * 从播放列表移除视频
     */
    @DeleteMapping("/{shareCode}/videos/{videoId}")
    public ResponseEntity<?> removeVideoFromPlaylist(
            @PathVariable String shareCode,
            @PathVariable Long videoId,
            @RequestParam String editCode) {
        try {
            playlistService.removeVideoFromPlaylist(shareCode, editCode, videoId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "移除视频失败: " + e.getMessage()));
        }
    }

    /**
     * 调整视频位置
     */
    @PutMapping("/{shareCode}/videos/{videoId}/position")
    public ResponseEntity<?> reorderVideo(
            @PathVariable String shareCode,
            @PathVariable Long videoId,
            @RequestBody Map<String, Object> body) {
        try {
            String editCode = (String) body.get("editCode");
            Integer newPosition = (Integer) body.get("position");

            if (editCode == null || newPosition == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "缺少必要参数"));
            }

            playlistService.reorderVideo(shareCode, editCode, videoId, newPosition);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "调整位置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}