package com.example.desktop.component;

import com.example.desktop.api.MovieApi;
import com.example.desktop.model.Movie;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.extras.AvatarIcon;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CardMovie extends JPanel {

    private final Movie movie;
    private final Consumer<Movie> event;
    private JLabel imageLabel;

    public CardMovie(Movie movie, Consumer<Movie> event) {
        this.movie = movie;
        this.event = event;
        init();
        loadPosterAsync();
    }

    private void init() {
        putClientProperty(FlatClientProperties.STYLE,
                "arc:30;" +
                "border:10,10,10,10,#a9a9a9");
        setBackground(new Color(234, 234, 234));
        setLayout(new MigLayout("", "", "fill"));

        JPanel panelHeader = createHeader();
        JPanel panelBody = createBody();

        add(panelHeader, "dock north");
        add(panelBody, "dock south");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout("fill,insets 0", "[fill]", "[top]"));
        header.putClientProperty(FlatClientProperties.STYLE,
                "background:null");

        imageLabel = new JLabel(new AvatarIcon(
                movie.getPoster(), 160, 220, 20
        ));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(imageLabel, "grow");
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new MigLayout("fillx,insets 10 5 10 5", "[160,center]", "[]5[]"));
        body.putClientProperty(FlatClientProperties.STYLE,
                "background:null");

        JLabel lblTitle = new JLabel(movie.getName());
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 14f));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Giới hạn độ rộng và hiển thị "..." khi text quá dài
        lblTitle.setMaximumSize(new Dimension(160, Integer.MAX_VALUE));
        lblTitle.setPreferredSize(new Dimension(160, lblTitle.getPreferredSize().height));
        lblTitle.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +0");

        // Tự động cắt text và thêm "..." nếu quá dài
        String truncated = truncateText(movie.getName(), 160, lblTitle);
        lblTitle.setText(truncated);

        // Thêm tooltip để xem full title khi hover
        if (!truncated.equals(movie.getName())) {
            lblTitle.setToolTipText(movie.getName());
        }

        JButton button = new JButton("Chi tiết");
        button.addActionListener(_ -> event.accept(movie));
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;" +
                "margin:5,30,5,30;" +
                "borderWidth:1;" +
                "focusWidth:0;" +
                "innerFocusWidth:0;" +
                "background:null;");

        body.add(lblTitle, "wrap,growx,align center,wmax 160");
        body.add(button, "align center");
        return body;
    }

    /**
     * Cắt text nếu quá dài và thêm "..."
     */
    private String truncateText(String text, int maxWidth, JLabel label) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        FontMetrics fm = label.getFontMetrics(label.getFont());
        int textWidth = fm.stringWidth(text);

        if (textWidth <= maxWidth - 10) { // -10 để có padding
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        int availableWidth = maxWidth - ellipsisWidth - 10;

        // Tìm độ dài text phù hợp
        int length = text.length();
        while (length > 0) {
            String truncated = text.substring(0, length);
            if (fm.stringWidth(truncated) <= availableWidth) {
                return truncated + ellipsis;
            }
            length--;
        }

        return ellipsis;
    }

    /**
     * Tải ảnh poster bất đồng bộ từ URL internet
     */
    private void loadPosterAsync() {
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            MovieApi.loadPosterAsync(movie, () -> {
                // Cập nhật UI khi ảnh đã tải xong
                imageLabel.setIcon(new AvatarIcon(movie.getPoster(), 160, 220, 20));
                imageLabel.revalidate();
                imageLabel.repaint();
            });
        }
    }
}
