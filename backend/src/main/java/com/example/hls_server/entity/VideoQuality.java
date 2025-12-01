package com.example.hls_server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video_qualities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false, length = 50)
    private String quality; // e.g., "360p", "480p", "720p", "1080p", "4K"

    @Lob
    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "is_default")
    private Boolean isDefault; // Default quality to play
}

