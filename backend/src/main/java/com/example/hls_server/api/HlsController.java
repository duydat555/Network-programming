package com.example.hls_server.api;


import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/hls/movie1")
public class HlsController {
    public static final long CHUNK_SIZE = 6024 * 6024;

    @GetMapping("/{folder}/{file}")
    public ResponseEntity<?> get(@PathVariable String folder, @PathVariable String file,
                                        @RequestHeader(value = "Range", required = false) String range) throws IOException {
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

    private MediaType guess(String name) {
        if (name.endsWith(".m3u8")) return MediaType.parseMediaType("application/vnd.apple.mpegurl");
        if (name.endsWith(".ts"))   return MediaType.parseMediaType("video/mp2t");
        if (name.endsWith(".m4s"))  return MediaType.parseMediaType("video/iso.segment");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
