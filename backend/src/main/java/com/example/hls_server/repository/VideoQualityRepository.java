package com.example.hls_server.repository;

import com.example.hls_server.entity.VideoQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoQualityRepository extends JpaRepository<VideoQuality, Long> {
    List<VideoQuality> findByMovieId(Long movieId);
    Optional<VideoQuality> findByMovieIdAndIsDefaultTrue(Long movieId);
    List<VideoQuality> findByMovieIdOrderByQualityAsc(Long movieId);
}

