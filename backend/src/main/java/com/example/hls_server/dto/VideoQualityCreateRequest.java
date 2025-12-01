package com.example.hls_server.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoQualityCreateRequest {
    private String quality;
    private String videoUrl;
    private Boolean isDefault;
}

