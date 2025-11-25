package com.example.hls_server.service;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.FavoriteDto;

import java.util.List;

public interface FavoriteService {
    BaseResponse<String> addFavorite(Long userId, Long movieId);
    BaseResponse<String> removeFavorite(Long userId, Long movieId);
    BaseResponse<List<FavoriteDto>> getFavorites(Long userId);
}
