package com.example.hls_server.controller;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.FavoriteDto;
import com.example.hls_server.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{movieId}")
    public BaseResponse<String> addFavorite(@PathVariable Long userId,
                                            @PathVariable Long movieId) {
        return favoriteService.addFavorite(userId, movieId);
    }

    @DeleteMapping("/{movieId}")
    public BaseResponse<String> removeFavorite(@PathVariable Long userId,
                                               @PathVariable Long movieId) {
        return favoriteService.removeFavorite(userId, movieId);
    }

    @GetMapping
    public BaseResponse<List<FavoriteDto>> getFavorites(@PathVariable Long userId) {
        return favoriteService.getFavorites(userId);
    }
}