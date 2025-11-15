package com.example.desktop.model;

import javax.swing.*;

public class Movie {

    private String name;
    private String description;
    private ImageIcon poster;

    public Movie(String name, String description, ImageIcon poster) {
        this.name = name;
        this.description = description;
        this.poster = poster;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ImageIcon getPoster() {
        return poster;
    }

    public void setPoster(ImageIcon poster) {
        this.poster = poster;
    }
}

