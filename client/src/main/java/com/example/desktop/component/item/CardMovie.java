package com.example.desktop.component.item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class CardMovie extends JPanel {

    private final JLabel posterLabel = new JLabel();
    private final JLabel titleLabel  = new JLabel();

    // màu & kích thước mặc định
    private static final Color BG            = new Color(25, 25, 28);
    private static final Color FG            = new Color(223, 225, 229);
    private static final Color HOVER_BORDER  = new Color(90, 126, 255);
    private static final Color NORMAL_BORDER = new Color(60, 60, 65);
    private static final Dimension POSTER_SIZE = new Dimension(160, 220);

    public CardMovie() {
        setOpaque(true);
        setBackground(BG);
        setLayout(new BorderLayout(0, 6));
        setBorder(new EmptyBorder(8, 8, 8, 8));         // padding xung quanh

        // ===== Poster =====
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setVerticalAlignment(SwingConstants.CENTER);
        posterLabel.setPreferredSize(POSTER_SIZE);
        posterLabel.setOpaque(true);
        posterLabel.setBackground(new Color(32, 32, 36));
        posterLabel.setBorder(new LineBorder(NORMAL_BORDER, 1, true));
        posterLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover viền cho poster
        posterLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                posterLabel.setBorder(new LineBorder(HOVER_BORDER, 2, true));
            }
            @Override public void mouseExited(MouseEvent e) {
                posterLabel.setBorder(new LineBorder(NORMAL_BORDER, 1, true));
            }
        });

        // ===== Title =====
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(FG);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setText("Movie Title");

        add(posterLabel, BorderLayout.CENTER);
        add(titleLabel,  BorderLayout.SOUTH);
    }

    /** Set dữ liệu poster + tiêu đề */
    public void setMovieData(Image image, String title) {
        titleLabel.setText(title);
        posterLabel.setIcon(new ImageIcon(scaleToFit(image, POSTER_SIZE)));
    }

    /** Cho phép thay icon trực tiếp nếu đã có dạng Icon */
    public void setMovieData(Icon posterIcon, String title) {
        titleLabel.setText(title);
        posterLabel.setIcon(posterIcon);
    }

    /** API cho click vào poster (ví dụ mở chi tiết) */
    public void onPosterClick(Runnable action) {
        for (MouseListener ml : posterLabel.getMouseListeners()) {
            // giữ nguyên listener hover; chỉ thêm click
        }
        posterLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (action != null) action.run();
            }
        });
    }

    // --- Utils ---
    private static Image scaleToFit(Image src, Dimension target) {
        int tw = target.width, th = target.height;
        int sw = src.getWidth(null), sh = src.getHeight(null);
        if (sw <= 0 || sh <= 0) return src;

        double r = Math.min((double) tw / sw, (double) th / sh);
        int nw = (int) Math.round(sw * r);
        int nh = (int) Math.round(sh * r);

        Image scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) scaled.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, nw, nh, null);
        g2.dispose();
        return scaled;
    }
}
