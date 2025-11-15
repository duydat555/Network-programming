package com.example.desktop.component;

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

    public CardMovie(Movie movie, Consumer<Movie> event) {
        this.movie = movie;
        this.event = event;
        init();
    }

    private void init() {
        putClientProperty(FlatClientProperties.STYLE,
                "arc:30;" +
                "border:10,10,10,10,#a9a9a9");
        setBackground(new Color(233, 238, 246));
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

        JLabel label = new JLabel(new AvatarIcon(
                movie.getPoster(), 160, 220, 20
        ));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(label, "grow");
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new MigLayout("fillx,insets 10 5 10 5", "[center]", "[]5[]"));
        body.putClientProperty(FlatClientProperties.STYLE,
                "background:null");

        JLabel lblTitle = new JLabel(movie.getName());
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 14f));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JButton button = new JButton("Chi tiết");
        button.addActionListener(_ -> event.accept(movie));
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;" +
                "margin:5,30,5,30;" +
                "borderWidth:1;" +
                "focusWidth:0;" +
                "innerFocusWidth:0;" +
                "background:null;");

        body.add(lblTitle, "wrap,growx,align center");
        body.add(button, "align center");
        return body;
    }
}
