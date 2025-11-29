package com.example.desktop.form;

import com.example.desktop.api.MovieApi;
import com.example.desktop.component.CardMovie;
import com.example.desktop.layout.WrapLayout;
import com.example.desktop.model.Movie;
import com.example.desktop.system.AllForms;
import com.example.desktop.system.Form;
import com.example.desktop.system.FormManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HorrorGenreForm extends Form {

    private JPanel contentPanel;
    private JScrollPane scroll;
    private static final Long GENRE_ID = 3L; // Kinh dị

    public HorrorGenreForm() {
        setLayout(new BorderLayout());
        setBackground(new Color(242, 242, 242));

        // Header panel with title
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create panel to hold movies with wrap layout
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

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(242, 242, 242));
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("Phim Kinh dị");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));

        header.add(titleLabel, BorderLayout.WEST);

        return header;
    }

    private void loadMovies() {
        // Show loading indicator
        contentPanel.removeAll();
        JLabel loadingLabel = new JLabel("Đang tải phim...");
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD, 16f));
        contentPanel.add(loadingLabel);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Fetch movies asynchronously
        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                return MovieApi.getMoviesByGenreId(GENRE_ID);
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
            emptyLabel.setForeground(Color.GRAY);
            contentPanel.add(emptyLabel);
        } else {
            for (Movie movie : movies) {
                CardMovie card = new CardMovie(movie, this::showMovieDetails);
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
                JOptionPane.INFORMATION_MESSAGE
        ));
        FormManager.showForm(detailForm);
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
}

