package com.example.hls_server.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoQualityDto {
    private Long id;
    private String quality;
    private String videoUrl;
    private Boolean isDefault;
}

