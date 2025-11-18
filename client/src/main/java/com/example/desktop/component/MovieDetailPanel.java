package com.example.desktop.component;

import com.example.desktop.api.MovieApi;
import com.example.desktop.model.Movie;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.extras.AvatarIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

/**
 * Rich presentation panel for movie detail pages.
 */
public class MovieDetailPanel extends JPanel {

    private final JLabel posterLabel = new JLabel();
    private final JLabel titleLabel = new JLabel();
    private final JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JTextArea descriptionArea = new JTextArea();
    private final JLabel ratingBadge = new JLabel();

    // Buttons
    private final JButton trailerButton = new JButton("Xem trailer", new FlatSVGIcon("icons/clapperboard.svg", 0.75f));
    private final JButton watchButton = new JButton("Xem phim", new FlatSVGIcon("icons/tv-minimal-play.svg", 0.9f));
    private final JButton favoriteButton = new JButton("Yêu thích", new FlatSVGIcon("icons/heart.svg", 0.75f));

    public MovieDetailPanel() {
        setLayout(new BorderLayout());
        setOpaque(false); // Panel chính trong suốt
        add(createSurface(), BorderLayout.CENTER);
    }

    private JComponent createSurface() {
        // Layout: Cột 1 (Poster) cố định 260px, Cột 2 (Nội dung) co giãn
        JPanel surface = new JPanel(new MigLayout("fill, insets 30", "[260!]25[grow, fill]", "[top]5[]10[grow, fill]push[]"));

        // --- SỬA ĐỔI TẠI ĐÂY ---
        surface.setOpaque(false); // Để trong suốt
        surface.putClientProperty(FlatClientProperties.STYLE,
                "arc:25;" +
                        "border:20,20,20,20;" +
                        "background:null;"); // Tắt màu nền trắng/xám
        // -----------------------

        // 1. POSTER (Cột 0, Hàng 0, Span 4 hàng dọc)
        configurePoster();
        surface.add(posterLabel, "cell 0 0 1 4, growy, top");

        // 2. TITLE & RATING (Cột 1, Hàng 0)
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +12; foreground:$Label.foreground;");

        // Rating Badge: Giữ nền tối để chữ vàng/trắng luôn nổi bật
        ratingBadge.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +0;" +
                        "border:5,10,5,10;" +
                        "arc:15;" +
                        "background:#222222;" + // Nền rất tối cho badge
                        "foreground:#FFFFFF;");

        surface.add(titleLabel, "cell 1 0, split 2, growx");
        surface.add(ratingBadge, "gapleft push, wrap");

        // 3. META INFO (Cột 1, Hàng 1)
        metaPanel.setOpaque(false);
        surface.add(metaPanel, "cell 1 1, growx, wrap");

        // 4. DESCRIPTION (Cột 1, Hàng 2)
        configureDescription();
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        surface.add(scrollPane, "cell 1 2, grow, hmin 100");

        // 5. ACTIONS (Cột 1, Hàng 3)
        surface.add(createActions(), "cell 1 3, growx");

