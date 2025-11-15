package com.example.hls_server.service;

import com.example.hls_server.dto.UpdateWatchHistoryRequest;
import com.example.hls_server.dto.WatchHistoryDto;

import java.util.List;

public interface WatchHistoryService {
    void updateWatchPosition(Long userId, UpdateWatchHistoryRequest request);
    List<WatchHistoryDto> getWatchHistory(Long userId);
}
