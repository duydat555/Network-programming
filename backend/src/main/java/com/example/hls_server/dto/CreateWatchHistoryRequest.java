package com.example.hls_server.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWatchHistoryRequest {
    private Long movieId;
    private Integer positionSec;
}

