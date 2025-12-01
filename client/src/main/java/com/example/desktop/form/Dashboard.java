package com.example.desktop.form;

import com.example.desktop.api.FavoriteApi;
import com.example.desktop.api.MovieApi;
import com.example.desktop.component.CardMovie;
import com.example.desktop.layout.WrapLayout;
import com.example.desktop.model.Movie;
import com.example.desktop.system.AllForms;
import com.example.desktop.system.Form;
import com.example.desktop.system.FormManager;
// Import VideoPlayerForm để phát video
import com.example.desktop.form.VideoPlayerForm;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Dashboard extends Form {

    private JPanel contentPanel;
    private JScrollPane scroll;

    // TODO: Replace with actual logged-in user ID from authentication system
    private static final Long CURRENT_USER_ID = 1L;

    public Dashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(242, 242, 242));

        // Tạo panel chứa movies với layout hỗ trợ cuộn dọc
        contentPanel = new JPanel();
        contentPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
        contentPanel.setBackground(new Color(242, 242, 242));

        scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(scroll, BorderLayout.CENTER);

        // Load movies from API
        loadMovies();
    }

    private void loadMovies() {
        // Show loading indicator
        contentPanel.removeAll();
        JLabel loadingLabel = new JLabel("Đang tải phim...");
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD, 16f));
        contentPanel.add(loadingLabel);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Fetch movies in a background thread to avoid blocking UI
        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                return MovieApi.getMovies();
            }

            @Override
            protected void done() {
                try {
                    List<Movie> movies = get();
                    displayMovies(movies);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Lỗi khi tải danh sách phim: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayMovies(List<Movie> movies) {
        contentPanel.removeAll();

        if (movies == null || movies.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có phim nào để hiển thị");
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.BOLD, 16f));
            contentPanel.add(emptyLabel);
        } else {
            for (Movie movie : movies) {
                CardMovie card = new CardMovie(movie, m -> {
                    showMovieDetails(m);
                });
                contentPanel.add(card);
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showMovieDetails(Movie movie) {
        MovieDetailForm detailForm = (MovieDetailForm) AllForms.getForm(MovieDetailForm.class);
        detailForm.bindMovie(movie);
        detailForm.setBackgroundUrl(movie.getBackdropUrl());

        detailForm.setOnWatchTrailer(m -> JOptionPane.showMessageDialog(
                FormManager.getFrame(),
                "Đang mở trailer cho: " + m.getTitle(),
                "Trailer",
                JOptionPane.INFORMATION_MESSAGE));

        // --- ĐOẠN ĐÃ SỬA LỖI ---
        detailForm.setOnWatchMovie(m -> {
            // Kiểm tra danh sách video
            if (m.getVideoQualities() == null || m.getVideoQualities().isEmpty()) {
                JOptionPane.showMessageDialog(
                        FormManager.getFrame(),
                        "Phim chưa có link video!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Gọi VideoPlayerForm để phát video
            VideoPlayerForm playerForm = (VideoPlayerForm) AllForms.getForm(VideoPlayerForm.class);
            // Truyền toàn bộ danh sách chất lượng vào player
            playerForm.setMovie(m.getTitle(), m.getVideoQualities());

            // Hiển thị form
            FormManager.showForm(playerForm);
        });
        // -----------------------

        // Toggle favorite functionality with API integration
        detailForm.setOnToggleFavorite(m -> {
            toggleFavorite(m, detailForm);
        });

        // Check and update favorite state asynchronously
        checkFavoriteState(movie, detailForm);

        FormManager.showForm(detailForm);
    }

    /**
     * Toggle favorite status for a movie
     */
    private void toggleFavorite(Movie movie, MovieDetailForm detailForm) {
        // Perform API call in background thread
        SwingWorker<FavoriteApi.ApiResponse, Void> worker = new SwingWorker<>() {
            private boolean currentState;

            @Override
            protected FavoriteApi.ApiResponse doInBackground() throws Exception {
                // First check current state
                currentState = FavoriteApi.isFavorite(CURRENT_USER_ID, movie.getId());

                // Toggle the state
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
                        // Update UI state
                        detailForm.updateFavoriteState(!currentState);

                        // Show success message
                        String message = currentState ?
                                "Đã xóa khỏi danh sách yêu thích: " + movie.getTitle() :
                                "Đã thêm vào danh sách yêu thích: " + movie.getTitle();

                        JOptionPane.showMessageDialog(
                                FormManager.getFrame(),
                                message,
                                "Yêu thích",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Show error message from API
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

    /**
     * Check if movie is in favorites and update UI
     */
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
                    // If check fails, default to false
                    detailForm.updateFavoriteState(false);
                    System.err.println("Lỗi khi kiểm tra trạng thái favorite: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        contentPanel.removeAll();
        JLabel errorLabel = new JLabel("<html><div style='text-align: center;'>" +
                message +
                "<br><br>Vui lòng thử lại sau</div></html>");
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD, 14f));
        errorLabel.setForeground(Color.RED);
        contentPanel.add(errorLabel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Method to refresh the movie list
    public void refresh() {
        loadMovies();
    }

    @Override
    public void formRefresh() {
        loadMovies();
    }
}