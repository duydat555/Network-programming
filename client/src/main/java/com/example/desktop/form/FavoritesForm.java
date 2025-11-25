package com.example.desktop.form;

import com.example.desktop.api.FavoriteApi;
import com.example.desktop.component.CardMovie;
import com.example.desktop.layout.WrapLayout;
import com.example.desktop.model.Movie;
import com.example.desktop.system.AllForms;
import com.example.desktop.system.Form;
import com.example.desktop.system.FormManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FavoritesForm extends Form {

    private JPanel contentPanel;
    private JScrollPane scroll;

    // TODO: Replace with actual logged-in user ID from authentication system
    private static final Long CURRENT_USER_ID = 1L;

    public FavoritesForm() {
        setLayout(new BorderLayout());
        setBackground(new Color(242, 242, 242));

        // Header panel with title
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create panel to hold favorite movies with wrap layout
        contentPanel = new JPanel();
        contentPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
        contentPanel.setBackground(new Color(242, 242, 242));

        scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(scroll, BorderLayout.CENTER);

        // Load favorites from API
        loadFavorites();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(242, 242, 242));
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("Phim yêu thích của tôi");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));

        header.add(titleLabel, BorderLayout.WEST);

        return header;
    }

    private void loadFavorites() {
        // Show loading indicator
        contentPanel.removeAll();
        JLabel loadingLabel = new JLabel("Đang tải danh sách yêu thích...");
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD, 16f));
        contentPanel.add(loadingLabel);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Fetch favorites asynchronously
        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                return FavoriteApi.getFavorites(CURRENT_USER_ID);
            }

            @Override
            protected void done() {
                try {
                    List<Movie> favorites = get();
                    displayFavorites(favorites);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Không thể tải danh sách yêu thích: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayFavorites(List<Movie> favorites) {
        contentPanel.removeAll();

        if (favorites.isEmpty()) {
            JLabel emptyLabel = new JLabel("Bạn chưa có phim yêu thích nào");
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.BOLD, 16f));
            emptyLabel.setForeground(Color.GRAY);
            contentPanel.add(emptyLabel);
        } else {
            for (Movie movie : favorites) {
                CardMovie card = new CardMovie(movie, this::showMovieDetails);
                contentPanel.add(card);
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showMovieDetails(Movie favoriteMovie) {
        // Since favorite API only returns basic info (id, title, posterUrl),
        // we need to fetch full movie details from MovieApi
        SwingWorker<Movie, Void> worker = new SwingWorker<>() {
            @Override
            protected Movie doInBackground() throws Exception {
                // Fetch full movie details using movie ID
                return fetchFullMovieDetails(favoriteMovie.getId());
            }

            @Override
            protected void done() {
                try {
                    Movie fullMovie = get();
                    if (fullMovie != null) {
                        showMovieDetailForm(fullMovie);
                    } else {
                        // If full details not available, show with limited info
                        showMovieDetailForm(favoriteMovie);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback to showing limited info
                    showMovieDetailForm(favoriteMovie);
                }
            }
        };
        worker.execute();
    }

    private Movie fetchFullMovieDetails(Long movieId) {
        // This method should call MovieApi to get full movie details
        // For now, we'll return null and you can implement it later
        // TODO: Implement MovieApi.getMovieById(movieId)
        return null;
    }

    private void showMovieDetailForm(Movie movie) {
        MovieDetailForm detailForm = (MovieDetailForm) AllForms.getForm(MovieDetailForm.class);
        detailForm.bindMovie(movie);
        detailForm.setBackgroundUrl(movie.getPosterUrl());

        detailForm.setOnWatchTrailer(m -> JOptionPane.showMessageDialog(
                FormManager.getFrame(),
                "Đang mở trailer cho: " + m.getTitle(),
                "Trailer",
                JOptionPane.INFORMATION_MESSAGE));

        detailForm.setOnWatchMovie(m -> JOptionPane.showMessageDialog(
                FormManager.getFrame(),
                "Bắt đầu xem phim: " + m.getVideoUrl(),
                "Xem phim",
                JOptionPane.INFORMATION_MESSAGE));

        // Toggle favorite functionality with refresh on change
        detailForm.setOnToggleFavorite(m -> {
            toggleFavorite(m, detailForm);
        });

        // Check and update favorite state
        checkFavoriteState(movie, detailForm);

        FormManager.showForm(detailForm);
    }

    private void toggleFavorite(Movie movie, MovieDetailForm detailForm) {
        SwingWorker<FavoriteApi.ApiResponse, Void> worker = new SwingWorker<>() {
            private boolean currentState;

            @Override
            protected FavoriteApi.ApiResponse doInBackground() throws Exception {
                currentState = FavoriteApi.isFavorite(CURRENT_USER_ID, movie.getId());

                if (currentState) {
                    return FavoriteApi.removeFavorite(CURRENT_USER_ID, movie.getId());
                } else {
                    return FavoriteApi.addFavorite(CURRENT_USER_ID, movie.getId());
                }
            }

            @Override
            protected void done() {
                try {
                    FavoriteApi.ApiResponse response = get();

                    if (response.isSuccess()) {
                        detailForm.updateFavoriteState(!currentState);

                        String message = currentState ?
                            "Đã xóa khỏi danh sách yêu thích: " + movie.getTitle() :
                            "Đã thêm vào danh sách yêu thích: " + movie.getTitle();

                        JOptionPane.showMessageDialog(
                            FormManager.getFrame(),
                            message,
                            "Yêu thích",
                            JOptionPane.INFORMATION_MESSAGE);

                        // Refresh the favorites list after removing
                        if (currentState) {
                            refresh();
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                            FormManager.getFrame(),
                            "Lỗi: " + response.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        FormManager.getFrame(),
                        "Lỗi kết nối: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void checkFavoriteState(Movie movie, MovieDetailForm detailForm) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return FavoriteApi.isFavorite(CURRENT_USER_ID, movie.getId());
            }

            @Override
            protected void done() {
                try {
                    boolean isFavorite = get();
                    detailForm.updateFavoriteState(isFavorite);
                } catch (Exception e) {
                    detailForm.updateFavoriteState(false);
                    System.err.println("Lỗi khi kiểm tra trạng thái favorite: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        contentPanel.removeAll();
        JLabel errorLabel = new JLabel(message);
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD, 16f));
        errorLabel.setForeground(Color.RED);
        contentPanel.add(errorLabel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void refresh() {
        loadFavorites();
    }

    @Override
    public void formRefresh() {
        loadFavorites();
    }
}

