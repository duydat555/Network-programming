package com.example.desktop.component;

import com.example.desktop.model.Movie;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * Rich presentation panel for movie detail pages.
 * Fixed: Replaced ALL invalid 'transparent' colors in FlatLaf style with 'null'.
 */
public class MovieDetailPanel extends JPanel {

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private final JLabel titleLabel = new JLabel();
    private final JPanel metaPanel = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JTextArea descriptionArea = new JTextArea();
    private final JLabel ratingBadge = new JLabel();

    // Buttons
    private final JButton trailerButton = new JButton("Xem trailer", new FlatSVGIcon("icons/clapperboard.svg", 0.75f));
    private final JButton watchButton = new JButton("Xem phim", new FlatSVGIcon("icons/tv-minimal-play.svg", 0.9f));
    private final JButton favoriteButton = new JButton("Yêu thích", new FlatSVGIcon("icons/heart.svg", 0.75f));

    public MovieDetailPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(TRANSPARENT);
        add(createSurface(), BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // prevent default background fill
    }

    private JComponent createSurface() {
        JPanel surface = new TransparentPanel(new MigLayout("fill, insets 30", "[grow, fill]", "[top]5[]10[grow, fill]push[]"));

        // ✅ FIX: Thay "background:transparent;" bằng "background:null;"
        surface.putClientProperty(FlatClientProperties.STYLE,
                "arc:25;" +
                        "border:20,20,20,20;" +
                        "background:null;");

        // 1. TITLE & RATING
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +16; foreground:#FFFFFF;");

        ratingBadge.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +0;" +
                        "border:5,10,5,10;" +
                        "arc:15;" +
                        "background:#313131;" +
                        "foreground:#FFFFFF;");

        surface.add(titleLabel, "cell 0 0, split 2, growx");
        surface.add(ratingBadge, "gapleft push, wrap");

        // 2. META INFO
        metaPanel.setOpaque(false);
        metaPanel.setBackground(TRANSPARENT);
        surface.add(metaPanel, "cell 0 1, growx, wrap");

        // 3. DESCRIPTION
        configureDescription();
        TransparentScrollPane scrollPane = new TransparentScrollPane(descriptionArea);
        surface.add(scrollPane, "cell 0 2, grow, hmin 100");

        // 4. ACTIONS
        surface.add(createActions(), "cell 0 3, growx");

        return surface;
    }

    private JPanel createActions() {
        JPanel panel = new TransparentPanel(new MigLayout("insets 0", "[grow][grow][grow]", "[fill, 45!]"));
        panel.setOpaque(false);
        panel.setBackground(TRANSPARENT);

        styleOutlineButton(trailerButton);
        stylePrimaryButton(watchButton);
        styleOutlineButton(favoriteButton);

        panel.add(trailerButton, "growx");
        panel.add(watchButton, "growx, gapleft 15");
        panel.add(favoriteButton, "growx, gapleft 15");
        return panel;
    }

    private void configureDescription() {
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setBackground(TRANSPARENT);
        descriptionArea.setFocusable(false);
        descriptionArea.putClientProperty(FlatClientProperties.STYLE,
                "font:+2;" +
                        "foreground:#FFFFFF;");
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
            metaPanel.add(createChip(String.join(" • ", genres.subList(0, Math.min(5, genres.size())))));
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
    }

    public void updateFavoriteState(boolean favorite) {
        favoriteButton.setText(favorite ? "Đã yêu thích" : "Yêu thích");
        favoriteButton.putClientProperty(FlatClientProperties.STYLE,
                favorite ?
                        "arc:999;margin:6,12,6,12;borderWidth:0;background:#E91E63;foreground:white;" :
                        "arc:999;margin:6,12,6,12;borderWidth:1;[light]foreground:$Component.accentColor;[dark]foreground:tint($Component.accentColor,80%);"
        );
    }

    private JLabel createChip(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE,
                "border:4,10,4,10;" +
                        "arc:10;" +
                        "font:medium;" +
                        "background:rgba(100, 100, 100, 80);" +
                        "foreground:#FFFFFF;");
        return label;
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

    private static class TransparentPanel extends JPanel {
        TransparentPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBackground(TRANSPARENT);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
        }
    }

    private static class TransparentScrollPane extends JScrollPane {
        TransparentScrollPane(Component view) {
            super(view);
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);
            setBackground(TRANSPARENT);
            getViewport().setOpaque(false);
            getViewport().setBackground(TRANSPARENT);
            getViewport().setBorder(null);
            setViewportBorder(null);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
        }
    }
}