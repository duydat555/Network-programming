package com.example.hls_server.service;

import com.example.hls_server.dto.BaseResponse;
import com.example.hls_server.dto.CreateWatchHistoryRequest;
import com.example.hls_server.dto.UpdateWatchHistoryRequest;
import com.example.hls_server.dto.WatchHistoryDto;

import java.util.List;

public interface WatchHistoryService {
    BaseResponse<WatchHistoryDto> createWatchHistory(Long userId, CreateWatchHistoryRequest request);
    BaseResponse<Void> updateWatchPosition(Long userId, UpdateWatchHistoryRequest request);
    BaseResponse<List<WatchHistoryDto>> getWatchHistory(Long userId);
}