        return surface;
    }

    private void configurePoster() {
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.putClientProperty(FlatClientProperties.STYLE,
                "arc:30;" +
                        "border:0,0,0,0;" +
                        "background:null;"); // Poster không cần nền riêng
        posterLabel.setPreferredSize(new Dimension(260, 360));
        posterLabel.setMinimumSize(new Dimension(260, 360));
        posterLabel.setIcon(createPlaceholderIcon());
    }

    private void configureDescription() {
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFocusable(false);
        descriptionArea.putClientProperty(FlatClientProperties.STYLE,
                "font:+1;" +
                        "foreground:$Label.foreground;");
    }

    private JPanel createActions() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow][grow][grow]", "[fill, 40!]"));
        panel.setOpaque(false);

        styleGhostButton(trailerButton);
        stylePrimaryButton(watchButton);
        styleOutlineButton(favoriteButton);

        panel.add(trailerButton, "growx");
        panel.add(watchButton, "growx, gapleft 15");
        panel.add(favoriteButton, "growx, gapleft 15");
        return panel;
    }

    private void styleGhostButton(JButton button) {
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;" +
                        "margin:6,12,6,12;" +
                        "background:null;" +
                        "borderWidth:1;" +
                        "focusWidth:0;" +
                        "innerFocusWidth:0;");
    }

    private void stylePrimaryButton(JButton button) {
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;" +
                        "margin:6,12,6,12;" +
                        "borderWidth:0;" +
                        "focusWidth:0;" +
                        "innerFocusWidth:0;" +
                        "foreground:#ffffff;" +
                        "background:$Component.accentColor;");
    }

    private void styleOutlineButton(JButton button) {
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;" +
                        "margin:6,12,6,12;" +
                        "borderWidth:1;" +
                        "focusWidth:0;" +
                        "innerFocusWidth:0;" +
                        "[light]foreground:$Component.accentColor;" +
                        "[dark]foreground:tint($Component.accentColor,80%);");
    }

    public void displayMovie(Movie movie) {
        if (movie == null) {
            titleLabel.setText("Chưa xác định");
            descriptionArea.setText("Chọn một bộ phim để xem chi tiết.");
            metaPanel.removeAll();
            posterLabel.setIcon(createPlaceholderIcon());
            ratingBadge.setVisible(false);
            revalidate();
            repaint();
            return;
        }

        titleLabel.setText(movie.getTitle());
        descriptionArea.setText(Objects.toString(movie.getDescription(), "Chưa có mô tả."));
        descriptionArea.setCaretPosition(0);

        metaPanel.removeAll();
        if (movie.getYear() != null) {
            metaPanel.add(createChip(movie.getYear().toString()));
        }
        if (movie.getDurationMin() != null) {
            metaPanel.add(createChip(movie.getDurationMin() + " phút"));
        }
        List<String> genres = movie.getGenres();
        if (genres != null && !genres.isEmpty()) {
            metaPanel.add(createChip(String.join(" • ", genres.subList(0, Math.min(3, genres.size())))));
        }
        metaPanel.revalidate();
        metaPanel.repaint();

        ratingBadge.setVisible(true);
        double score = computeRating(movie);
        String htmlRating = String.format(
                "<html><span style='color:#DDDDDD;'>Đánh giá</span>&nbsp;&nbsp;<span style='color:#FFC107; font-size:110%%;'>★ %.1f/5</span></html>",
                score
        );
        ratingBadge.setText(htmlRating);

        updatePoster(movie);
    }

    public void updateFavoriteState(boolean favorite) {
        favoriteButton.setText(favorite ? "Đã yêu thích" : "Yêu thích");
        favoriteButton.putClientProperty(FlatClientProperties.STYLE,
                favorite ?
                        "arc:999;margin:6,12,6,12;borderWidth:0;background:#E91E63;foreground:white;" :
                        "arc:999;margin:6,12,6,12;borderWidth:1;[light]foreground:$Component.accentColor;[dark]foreground:tint($Component.accentColor,80%);"
        );
    }

    private void updatePoster(Movie movie) {
        if (movie.getPoster() != null) {
            posterLabel.setIcon(new AvatarIcon(movie.getPoster(), 260, 360, 30));
            return;
        }
        posterLabel.setIcon(createPlaceholderIcon());
        MovieApi.loadPosterAsync(movie, () -> posterLabel.setIcon(new AvatarIcon(movie.getPoster(), 260, 360, 30)));
    }

    private JLabel createChip(String text) {
        JLabel label = new JLabel(text);
        // Chip cũng cần background bán trong suốt nếu muốn đẹp trên nền ảnh
        label.putClientProperty(FlatClientProperties.STYLE,
                "border:4,10,4,10;" +
                        "arc:10;" +
                        "font:medium;" +
                        "background:rgba(100, 100, 100, 80);" + // Màu xám trong suốt
                        "foreground:#FFFFFF;");
        return label;
    }

    private ImageIcon createPlaceholderIcon() {
        BufferedImage img = new BufferedImage(260, 360, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(30, 30, 30, 50));
        g2.fillRoundRect(0, 0, img.getWidth(), img.getHeight(), 30, 30);

        Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2.setStroke(dashed);
        g2.setColor(new Color(200, 200, 200, 100));
        g2.drawRoundRect(1, 1, img.getWidth()-2, img.getHeight()-2, 30, 30);

        g2.setColor(new Color(200, 200, 200));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));

        String text = "No Poster";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (img.getWidth() - fm.stringWidth(text)) / 2f, (img.getHeight() / 2f) + 5);
        g2.dispose();
        return new ImageIcon(img);
    }

    private double computeRating(Movie movie) {
        double base = 3.6;
        double durationFactor = movie.getDurationMin() == null ? 0.2 : Math.min(1.0, movie.getDurationMin() / 140.0);
        double genreFactor = movie.getGenres() == null ? 0 : Math.min(0.6, movie.getGenres().size() * 0.12);
        return Math.max(3.0, Math.min(5.0, base + durationFactor + genreFactor));
    }

    public void onWatchTrailer(ActionListener action) {
        resetAction(trailerButton, action);
    }

    public void onWatchMovie(ActionListener action) {
        resetAction(watchButton, action);
    }

    public void onToggleFavorite(ActionListener action) {
        resetAction(favoriteButton, action);
    }

    private void resetAction(JButton button, ActionListener action) {
        for (ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }
        if (action != null) {
            button.addActionListener(action);
        }
    }
}