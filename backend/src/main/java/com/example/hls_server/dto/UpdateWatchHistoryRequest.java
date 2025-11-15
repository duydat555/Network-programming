package com.example.hls_server.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWatchHistoryRequest {
    private Long movieId;
    private Integer positionSec;
}
