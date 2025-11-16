package com.example.desktop.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.Collections;

public class AuthApiClient {

    private static final String BASE_URL = "http://192.168.102.208:8080";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    // --- CÁC HÀM XÁC THỰC (GIỮ NGUYÊN) ---

    public static ApiResult register(String username, String email, String password) {
        try {
            // Tạo JSON body
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

            // Parse JSON: { success, message, data: { id, username, email } }
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
                // Thử parse lỗi từ server
                String message = "HTTP " + response.statusCode();
                try {
                    JsonNode root = mapper.readTree(response.body());
                    message = root.path("message").asText(message);
                } catch (Exception e) {
                    // Bỏ qua
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
                        data.path("role").asText("USER") // Lấy 'role', mặc định là USER
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


    // --- CÁC HÀM API MỚI CHO ADMIN (ĐÃ SỬA) ---

    /**
     * Lấy danh sách TẤT CẢ người dùng
     */
    public static List<UserInfo> getUsers() throws IOException, InterruptedException {
        // TODO: SỬA LẠI URL NÀY! Đường dẫn "/api/users" của bạn đang bị 404 Not Found.
        // Hãy thay bằng đường dẫn API lấy user chính xác từ server của bạn.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users")) // <-- LỖI 404 ĐANG Ở ĐÂY
                .header("Content-Type", "application/json")
                // .header("Authorization", "Bearer " + token) // (Nếu cần xác thực)
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }

        // SỬA LỖI MismatchedInput:
        // 1. Đọc toàn bộ JSON object (bắt đầu bằng {...})
        JsonNode root = mapper.readTree(response.body());

        // 2. Giả sử danh sách nằm trong trường "data", giống như API login
        JsonNode dataNode = root.path("data");

        // 3. Chuyển đổi chỉ node "data" (là một mảng) thành List<UserInfo>
        if (dataNode.isMissingNode() || !dataNode.isArray()) {
            // Nếu không có "data", thử xem có phải root chính là mảng không
            if(root.isArray()){
                return mapper.convertValue(root, new TypeReference<List<UserInfo>>() {});
            }
            // Nếu không tìm thấy, trả về danh sách rỗng
            return Collections.emptyList();
        }

        return mapper.convertValue(dataNode, new TypeReference<List<UserInfo>>() {});
    }

    /**
     * Lấy danh sách TẤT CẢ phim
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

        // SỬA LỖI MismatchedInput:
        // 1. Đọc toàn bộ JSON object (bắt đầu bằng {...})
        JsonNode root = mapper.readTree(response.body());

        // 2. Giả sử danh sách nằm trong trường "data"
        JsonNode dataNode = root.path("data");

        // 3. Chuyển đổi chỉ node "data" (là một mảng) thành List<Movie>
        if (dataNode.isMissingNode() || !dataNode.isArray()) {
            // Nếu không có "data", thử xem có phải root chính là mảng không
            if(root.isArray()){
                return mapper.convertValue(root, new TypeReference<List<Movie>>() {});
            }
            // Nếu không tìm thấy, trả về danh sách rỗng
            return Collections.emptyList();
        }

        return mapper.convertValue(dataNode, new TypeReference<List<Movie>>() {});
    }

    /**
     * Tạo một phim mới
     */
    public static boolean createMovie(NewMovie movie) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(movie);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // *** SỬA ĐỔI ĐỂ XEM LỖI ***
        if (response.statusCode() / 100 == 2) {
            return true;
        } else {
            // Ném lỗi với thông báo của server
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    /**
     * Cập nhật một phim đã có
     */
    public static boolean updateMovie(long movieId, NewMovie movie) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(movie);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies/" + movieId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // *** SỬA ĐỔI ĐỂ XEM LỖI ***
        if (response.statusCode() / 100 == 2) {
            return true;
        } else {
            // Ném lỗi với thông báo của server
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    /**
     * Xóa một phim
     */
    public static boolean deleteMovie(long movieId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/movies/" + movieId))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // *** SỬA ĐỔI ĐỂ XEM LỖI ***
        if (response.statusCode() / 100 == 2) {
            return true;
        } else {
            // Ném lỗi với thông báo của server
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    /**
     * HÀM MỚI: Tìm kiếm phim theo từ khóa
     */
    public static List<Movie> searchMovies(String keyword) throws IOException, InterruptedException {
        // Mã hóa từ khóa để dùng trong URL (ví dụ: "avengers" -> "avengers")
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

        // Parse JSON (giống hệt hàm getMovies)
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


    // --- CÁC RECORD (DATA MODEL) ---

    public record ApiResult(boolean success, String message) {}

    // Cập nhật UserInfo để có "role" cho bảng Quản lý Người dùng
    public record UserInfo(long id, String username, String email, String role) {}

    public record LoginResult(boolean success, String message, UserInfo user) {}

    // Record cho Phim (khi GET, có ID)
    public record Movie(
            long id, // Dùng long để nhất quán
            String title,
            String description,
            int year,
            int durationMin,
            String videoUrl,
            String posterUrl,
            List<String> genres
    ) {}

    // Record cho Phim Mới (khi POST/PUT, không có ID)
    public record NewMovie(
            String title,
            String description,
            int year,
            int durationMin,
            String videoUrl,
            String posterUrl,
            List<String> genres
    ) {}
}