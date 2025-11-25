package com.example.desktop.model;

import javax.swing.*;
import java.util.List;

public class Movie {

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
    private ImageIcon poster;
    private ImageIcon backdrop;

    public Movie() {
    }

    public Movie(Long id, String title, String description, Integer year, Integer durationMin, double rating,
                 String videoUrl, String backdropUrl, String posterUrl, List<String> genres) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.year = year;
        this.durationMin = durationMin;
        this.rating = rating;
        this.videoUrl = videoUrl;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.genres = genres;
    }

    // Keep old constructor for backward compatibility
    public Movie(String name, String description, ImageIcon poster, ImageIcon backdrop) {
        this.title = name;
        this.description = description;
        this.poster = poster;
        this.backdrop = backdrop;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Keep getName for backward compatibility
    public String getName() {
        return title;
    }

    public void setName(String name) {
        this.title = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(Integer durationMin) {
        this.durationMin = durationMin;
    }

    public double getRating() {return rating;}

    public void setRating(double rating) {this.rating = rating;}

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getBackdropUrl() { return backdropUrl; }

    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public ImageIcon getPoster() {
        return poster;
    }

    public void setPoster(ImageIcon poster) {
        this.poster = poster;
    }

}

