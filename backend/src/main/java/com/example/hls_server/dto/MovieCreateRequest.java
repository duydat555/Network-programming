package com.example.hls_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieCreateRequest {
    private String title;
    private String description;
    private Integer year;
    private Integer durationMin;
    private double rating;
    private List<VideoQualityCreateRequest> videoQualities;
    private String posterUrl;
    private String backdropUrl;
    private List<Long> genreIds;
}
