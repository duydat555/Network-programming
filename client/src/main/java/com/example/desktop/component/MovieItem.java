package com.example.desktop.component;

import com.example.desktop.api.AuthApiClient;
import com.formdev.flatlaf.icons.FlatMenuArrowIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class MovieItem extends JButton {
    private final AuthApiClient.Movie movie;
    private JLabel posterLabel;

    public MovieItem(AuthApiClient.Movie movie) {
        this.movie = movie;
        init();
        loadPoster();
    }

    private void init() {
        // Layout giống ảnh: [Poster] [Thông tin] [Mũi tên]
        setLayout(new MigLayout("insets 5", "[40!][grow]push[]", "[grow]"));
        setFocusable(false);
        setHorizontalAlignment(10); // Left
        putClientProperty("FlatLaf.style", "background:null;arc:10;borderWidth:0;focusWidth:0;innerFocusWidth:0;[light]selectedBackground:lighten($Button.selectedBackground,9%)");

        // 1. Poster
        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(40, 60)); // Poster nhỏ
        posterLabel.setOpaque(true);
        posterLabel.setBackground(UIManager.getColor("TextField.background")); // Màu nền placeholder
        add(posterLabel, "cell 0 0 1 3, w 40!, h 60!"); // Span 3 hàng

        // 2. Thông tin
        JLabel titleLabel = new JLabel(movie.title());
        titleLabel.putClientProperty("FlatLaf.style", "font:bold");
        add(titleLabel, "cell 1 0, wrap");

        String subtext = movie.year() + " • " + movie.durationMin() + "m";
        JLabel yearLabel = new JLabel(subtext);
        yearLabel.putClientProperty("FlatLaf.style", "font: -2; foreground:$Label.disabledForeground;");
        add(yearLabel, "cell 1 1, wrap");

        String genres = String.join(", ", movie.genres());
        JLabel genreLabel = new JLabel(genres);
        genreLabel.putClientProperty("FlatLaf.style", "font: -2; foreground:$Label.disabledForeground;");
        add(genreLabel, "cell 1 2, wrap");

        // 3. Mũi tên
        add(new JLabel(new FlatMenuArrowIcon()), "cell 2 0 1 3, east"); // Căn phải

        // Sự kiện khi click
        addActionListener(e -> {
            System.out.println("Clicked movie: " + movie.title());
            // Tạm thời chỉ đóng dialog
            ModalDialog.closeModal("search");
            // TODO: Mở trang chi tiết phim (nếu có)
        });
    }

    private void loadPoster() {
        // Tải ảnh bất đồng bộ
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                if (movie.posterUrl() != null && !movie.posterUrl().isEmpty()) {
                    URL url = new URL(movie.posterUrl());
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(40, 60, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        posterLabel.setIcon(icon);
                        posterLabel.setBackground(null); // Bỏ màu nền
                    }
                } catch (Exception e) {
                    // Tải lỗi, không cần làm gì
                }
            }
        }.execute();
    }
}
