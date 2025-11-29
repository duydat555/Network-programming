package com.example.desktop.component;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.api.MovieApi;
import com.example.desktop.form.MovieDetailForm;
import com.example.desktop.model.Movie;
import com.example.desktop.system.AllForms;
import com.example.desktop.system.FormManager;
import com.formdev.flatlaf.icons.FlatMenuArrowIcon;
import net.miginfocom.swing.MigLayout;
import raven.extras.AvatarIcon;
import raven.modal.ModalDialog;

import javax.swing.*;

public class MovieItem extends JButton {
    private final AuthApiClient.Movie movie;
    private JLabel posterLabel;

    public MovieItem(AuthApiClient.Movie movie) {
        this.movie = movie;
        init();
        loadPoster();
    }

    private void init() {
        setLayout(new MigLayout(
                "insets 8, aligny top",
                "[60!][grow,fill]push[]",
                "[][]"
        ));

        setFocusable(false);
        setHorizontalAlignment(10);
        putClientProperty("FlatLaf.style", "background:null;arc:10;borderWidth:0;focusWidth:0;innerFocusWidth:0;[light]selectedBackground:lighten($Button.selectedBackground,9%)");

        posterLabel = new JLabel(new AvatarIcon(
                getClass().getResource("/images/file.svg"), 60, 90, 8
        ));
        posterLabel.setBorder(null);
        posterLabel.setOpaque(false);
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(posterLabel, "cell 0 0 1 2, top");

        JLabel titleLabel = new JLabel(movie.title());
        titleLabel.putClientProperty("FlatLaf.style", "font:bold");
        add(titleLabel, "cell 1 0, wmin 0, wrap");

        String subtext = movie.year() + " • " + movie.durationMin() + "m";
        if (!movie.genres().isEmpty()) {
            subtext += " • " + String.join(", ", movie.genres());
        }
        JLabel subtextLabel = new JLabel(subtext);
        subtextLabel.putClientProperty("FlatLaf.style", "font: -2; foreground:$Label.disabledForeground;");
        add(subtextLabel, "cell 1 1, wmin 0, wrap");

        add(new JLabel(new FlatMenuArrowIcon()), "cell 2 0 1 2, east");

        // --- SỰ KIỆN CLICK ---
        addActionListener(e -> {
            ModalDialog.closeModal("search");

            // SỬA LẠI ĐOẠN NÀY: Truyền đúng videoUrl và backdropUrl
            Movie movieModel = new Movie(
                    movie.id(),
                    movie.title(),
                    movie.description(),
                    movie.year(),
                    movie.durationMin(),
                    movie.rating(),
                    movie.videoUrl(),     // Vị trí tham số 7: Truyền Link phim
                    movie.backdropUrl(),  // Vị trí tham số 8: Truyền Ảnh nền
                    movie.posterUrl(),
                    movie.genres()
            );

            MovieDetailForm detailForm = (MovieDetailForm) AllForms.getForm(MovieDetailForm.class);
            detailForm.bindMovie(movieModel);
            FormManager.showForm(detailForm);
        });
    }

    private void loadPoster() {
        if (movie.posterUrl() != null && !movie.posterUrl().isEmpty()) {
            // SỬA LẠI ĐOẠN NÀY CHO ĐỒNG BỘ
            Movie movieModel = new Movie(
                    movie.id(),
                    movie.title(),
                    movie.description(),
                    movie.year(),
                    movie.durationMin(),
                    movie.rating(),
                    movie.videoUrl(),    // Link phim
                    movie.backdropUrl(), // Ảnh nền
                    movie.posterUrl(),
                    movie.genres()
            );

            MovieApi.loadPosterAsync(movieModel, () -> {
                posterLabel.setIcon(new AvatarIcon(movieModel.getPoster(), 60, 90, 8));
                posterLabel.revalidate();
                posterLabel.repaint();
            });
        }
    }
}