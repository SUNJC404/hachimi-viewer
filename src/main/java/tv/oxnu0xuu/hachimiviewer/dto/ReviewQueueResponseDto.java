package tv.oxnu0xuu.hachimiviewer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReviewQueueResponseDto {
    private String reviewerId;
    private List<VideoReviewDto> videos;
}