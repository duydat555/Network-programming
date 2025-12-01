package com.example.hls_server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientInfo {
    private String ipAddress;
    private LocalDateTime lastActivity;
    private List<String> recentSegments;
    private String currentSegment;
    private int totalRequests;
    private String currentQuality;

    public ClientInfo(String ipAddress) {
        this.ipAddress = ipAddress;
        this.lastActivity = LocalDateTime.now();
        this.recentSegments = new ArrayList<>();
        this.totalRequests = 0;
        this.currentQuality = "N/A";
    }

    public void addSegment(String segment) {
        this.currentSegment = segment;
        this.lastActivity = LocalDateTime.now();
        this.totalRequests++;

        // Extract quality from segment path
        extractQualityFromSegment(segment);

        // Keep only last 10 segments
        if (!recentSegments.contains(segment)) {
            recentSegments.add(0, segment);
            if (recentSegments.size() > 10) {
                recentSegments.remove(recentSegments.size() - 1);
            }
        }
    }

    private void extractQualityFromSegment(String segment) {
        if (segment == null) return;

        // Try to extract quality from path like: /hls/movie1/720p/segment0.ts
        String[] parts = segment.split("/");
        for (String part : parts) {
            if (part.matches("\\d+p")) { // Match patterns like 360p, 720p, 1080p
                this.currentQuality = part;
                return;
            }
        }
    }

    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public List<String> getRecentSegments() {
        return new ArrayList<>(recentSegments);
    }

    public String getCurrentSegment() {
        return currentSegment;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public String getCurrentQuality() {
        return currentQuality;
    }

    public boolean isActive() {
        return java.time.Duration.between(lastActivity, LocalDateTime.now()).getSeconds() < 10;
    }

    public long getInactiveSeconds() {
        return java.time.Duration.between(lastActivity, LocalDateTime.now()).getSeconds();
    }
}

