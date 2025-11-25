package com.example.hls_server.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDto {
    private Long id;
    private String title;
    private String description;
    private Integer year;
    private Integer durationMin;
    private double rating;
    private String videoUrl;
    private String posterUrl;
    private String backdropUrl;
    private List<String> genres;
}
