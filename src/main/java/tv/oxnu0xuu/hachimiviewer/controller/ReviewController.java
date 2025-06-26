package tv.oxnu0xuu.hachimiviewer.controller;

import tv.oxnu0xuu.hachimiviewer.dto.UpdateStatusRequestDto;
import tv.oxnu0xuu.hachimiviewer.dto.VideoReviewDto;
import tv.oxnu0xuu.hachimiviewer.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * API endpoint to get the queue of videos for review.
     * @return A JSON array of up to 50 videos.
     */
    @GetMapping("/review-queue")
    public ResponseEntity<List<VideoReviewDto>> getReviewQueue() {
        List<VideoReviewDto> queue = reviewService.getReviewQueue();
        return ResponseEntity.ok(queue);
    }

    /**
     * API endpoint to update the review status of a video.
     * @param bvid The BVID of the video from the URL path.
     * @param request The request body containing the 'bachimi' status.
     * @return A success response.
     */
    @PostMapping("/videos/{bvid}/status")
    public ResponseEntity<Void> updateVideoStatus(
            @PathVariable String bvid,
            @RequestBody UpdateStatusRequestDto request) {
        reviewService.updateVideoStatus(bvid, request.isHachimi());
        return ResponseEntity.ok().build();
    }
}