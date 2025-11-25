package com.example.desktop.api;

import com.example.desktop.model.Movie;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class FavoriteApi {

    private static final String BASE_URL = "http://192.168.12.197:8080";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Add a movie to user's favorites
     * @param userId User ID
     * @param movieId Movie ID
     * @return Response message
     */
    public static ApiResponse addFavorite(Long userId, Long movieId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId + "/favorites/" + movieId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return parseResponse(response);
    }

    /**
     * Remove a movie from user's favorites
     * @param userId User ID
     * @param movieId Movie ID
     * @return Response message
     */
    public static ApiResponse removeFavorite(Long userId, Long movieId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId + "/favorites/" + movieId))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return parseResponse(response);
    }

    /**
     * Check if a movie is in user's favorites
     * @param userId User ID
     * @param movieId Movie ID
     * @return true if movie is in favorites, false otherwise
     */
    public static boolean isFavorite(Long userId, Long movieId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId + "/favorites"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            return false;
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode dataNode = root.path("data");

        if (dataNode.isArray()) {
            for (JsonNode favorite : dataNode) {
                Long favMovieId = favorite.path("movieId").asLong();
                if (favMovieId.equals(movieId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get all favorite movies for a user
     * @param userId User ID
     * @return List of favorite movies
     */
    public static List<Movie> getFavorites(Long userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId + "/favorites"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Movie> favorites = new ArrayList<>();

        if (response.statusCode() / 100 != 2) {
            return favorites;
        }

        JsonNode root = mapper.readTree(response.body());
        boolean success = root.path("success").asBoolean(false);

        if (!success) {
            return favorites;
        }

        JsonNode dataNode = root.path("data");

        if (dataNode.isArray()) {
            for (JsonNode favorite : dataNode) {
                Long movieId = favorite.path("movieId").asLong();
                String title = favorite.path("title").asText();
                String posterUrl = favorite.path("posterUrl").asText();

                // Create Movie object from favorite data
                Movie movie = new Movie();
                movie.setId(movieId);
                movie.setTitle(title);
                movie.setPosterUrl(posterUrl);

                favorites.add(movie);
            }
        }

        return favorites;
    }

    /**
     * Parse API response
     */
    private static ApiResponse parseResponse(HttpResponse<String> response) throws IOException {
        JsonNode root = mapper.readTree(response.body());

        boolean success = root.path("success").asBoolean(false);
        String message = root.path("message").asText("Unknown error");

        return new ApiResponse(success, message);
    }

    /**
     * Simple API response class
     */
    public static class ApiResponse {
        private final boolean success;
        private final String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}

