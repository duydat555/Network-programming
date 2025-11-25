package com.example.hls_server.service.impl;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.CreateWatchHistoryRequest;
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
    public BaseResponse<WatchHistoryDto> createWatchHistory(Long userId, CreateWatchHistoryRequest request) {
        try {
            // Kiểm tra user có tồn tại không
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Kiểm tra movie có tồn tại không
            Movie movie = movieRepository.findById(request.getMovieId())
                    .orElseThrow(() -> new RuntimeException("Movie not found with id: " + request.getMovieId()));

            // Kiểm tra xem đã có watch history cho movie này chưa
            if (watchHistoryRepository.findByUserIdAndMovieId(userId, request.getMovieId()).isPresent()) {
                return BaseResponse.<WatchHistoryDto>builder()
                        .success(false)
                        .message("Watch history already exists for this movie. Use update instead.")
                        .build();
            }

            // Tạo watch history mới
            WatchHistory watchHistory = WatchHistory.builder()
                    .user(user)
                    .movie(movie)
                    .positionSec(request.getPositionSec() != null ? request.getPositionSec() : 0)
                    .updatedAt(LocalDateTime.now())
                    .build();

            WatchHistory savedHistory = watchHistoryRepository.save(watchHistory);

            // Tạo DTO để trả về
            WatchHistoryDto responseDto = WatchHistoryDto.builder()
                    .movieId(savedHistory.getMovie().getId())
                    .movieTitle(savedHistory.getMovie().getTitle())
                    .positionSec(savedHistory.getPositionSec())
                    .updatedAt(savedHistory.getUpdatedAt())
                    .build();

            return BaseResponse.<WatchHistoryDto>builder()
                    .success(true)
                    .message("Watch history created successfully")
                    .data(responseDto)
                    .build();
        } catch (Exception e) {
            return BaseResponse.<WatchHistoryDto>builder()
                    .success(false)
                    .message("Failed to create watch history: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public BaseResponse<Void> updateWatchPosition(Long userId, UpdateWatchHistoryRequest request) {
        try {
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

            return BaseResponse.<Void>builder()
                    .success(true)
                    .message("Watch position updated successfully")
                    .build();
        } catch (Exception e) {
            return BaseResponse.<Void>builder()
                    .success(false)
                    .message("Failed to update watch position: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public BaseResponse<List<WatchHistoryDto>> getWatchHistory(Long userId) {
        try {
            List<WatchHistoryDto> historyList = watchHistoryRepository.findByUserId(userId)
                    .stream()
                    .map(h -> WatchHistoryDto.builder()
                            .movieId(h.getMovie().getId())
                            .movieTitle(h.getMovie().getTitle())
                            .positionSec(h.getPositionSec())
                            .updatedAt(h.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            return BaseResponse.<List<WatchHistoryDto>>builder()
                    .success(true)
                    .message("Watch history retrieved successfully")
                    .data(historyList)
                    .build();
        } catch (Exception e) {
            return BaseResponse.<List<WatchHistoryDto>>builder()
                    .success(false)
                    .message("Failed to retrieve watch history: " + e.getMessage())
                    .build();
        }
    }
}