package com.example.hls_server.service.impl;

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
    public void addFavorite(Long userId, Long movieId) {
        if (favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
            return; // đã tồn tại thì bỏ qua
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
    }

    @Override
    public void removeFavorite(Long userId, Long movieId) {
        favoriteRepository.deleteByUserIdAndMovieId(userId, movieId);
    }

    @Override
    public List<FavoriteDto> getFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);

        return favorites.stream()
                .map(fav -> FavoriteDto.builder()
                        .movieId(fav.getMovie().getId())
                        .title(fav.getMovie().getTitle())
                        .posterUrl(fav.getMovie().getPosterUrl())
                        .build())
                .collect(Collectors.toList());
    }
}