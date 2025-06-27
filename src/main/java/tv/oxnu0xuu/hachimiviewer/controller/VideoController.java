package tv.oxnu0xuu.hachimiviewer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.service.VideoService;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/latest")
    public ResponseEntity<List<VideoReviewDto>> getLatestVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(videoService.getLatestHachimiVideos(page, size));
    }

    @GetMapping("/random")
    public ResponseEntity<List<VideoReviewDto>> getRandomVideos(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(videoService.getRandomHachimiVideos(limit));
    }
}