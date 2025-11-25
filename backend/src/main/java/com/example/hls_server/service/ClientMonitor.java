package com.example.hls_server.service;

import com.example.hls_server.model.ClientInfo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientMonitor {
    private final ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public void trackClient(String ipAddress, String file) {
        ClientInfo client = clients.computeIfAbsent(ipAddress, ClientInfo::new);

        if (file != null && (file.endsWith(".ts") || file.endsWith(".m4s"))) {
            client.addSegment(file);
        } else {
            client.updateActivity();
        }
    }

    public void cleanupInactiveClients() {
        clients.entrySet().removeIf(entry -> !entry.getValue().isActive());
    }

    public List<ClientInfo> getActiveClients() {
        return new ArrayList<>(clients.values());
    }

    public int getActiveClientCount() {
        return clients.size();
    }
}

