package com.example.hls_server.controller;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.CreateWatchHistoryRequest;
import com.example.hls_server.dto.UpdateWatchHistoryRequest;
import com.example.hls_server.dto.WatchHistoryDto;
import com.example.hls_server.service.WatchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @PostMapping
    public BaseResponse<WatchHistoryDto> createWatchHistory(@PathVariable Long userId,
                                                             @RequestBody CreateWatchHistoryRequest request) {
        return watchHistoryService.createWatchHistory(userId, request);
    }

    @PutMapping
    public BaseResponse<Void> updatePosition(@PathVariable Long userId,
                               @RequestBody UpdateWatchHistoryRequest request) {
        return watchHistoryService.updateWatchPosition(userId, request);
    }

    @GetMapping
    public BaseResponse<List<WatchHistoryDto>> getHistory(@PathVariable Long userId) {
        return watchHistoryService.getWatchHistory(userId);
    }
}