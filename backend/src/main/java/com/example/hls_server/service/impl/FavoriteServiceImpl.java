package com.example.hls_server.service.impl;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.FavoriteDto;
import com.example.hls_server.entity.Favorite;
import com.example.hls_server.entity.Movie;
import com.example.hls_server.entity.User;
import com.example.hls_server.repository.FavoriteRepository;
import com.example.hls_server.repository.MovieRepository;
import com.example.hls_server.repository.UserRepository;
import com.example.hls_server.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Override
    public BaseResponse<String> addFavorite(Long userId, Long movieId) {
        try {
            if (favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
                return BaseResponse.<String>builder()
                        .success(false)
                        .message("Movie already in favorites")
                        .data(null)
                        .build();
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new RuntimeException("Movie not found"));

            Favorite favorite = Favorite.builder()
                    .user(user)
                    .movie(movie)
                    .createdAt(LocalDateTime.now())
                    .build();

            favoriteRepository.save(favorite);

            return BaseResponse.<String>builder()
                    .success(true)
                    .message("Movie added to favorites successfully")
                    .data(null)
                    .build();
        } catch (Exception e) {
            return BaseResponse.<String>builder()
                    .success(false)
                    .message("Failed to add movie to favorites: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public BaseResponse<String> removeFavorite(Long userId, Long movieId) {
        try {
            if (!favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
                return BaseResponse.<String>builder()
                        .success(false)
                        .message("Movie not in favorites")
                        .data(null)
                        .build();
            }

            favoriteRepository.deleteByUserIdAndMovieId(userId, movieId);

            return BaseResponse.<String>builder()
                    .success(true)
                    .message("Movie removed from favorites successfully")
                    .data(null)
                    .build();
        } catch (Exception e) {
            return BaseResponse.<String>builder()
                    .success(false)
                    .message("Failed to remove movie from favorites: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public BaseResponse<List<FavoriteDto>> getFavorites(Long userId) {
        try {
            List<Favorite> favorites = favoriteRepository.findByUserId(userId);

            List<FavoriteDto> favoriteDtos = favorites.stream()
                    .map(fav -> FavoriteDto.builder()
                            .movieId(fav.getMovie().getId())
                            .title(fav.getMovie().getTitle())
                            .posterUrl(fav.getMovie().getPosterUrl())
                            .build())
                    .collect(Collectors.toList());

            return BaseResponse.<List<FavoriteDto>>builder()
                    .success(true)
                    .message("Favorites retrieved successfully")
                    .data(favoriteDtos)
                    .build();
        } catch (Exception e) {
            return BaseResponse.<List<FavoriteDto>>builder()
                    .success(false)
                    .message("Failed to retrieve favorites: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
}