package tv.oxnu0xuu.hachimiviewer.repository;

import tv.oxnu0xuu.hachimiviewer.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

    /**
     * Finds the top 50 videos that have not yet been reviewed,
     * ordered by their publication date to review older videos first.
     * @return A list of up to 50 unreviewed Video entities.
     */
    List<Video> findTop50ByIsReviewedFalseOrderByPubDateAsc();
}
