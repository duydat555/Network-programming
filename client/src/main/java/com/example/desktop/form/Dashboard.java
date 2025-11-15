package com.example.desktop.form;

import com.example.desktop.api.MovieApi;
import com.example.desktop.component.CardMovie;
import com.example.desktop.layout.WrapLayout;
import com.example.desktop.model.Movie;
import com.example.desktop.system.Form;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Dashboard extends Form {

    private JPanel contentPanel;
    private JScrollPane scroll;

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
        StringBuilder details = new StringBuilder();
        details.append("Tên phim: ").append(movie.getTitle()).append("\n");
        details.append("Mô tả: ").append(movie.getDescription()).append("\n");

        if (movie.getYear() != null) {
            details.append("Năm: ").append(movie.getYear()).append("\n");
        }

        if (movie.getDurationMin() != null) {
            details.append("Thời lượng: ").append(movie.getDurationMin()).append(" phút\n");
        }

        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            details.append("Thể loại: ").append(String.join(", ", movie.getGenres())).append("\n");
        }

        JOptionPane.showMessageDialog(
                Dashboard.this,
                details.toString(),
                "Chi tiết phim",
                JOptionPane.INFORMATION_MESSAGE
        );
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
}
