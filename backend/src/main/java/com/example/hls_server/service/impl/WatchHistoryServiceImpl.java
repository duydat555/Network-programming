package com.example.hls_server.service.impl;

import com.example.hls_server.dto.UpdateWatchHistoryRequest;
import com.example.hls_server.dto.WatchHistoryDto;
import com.example.hls_server.entity.Movie;
import com.example.hls_server.entity.User;
import com.example.hls_server.entity.WatchHistory;
import com.example.hls_server.repository.MovieRepository;
import com.example.hls_server.repository.UserRepository;
import com.example.hls_server.repository.WatchHistoryRepository;
import com.example.hls_server.service.WatchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchHistoryServiceImpl implements WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Override
    public void updateWatchPosition(Long userId, UpdateWatchHistoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        WatchHistory history = watchHistoryRepository
                .findByUserIdAndMovieId(userId, request.getMovieId())
                .orElse(
                        WatchHistory.builder()
                                .user(user)
                                .movie(movie)
                                .build()
                );

        history.setPositionSec(request.getPositionSec());
        history.setUpdatedAt(LocalDateTime.now());

        watchHistoryRepository.save(history);
    }

    @Override
    public List<WatchHistoryDto> getWatchHistory(Long userId) {
        return watchHistoryRepository.findByUserId(userId)
                .stream()
                .map(h -> WatchHistoryDto.builder()
                        .movieId(h.getMovie().getId())
                        .movieTitle(h.getMovie().getTitle())
                        .positionSec(h.getPositionSec())
                        .updatedAt(h.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}