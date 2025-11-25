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

    public ClientInfo(String ipAddress) {
        this.ipAddress = ipAddress;
        this.lastActivity = LocalDateTime.now();
        this.recentSegments = new ArrayList<>();
        this.totalRequests = 0;
    }

    public void addSegment(String segment) {
        this.currentSegment = segment;
        this.lastActivity = LocalDateTime.now();
        this.totalRequests++;

        // Keep only last 10 segments
        if (!recentSegments.contains(segment)) {
            recentSegments.add(0, segment);
            if (recentSegments.size() > 10) {
                recentSegments.remove(recentSegments.size() - 1);
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

    public boolean isActive() {
        return java.time.Duration.between(lastActivity, LocalDateTime.now()).getSeconds() < 10;
    }

    public long getInactiveSeconds() {
        return java.time.Duration.between(lastActivity, LocalDateTime.now()).getSeconds();
    }
}

