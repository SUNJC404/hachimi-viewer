package tv.oxnu0xuu.hachimiviewer.controller;

import tv.oxnu0xuu.hachimiviewer.dto.ReviewQueueResponseDto;
import tv.oxnu0xuu.hachimiviewer.dto.UpdateStatusRequestDto;
import tv.oxnu0xuu.hachimiviewer.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/review-queue")
    public ResponseEntity<ReviewQueueResponseDto> getReviewQueue() {
        ReviewQueueResponseDto queue = reviewService.getReviewQueue();
        return ResponseEntity.ok(queue);
    }

    @PostMapping("/review-queue/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestBody Map<String, String> payload) {
        String reviewerId = payload.get("reviewerId");
        if (reviewerId != null) {
            reviewService.extendLease(reviewerId);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/videos/{bvid}/status")
    public ResponseEntity<Void> updateVideoStatus(
            @PathVariable String bvid,
            @RequestBody UpdateStatusRequestDto request) {
        reviewService.updateVideoStatus(bvid, request.isHachimi());
        return ResponseEntity.ok().build();
    }
}