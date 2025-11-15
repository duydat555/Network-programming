package com.example.desktop.form;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.component.ButtonLink;
import com.example.desktop.system.UserSession;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.DropShadowBorder;
import raven.modal.demo.component.LabelButton;
import com.example.desktop.system.Form;
import com.example.desktop.system.FormManager;

import javax.swing.*;
import java.awt.*;

public class Login extends Form {

    public Login() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("al center center"));
        createLogin();
    }

    private void createLogin() {
        JPanel panelLogin = new JPanel(new BorderLayout()) {
            @Override
            public void updateUI() {
                super.updateUI();
                //applyShadowBorder(this);
            }
        };
        panelLogin.setOpaque(false);
        // applyShadowBorder(panelLogin);

        JPanel loginContent = new JPanel(new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]"));

        JLabel lbTitle = new JLabel("Chào mừng trở lại!");
        JLabel lbDescription = new JLabel("Vui lòng đăng nhập để truy cập tài khoản của bạn");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +12;");

        loginContent.add(lbTitle);
        loginContent.add(lbDescription);

        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JCheckBox chRememberMe = new JCheckBox("Ghi nhớ đăng nhập");
        JButton cmdLogin = new JButton("Đăng nhập", new FlatSVGIcon("icons/arrow-right.svg")) {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };
        cmdLogin.setHorizontalTextPosition(JButton.LEADING);

        // style
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập email đăng nhập của bạn");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu của bạn");

        panelLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "[dark]background:tint($Panel.background,1%);");

        loginContent.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");

        txtUsername.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;" +
                "showRevealButton:true;");

        cmdLogin.setBackground(new Color(0x57, 0x4b, 0xce));
        cmdLogin.setOpaque(true);
        cmdLogin.setContentAreaFilled(true);
        cmdLogin.setBorderPainted(false);
        cmdLogin.setForeground(Color.WHITE);

        cmdLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");

        loginContent.add(new JLabel("Email đăng nhập"), "gapy 25");
        loginContent.add(txtUsername);

        loginContent.add(new JLabel("Mật khẩu"), "gapy 10");
        loginContent.add(txtPassword);
        loginContent.add(chRememberMe);
        loginContent.add(cmdLogin, "gapy 20");
        loginContent.add(createInfo());

        panelLogin.add(loginContent);
        add(panelLogin);

        // event
        cmdLogin.addActionListener(e -> {
            String email = txtUsername.getText();
            String password = String.valueOf(txtPassword.getPassword());

            // Email admin (bạn có thể thay đổi)
            final String ADMIN_EMAIL = "admin1@gmail.com";

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            cmdLogin.setEnabled(false);

            new Thread(() -> {
                // Bước 1: Luôn gọi API để server xác thực
                AuthApiClient.LoginResult result =
                        AuthApiClient.login(email, password);

                SwingUtilities.invokeLater(() -> {
                    cmdLogin.setEnabled(true);

                    if (result.success()) {

                        // Bước 2: Kiểm tra xem người đăng nhập có phải là Admin không
                        if (result.user() != null && result.user().email().equals(ADMIN_EMAIL)) {

                            // Gọi hàm hiển thị giao diện Admin
                            FormManager.showAdminUI();

                            // === ĐÂY LÀ ADMIN ===
                            JOptionPane.showMessageDialog(this,
                                    "Chào mừng Admin!",
                                    "Đăng nhập thành công",
                                    JOptionPane.INFORMATION_MESSAGE);

                        } else {

                            // === ĐÂY LÀ CLIENT THÔNG THƯỜNG ===
                            System.out.println("=== LOGIN SUCCESS (CLIENT) ===");
                            System.out.println("User from API: " + result.user());
                            UserSession.setUser(result.user());
                            System.out.println("User in session: " + UserSession.getUser());

                            // Install drawer FIRST with user data
                            FormManager.login();

                            // Then show success message
                            JOptionPane.showMessageDialog(this,
                                    result.message().isEmpty() ? "Đăng nhập thành công!" : result.message(),
                                    "Thông báo",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }

                    } else {
                        // Đăng nhập thất bại (sai pass, sai email...)
                        JOptionPane.showMessageDialog(this,
                                result.message(),
                                "Đăng nhập thất bại",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        });
    }

    private JPanel createInfo() {
        JPanel panelInfo = new JPanel(new MigLayout("wrap,al center", "[center]"));
        panelInfo.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");

        panelInfo.add(new JLabel("Bạn chưa có tài khoản?"), "split 2,gapx push n");
        ButtonLink cmdSignUp = new ButtonLink("Đăng ký");
        panelInfo.add(cmdSignUp, "gapx n push");
        panelInfo.add(new JLabel("Bạn không nhớ chi tiết tài khoản của mình?"));
        panelInfo.add(new JLabel("Liên hệ với chúng tôi tại"), "split 2");
        LabelButton lbLink = new LabelButton("help@info.com");

        panelInfo.add(lbLink);

        // event
        lbLink.addOnClick(e -> {

        });

        cmdSignUp.addActionListener(e -> {
            // Navigate to Register form
            JFrame frame = FormManager.getFrame();
            frame.getContentPane().removeAll();
            Register register = new Register();
            frame.getContentPane().add(register);
            frame.repaint();
            frame.revalidate();
        });
        return panelInfo;
    }

    private void applyShadowBorder(JPanel panel) {
        if (panel != null) {
            panel.setBorder(new DropShadowBorder(new Insets(5, 8, 12, 8), 1, 25));
        }
    }
}

