package com.example.desktop.test;

import com.example.desktop.api.FavoriteApi;
import com.example.desktop.model.Movie;

import java.util.List;

/**
 * Test class for FavoritesForm functionality
 */
public class FavoritesFormTest {

    public static void main(String[] args) {
        Long testUserId = 1L;

        System.out.println("=== Testing Favorites API for FavoritesForm ===\n");

        try {
            // Test: Get all favorites
            System.out.println("Test: Getting all favorites for user " + testUserId);
            List<Movie> favorites = FavoriteApi.getFavorites(testUserId);

            if (favorites.isEmpty()) {
                System.out.println("No favorites found.");
            } else {
                System.out.println("Found " + favorites.size() + " favorite(s):");
                for (int i = 0; i < favorites.size(); i++) {
                    Movie movie = favorites.get(i);
                    System.out.println((i + 1) + ". " + movie.getTitle() +
                                     " (ID: " + movie.getId() + ")");
                    System.out.println("   Poster URL: " + movie.getPosterUrl());
                }
            }

            System.out.println("\n=== Test completed successfully! ===");

        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

