package com.example.desktop.api;

import com.example.desktop.model.Movie;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieApi {

    private static final String BASE_URL = "http://192.168.1.7:8080";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    // Cache để lưu ảnh đã tải
    private static final Map<String, ImageIcon> imageCache = new HashMap<>();

    // Kích thước poster chuẩn
    private static final int POSTER_WIDTH = 160;
    private static final int POSTER_HEIGHT = 220;

    /**
     * Fetch all movies from the API
     * @return List of Movie objects with loaded poster images
     */
    public static List<Movie> getMovies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }

        // Parse JSON response
        JsonNode root = mapper.readTree(response.body());

        // Check if response is successful
        boolean success = root.path("success").asBoolean(false);
        if (!success) {
            String message = root.path("message").asText("Failed to fetch movies");
            throw new IOException(message);
        }

        // Get data array
        JsonNode dataNode = root.path("data");

        if (dataNode.isMissingNode() || !dataNode.isArray()) {
            return Collections.emptyList();
        }

        // Convert JSON to Movie list
        List<Movie> movies = new ArrayList<>();
        for (JsonNode movieNode : dataNode) {
            Movie movie = new Movie();
            movie.setId(movieNode.path("id").asLong());
            movie.setTitle(movieNode.path("title").asText());
            movie.setDescription(movieNode.path("description").asText());
            movie.setYear(movieNode.path("year").asInt());
            movie.setDurationMin(movieNode.path("durationMin").asInt());
            movie.setRating(movieNode.path("rating").asDouble());
            movie.setVideoUrl(movieNode.path("videoUrl").asText());
            movie.setPosterUrl(movieNode.path("posterUrl").asText());
            movie.setBackdropUrl(movieNode.path("backdropUrl").asText());

            // Parse genres
            List<String> genres = new ArrayList<>();
            JsonNode genresNode = movieNode.path("genres");
            if (genresNode.isArray()) {
                for (JsonNode genreNode : genresNode) {
                    genres.add(genreNode.asText());
                }
            }
            movie.setGenres(genres);

            // Tải ảnh poster từ URL (có thể là URL internet hoặc local)
            // Không tải ảnh ở đây để tránh làm chậm, sẽ tải lazy trong CardMovie
            movie.setPoster(createPlaceholderIcon());

            movies.add(movie);
        }

        return movies;
    }

    /**
     * Load image from URL and return as ImageIcon with caching and resizing
     * Hỗ trợ tải ảnh từ URL internet (http/https)
     */
    public static ImageIcon loadImageFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return createPlaceholderIcon();
        }

        // Kiểm tra cache trước
        if (imageCache.containsKey(imageUrl)) {
            return imageCache.get(imageUrl);
        }

        try {
            System.out.println("Đang tải ảnh từ: " + imageUrl);

            // Tạo URL connection với timeout
            URL url = URI.create(imageUrl).toURL();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000); // 5 giây timeout
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Đọc ảnh từ stream
            try (InputStream inputStream = connection.getInputStream()) {
                BufferedImage originalImage = ImageIO.read(inputStream);

                if (originalImage != null) {
                    // Resize ảnh về kích thước chuẩn
                    BufferedImage resizedImage = resizeImage(originalImage, POSTER_WIDTH, POSTER_HEIGHT);
                    ImageIcon icon = new ImageIcon(resizedImage);

                    // Lưu vào cache
                    imageCache.put(imageUrl, icon);

                    System.out.println("Đã tải thành công ảnh: " + imageUrl);
                    return icon;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh từ: " + imageUrl);
            System.err.println("Chi tiết lỗi: " + e.getMessage());
            e.printStackTrace();
        }

        // Nếu tải thất bại, trả về placeholder
        ImageIcon placeholder = createPlaceholderIcon();
        imageCache.put(imageUrl, placeholder);
        return placeholder;
    }

    /**
     * Resize ảnh về kích thước mong muốn với tỷ lệ đúng
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * Create a placeholder icon when image loading fails
     */
    private static ImageIcon createPlaceholderIcon() {
        BufferedImage placeholder = new BufferedImage(160, 220, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(0, 0, 160, 220);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString("No Image", 50, 110);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }

    /**
     * Fetch a single movie by ID
     */
    public static Movie getMovieById(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies/" + id))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode dataNode = root.path("data");

        Movie movie = new Movie();
        movie.setId(dataNode.path("id").asLong());
        movie.setTitle(dataNode.path("title").asText());
        movie.setDescription(dataNode.path("description").asText());
        movie.setYear(dataNode.path("year").asInt());
        movie.setDurationMin(dataNode.path("durationMin").asInt());
        movie.setVideoUrl(dataNode.path("videoUrl").asText());
        movie.setPosterUrl(dataNode.path("posterUrl").asText());

        List<String> genres = new ArrayList<>();
        JsonNode genresNode = dataNode.path("genres");
        if (genresNode.isArray()) {
            for (JsonNode genreNode : genresNode) {
                genres.add(genreNode.asText());
            }
        }
        movie.setGenres(genres);

        try {
            ImageIcon posterIcon = loadImageFromUrl(movie.getPosterUrl());
            movie.setPoster(posterIcon);
        } catch (Exception e) {
            movie.setPoster(createPlaceholderIcon());
        }

        return movie;
    }

    /**
     * Tải ảnh poster cho movie bất đồng bộ
     * Gọi callback khi tải xong
     */
    public static void loadPosterAsync(Movie movie, Runnable onComplete) {
        if (movie == null || movie.getPosterUrl() == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        // Tải ảnh trong background thread
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                return loadImageFromUrl(movie.getPosterUrl());
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    movie.setPoster(icon);
                    if (onComplete != null) {
                        SwingUtilities.invokeLater(onComplete);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi tải ảnh async: " + e.getMessage());
                    movie.setPoster(createPlaceholderIcon());
                    if (onComplete != null) {
                        SwingUtilities.invokeLater(onComplete);
                    }
                }
            }
        };
        worker.execute();
    }
}
