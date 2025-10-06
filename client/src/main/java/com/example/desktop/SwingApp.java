package com.example.desktop;

import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.*;

public class SwingApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FlatLaf Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            JButton btn = new JButton("Nhấn vào đây");
            frame.add(btn);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
