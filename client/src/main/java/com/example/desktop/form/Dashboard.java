package com.example.desktop.form;

import com.example.desktop.component.item.CardMovie;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JPanel {

    public Dashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(25, 25, 28));

        JLabel label = new JLabel("Trang chủ", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setBackground(new Color(30, 30, 30));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setPreferredSize(new Dimension(0, 60));
        add(label, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        contentPanel.setBackground(new Color(25, 25, 28));

        CardMovie card1 = new CardMovie();
        card1.setMovieData(new ImageIcon("C:/Users/GAMING F15/Downloads/hls-server/client/src/main/resources/images/avatar.jpg").getImage(), "Avatar");

        contentPanel.add(card1);


        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }
}
