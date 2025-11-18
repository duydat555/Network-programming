package com.example.desktop.form;

import com.example.desktop.component.MovieDetailPanel;
import com.example.desktop.model.Movie;
import com.example.desktop.system.Form;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.function.Consumer;

public class MovieDetailForm extends Form {

    private final MovieDetailPanel detailPanel = new MovieDetailPanel();
    private Movie currentMovie;
    private Image backgroundImage; // Lưu trữ ảnh nền hiện tại
    private SwingWorker<Image, Void> backgroundWorker;

    private Consumer<Movie> trailerHandler;
    private Consumer<Movie> watchHandler;
    private Consumer<Movie> favoriteHandler;

    public MovieDetailForm() {
        // Layout chính
        setLayout(new MigLayout("fill, insets 30", "[center]", "[center]"));

        // Card chứa nội dung (Glassmorphism - Nền đen trong suốt)
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBackground(new Color(0, 0, 0, 0));
        card.putClientProperty(FlatClientProperties.STYLE,
                "border:0,0,0,0;" +
                        "arc:30;");

        card.add(detailPanel, BorderLayout.CENTER);

        // Thêm card vào form
        add(card, "grow, wmax 1200, hmax 800");
    }

    /**
     * Phương thức mới: Đặt hình nền từ một đường dẫn URL bất kỳ.
     * Xử lý tải ảnh dưới background thread để không bị đơ giao diện.
     */
    public void setBackgroundUrl(String urlString) {
        if (backgroundWorker != null && !backgroundWorker.isDone()) {
            backgroundWorker.cancel(true);
        }
        if (urlString == null || urlString.isBlank()) {
            this.backgroundImage = null;
            repaint();
            return;
        }

        backgroundWorker = new SwingWorker<>() {
            @Override
            protected Image doInBackground() throws Exception {
                return ImageIO.read(new URL(urlString));
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    backgroundImage = get();
                } catch (Exception e) {
                    System.err.println("Không thể tải ảnh nền: " + e.getMessage());
                    backgroundImage = null;
                }
                repaint();
            }
        };
        backgroundWorker.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        // Bật khử răng cưa để ảnh mượt nhất
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        // 1. Vẽ Ảnh nền (Effect: Object-Fit Cover)
        if (backgroundImage != null) {
            double imageW = backgroundImage.getWidth(null);
            double imageH = backgroundImage.getHeight(null);

            // Tính toán tỷ lệ scale để lấp đầy khung hình
            double scale = Math.max(width / imageW, height / imageH);
            int drawW = (int) (imageW * scale);
            int drawH = (int) (imageH * scale);

            // Căn giữa ảnh (Center Crop)
            int x = (width - drawW) / 2;
            int y = (height - drawH) / 2;

            g2.drawImage(backgroundImage, x, y, drawW, drawH, null);
        } else {
            // Fallback: Gradient tối nếu chưa có ảnh
            g2.setPaint(new GradientPaint(0, 0, new Color(20, 20, 20), 0, height, new Color(5, 5, 5)));
            g2.fillRect(0, 0, width, height);
        }

        // 2. Lớp phủ tối (Overlay)
        // Quan trọng: Phủ một lớp đen 70% để làm chìm ảnh nền, giúp nội dung text nổi bật
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, width, height);

        // 3. Vignette (Góc tối) - Tạo chiều sâu nghệ thuật
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(width / 2f, height / 2f),
                Math.max(width, height),
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)}
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, width, height);

        g2.dispose();
    }

    public void bindMovie(Movie movie) {
        this.currentMovie = movie;
        detailPanel.displayMovie(movie);
        setBackgroundUrl(movie != null ? movie.getPosterUrl() : null);
    }

    public void setOnWatchTrailer(Consumer<Movie> handler) {
        this.trailerHandler = handler;
        detailPanel.onWatchTrailer(e -> trigger(handler));
    }

    public void setOnWatchMovie(Consumer<Movie> handler) {
        this.watchHandler = handler;
        detailPanel.onWatchMovie(e -> trigger(handler));
    }

    public void setOnToggleFavorite(Consumer<Movie> handler) {
        this.favoriteHandler = handler;
        detailPanel.onToggleFavorite(e -> trigger(handler));
    }

    public void updateFavoriteState(boolean isFavorite) {
        detailPanel.updateFavoriteState(isFavorite);
    }

    private void trigger(Consumer<Movie> handler) {
        if (handler != null && currentMovie != null) {
            handler.accept(currentMovie);
        }
    }
}