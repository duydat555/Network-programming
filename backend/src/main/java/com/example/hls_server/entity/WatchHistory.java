package com.example.hls_server.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watch_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // movie_id
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "position_sec", nullable = false)
    private Integer positionSec;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
