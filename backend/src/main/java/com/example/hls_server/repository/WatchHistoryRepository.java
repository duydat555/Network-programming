package com.example.hls_server.repository;

import com.example.hls_server.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    List<WatchHistory> findByUserId(Long userId);
    Optional<WatchHistory> findByUserIdAndMovieId(Long userId, Long movieId);
}
