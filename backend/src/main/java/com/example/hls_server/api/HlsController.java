package com.example.hls_server.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
@RequestMapping("/hls/movie1")
public class HlsController {
    public static final long CHUNK_SIZE = 1024 * 1024;
    private final ConcurrentHashMap<String, Long> activeClients = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<String> printedClients = new CopyOnWriteArraySet<>();
    private int lastClientCount = 0;

    @GetMapping("/{folder}/{file}")
    public ResponseEntity<?> get(@PathVariable String folder, @PathVariable String file,
                                 @RequestHeader(value = "Range", required = false) String range,
                                 HttpServletRequest request) throws IOException {

        String clientIp = getClientIp(request);
        activeClients.put(clientIp, System.currentTimeMillis());
        cleanupInactiveClients();

        int currentCount = activeClients.size();
        if (currentCount != lastClientCount || !printedClients.contains(clientIp)) {
            System.out.println("=================================");
            System.out.println("Số client đang kết nối: " + currentCount);
            System.out.println("Client mới: " + clientIp);
            System.out.println("Danh sách: " + activeClients.keySet());
            System.out.println("=================================");

            printedClients.add(clientIp);
            lastClientCount = currentCount;
        }

        File target = new File("D:/hls/movie1/" + folder + "/" + file);
        if (!target.exists()) return ResponseEntity.notFound().build();

        FileSystemResource resource = new FileSystemResource(target);
        MediaType contentType = guess(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofHours(12)).cachePublic());
        headers.set("Accept-Ranges", "bytes");

        if(file.endsWith(".m3u8")) {
            return  ResponseEntity.ok()
                    .contentType(contentType)
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);
        }

        long fileLength = resource.contentLength();

        if(range == null || range.isEmpty()) {
            return  ResponseEntity.ok()
                    .contentType(contentType)
                    .headers(headers)
                    .contentLength(fileLength)
                    .body(resource);
        }

        long start = 0;
        try {
            String[] parts = range.replace("bytes=", "").split("-");
            start = Long.parseLong(parts[0]);
        } catch (Exception ignored) {}

        long end = Math.min(start + CHUNK_SIZE - 1, fileLength - 1);
        HttpRange httpRange = HttpRange.createByteRange(start, end);
        ResourceRegion region = new ResourceRegion(resource, start, end - start + 1);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(contentType)
                .headers(headers)
                .header(HttpHeaders.CONTENT_RANGE, httpRange.toString() + "/" + fileLength)
                .body(region);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private void cleanupInactiveClients() {
        long now = System.currentTimeMillis();
        activeClients.entrySet().removeIf(entry -> {
            boolean inactive = now - entry.getValue() > 30000;
            if (inactive) {
                printedClients.remove(entry.getKey());
            }
            return inactive;
        });
    }

    private MediaType guess(String name) {
        if (name.endsWith(".m3u8")) return MediaType.parseMediaType("application/vnd.apple.mpegurl");
        if (name.endsWith(".ts"))   return MediaType.parseMediaType("video/mp2t");
        if (name.endsWith(".m4s"))  return MediaType.parseMediaType("video/iso.segment");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
