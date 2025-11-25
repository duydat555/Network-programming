package com.example.hls_server.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    private String description;

    private Integer year;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "rating")
    private double rating;

    @Lob
    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Lob
    @Column(name = "poster_url")
    private String posterUrl;

    @Lob
    @Column(name = "backdrop_url")
    private String backdropUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "movie")
    private List<WatchHistory> watchHistoryList;

    @OneToMany(mappedBy = "movie")
    private List<Favorite> favorites;
}