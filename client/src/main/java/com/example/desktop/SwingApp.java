package com.example.desktop;

import com.example.desktop.drawer.MyDrawerBuilder;
import com.example.desktop.form.Dashboard;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import raven.drawer.Drawer;
import raven.popup.GlassPanePopup;

import javax.swing.*;
import java.awt.*;

public class SwingApp extends JFrame {
    private static SwingApp app;
    private JPanel contentPanel;
    private JPanel drawerPanel;
    private JSplitPane splitPane;

    public SwingApp() {
        init();
        getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
    }

    private void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);

        // Tạo panel cho drawer
        drawerPanel = new JPanel(new BorderLayout());
        drawerPanel.setPreferredSize(new Dimension(300, 0));

        // Tạo panel cho nội dung chính
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // Sử dụng JSplitPane để chia màn hình
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, drawerPanel, contentPanel);
        splitPane.setDividerLocation(300); // Chiều rộng drawer
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        setContentPane(splitPane);

        initDrawer();
        showForm(new Dashboard());
    }

    private void initDrawer() {
        // Tạo builder
        MyDrawerBuilder builder = new MyDrawerBuilder();

        // Tạo DrawerPanel với builder
        raven.drawer.component.DrawerPanel drawerPanelComponent = new raven.drawer.component.DrawerPanel(builder);

        drawerPanelComponent.setOpaque(true);

        drawerPanel.setOpaque(true);

        // Thêm DrawerPanel vào drawerPanel
        drawerPanel.add(drawerPanelComponent, BorderLayout.CENTER);
    }


    public void showForm(Component component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void showFormStatic(Component component) {
        if (app != null) {
            app.showForm(component);
        }
    }

    public static void main(String[] args) {
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("raven.themes");
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatMacDarkLaf.setup();

        EventQueue.invokeLater(() -> {
            app = new SwingApp();
            app.setVisible(true);
        });
    }
}
