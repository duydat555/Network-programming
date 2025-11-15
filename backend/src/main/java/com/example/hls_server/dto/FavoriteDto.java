package com.example.hls_server.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDto {
    private Long movieId;
    private String title;
    private String posterUrl;
}

