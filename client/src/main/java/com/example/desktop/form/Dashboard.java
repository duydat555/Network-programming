package com.example.desktop.form;

import com.example.desktop.component.CardMovie;
import com.example.desktop.model.Movie;
import com.example.desktop.system.Form;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends Form {

    public Dashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(234, 234, 234));

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        contentPanel.setBackground(new Color(234, 234, 234));

        ImageIcon poster = new ImageIcon(
                "D:/hls-server/client/src/main/resources/images/avatar.jpg"
        );

        Movie movie1 = new Movie(
                "Avatar",
                "Bộ phim khoa học viễn tưởng về thế giới Pandora.",
                poster
        );

        CardMovie card1 = new CardMovie(movie1, m -> {
            JOptionPane.showMessageDialog(
                    Dashboard.this,
                    "Chi tiết phim: " + m.getName(),
                    "Thông tin phim",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        contentPanel.add(card1);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }
}
