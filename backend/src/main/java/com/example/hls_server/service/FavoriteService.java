package com.example.hls_server.service;

import com.example.hls_server.dto.FavoriteDto;

import java.util.List;

public interface FavoriteService {
    void addFavorite(Long userId, Long movieId);
    void removeFavorite(Long userId, Long movieId);
    List<FavoriteDto> getFavorites(Long userId);
}
