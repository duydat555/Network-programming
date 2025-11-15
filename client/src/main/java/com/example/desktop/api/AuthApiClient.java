package com.example.desktop.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthApiClient {

    private static final String BASE_URL = "http://192.168.1.7:8080";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

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
                return new LoginResult(false, "HTTP " + response.statusCode(), null);
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
                        data.path("email").asText("")
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

    public record ApiResult(boolean success, String message) {}

    public record UserInfo(long id, String username, String email) {}

    public record LoginResult(boolean success, String message, UserInfo user) {}

}
