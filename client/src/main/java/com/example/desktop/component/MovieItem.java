package com.example.desktop.component;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.api.MovieApi;
import com.example.desktop.model.Movie;
import com.formdev.flatlaf.icons.FlatMenuArrowIcon;
import net.miginfocom.swing.MigLayout;
import raven.extras.AvatarIcon;
import raven.modal.ModalDialog;

import javax.swing.*;
import java.awt.*;

public class MovieItem extends JButton {
    private final AuthApiClient.Movie movie;
    private JLabel posterLabel;

    public MovieItem(AuthApiClient.Movie movie) {
        this.movie = movie;
        init();
        loadPoster();
    }

    private void init() {
        // SỬA LỖI Ở ĐÂY: Thêm "aligny top"
        // Lệnh này buộc TẤT CẢ nội dung (poster, text)
        // phải được căn lên trên cùng của JButton cha.
        setLayout(new MigLayout(
                "insets 8, aligny top", // Thêm "aligny top" vào đây
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

        // "top" ở đây để poster căn trên so với 2 dòng text
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

        addActionListener(e -> {
            System.out.println("Clicked movie: " + movie.title());
            ModalDialog.closeModal("search");
        });
    }

    private void loadPoster() {
        if (movie.posterUrl() != null && !movie.posterUrl().isEmpty()) {
            Movie movieModel = new Movie(
                    movie.id(),
                    movie.title(),
                    movie.description(),
                    movie.year(),
                    movie.durationMin(),
                    null,
                    null,
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