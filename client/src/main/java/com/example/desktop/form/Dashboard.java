package com.example.desktop.form;

import com.example.desktop.component.item.CardMovie;
import com.example.desktop.system.Form;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends Form {

    public Dashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(234,234,234));

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        contentPanel.setBackground(new Color(234,234,234));

        CardMovie card1 = new CardMovie();
        card1.setMovieData(new ImageIcon("D:/hls-server/client/src/main/resources/images/avatar.jpg").getImage(), "Avatar");

        contentPanel.add(card1);


        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }
}
