package com.example.hls_server.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistoryDto {
    private Long movieId;
    private String movieTitle;
    private Integer positionSec;
    private LocalDateTime updatedAt;
}

