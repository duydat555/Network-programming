package com.example.desktop.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Movie {

    private Long id;
    private String title;
    private String description;
    private Integer year;
    private Integer durationMin;
    private double rating;

    // --- THAY ĐỔI QUAN TRỌNG: Dùng List<VideoQuality> thay vì List<String> ---
    private List<VideoQuality> videoQualities;

    private String posterUrl;
    private String backdropUrl;
    private List<String> genres;
    private ImageIcon poster;
    private ImageIcon backdrop;

    // --- Inner Class để lưu thông tin chất lượng ---
    public static class VideoQuality {
        private String quality; // Ví dụ: "1080p", "720p"
        private String videoUrl; // Link phim
        private boolean isDefault;

        public VideoQuality() {}

        public VideoQuality(String quality, String videoUrl) {
            this.quality = quality;
            this.videoUrl = videoUrl;
        }

        public String getQuality() { return quality; }
        public void setQuality(String quality) { this.quality = quality; }
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
        public boolean isDefault() { return isDefault; }
        public void setDefault(boolean aDefault) { isDefault = aDefault; }
    }

    public Movie() {
    }

    // Constructor cập nhật để nhận List<VideoQuality>
    public Movie(Long id, String title, String description, Integer year, Integer durationMin, double rating,
                 List<VideoQuality> videoQualities, String backdropUrl, String posterUrl, List<String> genres) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.year = year;
        this.durationMin = durationMin;
        this.rating = rating;
        this.videoQualities = videoQualities;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.genres = genres;
    }

    // Constructor cũ (giữ tương thích)
    public Movie(String name, String description, ImageIcon poster, ImageIcon backdrop) {
        this.title = name;
        this.description = description;
        this.poster = poster;
        this.backdrop = backdrop;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    // --- Getter/Setter cho VideoQualities (Kiểu mới) ---
    public List<VideoQuality> getVideoQualities() {
        return videoQualities;
    }

    public void setVideoQualities(List<VideoQuality> videoQualities) {
        this.videoQualities = videoQualities;
    }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public ImageIcon getPoster() { return poster; }
    public void setPoster(ImageIcon poster) { this.poster = poster; }

    // Helper: Lấy URL mặc định (để tương thích với code cũ nếu cần)
    public String getDefaultVideoUrl() {
        if (videoQualities != null && !videoQualities.isEmpty()) {
            return videoQualities.get(0).getVideoUrl();
        }
        return "";
    }
}