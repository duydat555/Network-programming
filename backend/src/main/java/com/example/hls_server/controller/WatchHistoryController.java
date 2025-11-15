package com.example.hls_server.controller;

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

    @PutMapping
    public void updatePosition(@PathVariable Long userId,
                               @RequestBody UpdateWatchHistoryRequest request) {
        watchHistoryService.updateWatchPosition(userId, request);
    }

    @GetMapping
    public List<WatchHistoryDto> getHistory(@PathVariable Long userId) {
        return watchHistoryService.getWatchHistory(userId);
    }
}