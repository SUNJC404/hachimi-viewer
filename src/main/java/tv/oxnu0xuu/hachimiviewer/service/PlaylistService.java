// src/main/java/tv/oxnu0xuu/hachimiviewer/service/PlaylistService.java
package tv.oxnu0xuu.hachimiviewer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.oxnu0xuu.hachimiviewer.dto.*;
import tv.oxnu0xuu.hachimiviewer.mapper.PlaylistMapper;
import tv.oxnu0xuu.hachimiviewer.mapper.PlaylistVideoMapper;
import tv.oxnu0xuu.hachimiviewer.mapper.VideoMapper;
import tv.oxnu0xuu.hachimiviewer.model.Playlist;
import tv.oxnu0xuu.hachimiviewer.model.PlaylistVideo;
import tv.oxnu0xuu.hachimiviewer.model.Video;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);
    private static final int MAX_VIDEOS_PER_PLAYLIST = 100;
    private static final String SHARE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String EDIT_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private PlaylistVideoMapper playlistVideoMapper;

    @Autowired
    private VideoMapper videoMapper;

    /**
     * 创建新的播放列表
     */
    @Transactional
    public Playlist createPlaylist(CreatePlaylistDto dto, String creatorIp) {
        Playlist playlist = new Playlist();
        playlist.setTitle(dto.getTitle());
        playlist.setDescription(dto.getDescription());
        playlist.setShareCode(generateUniqueShareCode());
        playlist.setEditCode(generateEditCode());
        playlist.setCreatorIp(creatorIp);
        playlist.setViewCount(0);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());

        playlistMapper.insert(playlist);

        return playlist;
    }

    /**
     * 通过分享码获取播放列表
     */
    @Transactional
    public PlaylistDto getPlaylistByShareCode(String shareCode) {
        Playlist playlist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>().eq("share_code", shareCode)
        );

        if (playlist == null) {
            return null;
        }

        // 增加浏览次数
        playlistMapper.incrementViewCount(shareCode);

        PlaylistDto dto = PlaylistDto.fromEntity(playlist);

        // 获取视频列表
        List<PlaylistVideo> playlistVideos = playlistVideoMapper.findByPlaylistIdWithVideos(playlist.getId());
        List<PlaylistVideoDto> videoDtos = playlistVideos.stream()
                .map(PlaylistVideoDto::fromEntity)
                .collect(Collectors.toList());

        dto.setVideos(videoDtos);
        dto.setVideoCount(videoDtos.size());

        return dto;
    }

    /**
     * 验证编辑码
     */
    public boolean validateEditCode(String shareCode, String editCode) {
        Playlist playlist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>()
                        .eq("share_code", shareCode)
                        .eq("edit_code", editCode)
        );
        return playlist != null;
    }

    /**
     * 更新播放列表信息
     */
    @Transactional
    public PlaylistDto updatePlaylist(String shareCode, UpdatePlaylistDto dto) {
        Playlist playlist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>()
                        .eq("share_code", shareCode)
                        .eq("edit_code", dto.getEditCode())
        );

        if (playlist == null) {
            throw new IllegalArgumentException("无效的编辑码");
        }

        if (dto.getTitle() != null) {
            playlist.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            playlist.setDescription(dto.getDescription());
        }
        playlist.setUpdatedAt(LocalDateTime.now());

        playlistMapper.updateById(playlist);

        return getPlaylistByShareCode(shareCode);
    }

    /**
     * 添加视频到播放列表
     */
    @Transactional
    public PlaylistVideoDto addVideoToPlaylist(String shareCode, AddVideoToPlaylistDto dto) {
        Playlist playlist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>()
                        .eq("share_code", shareCode)
                        .eq("edit_code", dto.getEditCode())
        );

        if (playlist == null) {
            throw new IllegalArgumentException("无效的编辑码");
        }

        // 检查视频是否存在
        Video video = videoMapper.selectById(dto.getBvid());
        if (video == null) {
            throw new IllegalArgumentException("视频不存在");
        }

        // 检查视频是否已在列表中
        Long existingCount = playlistVideoMapper.selectCount(
                new QueryWrapper<PlaylistVideo>()
                        .eq("playlist_id", playlist.getId())
                        .eq("bvid", dto.getBvid())
        );

        if (existingCount > 0) {
            throw new IllegalArgumentException("视频已在播放列表中");
        }

        // 检查播放列表视频数量限制
        // 1. 获取当前播放列表的专属限制。如果为null或小于等于0，则使用系统默认值。
        final int limit = (playlist.getMaxVideos() != null && playlist.getMaxVideos() > 0)
                ? playlist.getMaxVideos()
                : MAX_VIDEOS_PER_PLAYLIST;

        // 2. 获取当前播放列表的视频总数
        Long videoCount = playlistVideoMapper.selectCount(
                new QueryWrapper<PlaylistVideo>().eq("playlist_id", playlist.getId())
        );

        // 3. 使用动态获取的 limit 进行比较
        if (videoCount >= limit) {
            throw new IllegalArgumentException("播放列表已达到最大视频数量限制 (" + limit + ")");
        }

        // 获取新位置
        Integer maxPosition = playlistVideoMapper.findMaxPosition(playlist.getId());
        int newPosition = (maxPosition == null) ? 1 : maxPosition + 1;

        // 添加视频
        PlaylistVideo playlistVideo = new PlaylistVideo();
        playlistVideo.setPlaylistId(playlist.getId());
        playlistVideo.setBvid(dto.getBvid());
        playlistVideo.setPosition(newPosition);
        playlistVideo.setAddedAt(LocalDateTime.now());

        playlistVideoMapper.insert(playlistVideo);

        // 更新播放列表更新时间
        playlist.setUpdatedAt(LocalDateTime.now());
        playlistMapper.updateById(playlist);

        // 加载完整视频信息
        playlistVideo.setVideo(video);

        return PlaylistVideoDto.fromEntity(playlistVideo);
    }

    /**
     * 从播放列表移除视频
     */
    @Transactional
    public void removeVideoFromPlaylist(String shareCode, String editCode, Long playlistVideoId) {
        Playlist playlist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>()
                        .eq("share_code", shareCode)
                        .eq("edit_code", editCode)
        );

        if (playlist == null) {
            throw new IllegalArgumentException("无效的编辑码");
        }

        PlaylistVideo playlistVideo = playlistVideoMapper.selectOne(
                new QueryWrapper<PlaylistVideo>()
                        .eq("id", playlistVideoId)
                        .eq("playlist_id", playlist.getId())
        );

        if (playlistVideo == null) {
            throw new IllegalArgumentException("视频不在播放列表中");
        }

        // 删除视频
        playlistVideoMapper.deleteById(playlistVideoId);

        // 调整后续视频的位置
        playlistVideoMapper.decrementPositionsAfter(playlist.getId(), playlistVideo.getPosition());

        // 更新播放列表更新时间
        playlist.setUpdatedAt(LocalDateTime.now());
        playlistMapper.updateById(playlist);
    }

    /**
     * 调整视频在播放列表中的位置
     */
    @Transactional
    public void reorderVideo(String shareCode, String editCode, Long playlistVideoId, Integer newPosition) {
        Playlist playlist = playlistMapper.selectOne(
                new QueryWrapper<Playlist>()
                        .eq("share_code", shareCode)
                        .eq("edit_code", editCode)
        );

        if (playlist == null) {
            throw new IllegalArgumentException("无效的编辑码");
        }

        PlaylistVideo playlistVideo = playlistVideoMapper.selectOne(
                new QueryWrapper<PlaylistVideo>()
                        .eq("id", playlistVideoId)
                        .eq("playlist_id", playlist.getId())
        );

        if (playlistVideo == null) {
            throw new IllegalArgumentException("视频不在播放列表中");
        }

        // 更新位置逻辑较复杂，这里简化处理
        playlistVideoMapper.updatePosition(playlistVideoId, newPosition);

        // 更新播放列表更新时间
        playlist.setUpdatedAt(LocalDateTime.now());
        playlistMapper.updateById(playlist);
    }

    /**
     * 获取随机播放列表
     */
    @Transactional(readOnly = true)
    public List<PlaylistDto> getRandomPlaylists(int limit) {
        List<Playlist> playlists = playlistMapper.findRandomPlaylists(limit);
        if (playlists.isEmpty()) {
            return Collections.emptyList();
        }
        return playlists.stream().map(playlist -> {
            PlaylistDto dto = PlaylistDto.fromEntity(playlist);
            List<PlaylistVideo> playlistVideos = playlistVideoMapper.findByPlaylistIdWithVideos(playlist.getId());
            List<PlaylistVideoDto> videoDtos = playlistVideos.stream()
                    .limit(5)
                    .map(PlaylistVideoDto::fromEntity)
                    .collect(Collectors.toList());
            dto.setVideos(videoDtos);
            dto.setVideoCount(playlistVideos.size());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 生成唯一的分享码
     */
    private String generateUniqueShareCode() {
        String code;
        int attempts = 0;
        do {
            code = generateRandomString(SHARE_CODE_CHARS, 5);
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("无法生成唯一的分享码");
            }
        } while (playlistMapper.selectCount(new QueryWrapper<Playlist>().eq("share_code", code)) > 0);

        return code;
    }

    /**
     * 生成编辑码
     */
    private String generateEditCode() {
        return generateRandomString(EDIT_CODE_CHARS, 10);
    }

    /**
     * 生成随机字符串
     */
    private String generateRandomString(String chars, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}