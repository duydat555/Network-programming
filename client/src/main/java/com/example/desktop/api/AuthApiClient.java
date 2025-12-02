package com.example.desktop.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature; // Import thêm để cấu hình mapper

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

public class AuthApiClient {
    private static final String BASE_URL = "http://192.168.12.134:8080";

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final HttpClient client = HttpClient.newHttpClient();

    public static ApiResult register(String username, String email, String password) {
        try {
            String json = String.format(
                    "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    escapeJson(username),
                    escapeJson(email),
                    escapeJson(password)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                return new ApiResult(false, "HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            boolean success = root.path("success").asBoolean(false);
            String message = root.path("message").asText("");

            return new ApiResult(success, message);

        } catch (Exception ex) {
            ex.printStackTrace();
            return new ApiResult(false, "Lỗi kết nối server: " + ex.getMessage());
        }
    }

    public static LoginResult login(String email, String password) {
        try {
            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    escapeJson(email),
                    escapeJson(password)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                String message = "HTTP " + response.statusCode();
                try {
                    JsonNode root = mapper.readTree(response.body());
                    message = root.path("message").asText(message);
                } catch (Exception e) {
                }
                return new LoginResult(false, message, null);
            }

            JsonNode root = mapper.readTree(response.body());
            boolean success = root.path("success").asBoolean(false);
            String message = root.path("message").asText("");

            UserInfo user = null;

            if (success) {
                JsonNode data = root.path("data");
                user = new UserInfo(
                        data.path("id").asLong(),
                        data.path("username").asText(""),
                        data.path("email").asText(""),
                        data.path("role").asText("USER")
                );
            }

            return new LoginResult(success, message, user);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new LoginResult(false, "Lỗi kết nối: " + ex.getMessage(), null);
        }
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    public static List<UserInfo> getUsers() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users"))
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

        if (dataNode.isMissingNode() || !dataNode.isArray()) {
            if(root.isArray()){
                return mapper.convertValue(root, new TypeReference<List<UserInfo>>() {});
            }
            return Collections.emptyList();
        }

        return mapper.convertValue(dataNode, new TypeReference<List<UserInfo>>() {});
    }

    // --- Cập nhật logic lấy phim để khớp với Record VideoQuality ---
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

        JsonNode root = mapper.readTree(response.body());
        JsonNode dataNode = root.path("data");

        // Logic cũ của bạn: convert thẳng sang List<Movie>
        // Jackson sẽ tự động map mảng JSON vào List<VideoQuality>
        if (dataNode.isMissingNode() || !dataNode.isArray()) {
            if(root.isArray()){
                return mapper.convertValue(root, new TypeReference<List<Movie>>() {});
            }
            return Collections.emptyList();
        }

        return mapper.convertValue(dataNode, new TypeReference<List<Movie>>() {});
    }

    // --- (Giữ nguyên create/update/delete/search nhưng chúng sẽ dùng NewMovie mới) ---

    public static boolean createMovie(NewMovie movie) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(movie);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies")) // Lưu ý: API tạo mới thường là POST
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 == 2) {
            return true;
        } else {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    public static boolean updateMovie(long movieId, NewMovie movie) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(movie);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies/" + movieId)) // Lưu ý: API sửa thường là PUT
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 == 2) {
            return true;
        } else {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    public static boolean deleteMovie(long movieId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies/" + movieId))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 == 2) {
            return true;
        } else {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    public static List<Movie> searchMovies(String keyword) throws IOException, InterruptedException {
        String encodedKeyword;
        try {
            encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        String url = BASE_URL + "/api/movies/search?keyword=" + encodedKeyword;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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

        if (dataNode.isMissingNode() || !dataNode.isArray()) {
            if(root.isArray()){
                return mapper.convertValue(root, new TypeReference<List<Movie>>() {});
            }
            return Collections.emptyList();
        }

        return mapper.convertValue(dataNode, new TypeReference<List<Movie>>() {});
    }

    // =========================================================================
    // PHẦN CẦN SỬA ĐỔI CHÍNH LÀ Ở ĐÂY
    // =========================================================================

    public record ApiResult(boolean success, String message) {}

    public record UserInfo(long id, String username, String email, String role) {}

    public record LoginResult(boolean success, String message, UserInfo user) {}

    // 1. Thêm Record VideoQuality để hứng dữ liệu JSON object trong mảng
    public record VideoQuality(
            int id,
            String quality,
            String videoUrl,
            boolean isDefault
    ) {}

    // 2. Sửa Record Movie: videoQualities đổi thành List<VideoQuality>
    public record Movie(
            int id,
            String title,
            String description,
            int year,
            int durationMin,
            double rating,
            List<VideoQuality> videoQualities, // <-- Đã sửa: dùng List Object thay vì List String
            String backdropUrl,
            String posterUrl,
            List<String> genres
    ) {}

    // 3. Sửa Record NewMovie: videoQualities đổi thành String
    // Lý do: MovieDialog đang gửi lên một link duy nhất (String) khi tạo/sửa
    public record NewMovie(
            String title,
            String description,
            int year,
            int durationMin,
            String videoQualities, // <-- Đã sửa: dùng String để khớp với constructor bên MovieDialog
            String backdropUrl,
            String posterUrl,
            List<String> genres
    ) {}
}