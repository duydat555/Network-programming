package com.example.desktop.form;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.component.ButtonLink;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.DropShadowBorder;
import raven.modal.demo.component.LabelButton;
import com.example.desktop.system.Form;
import com.example.desktop.system.FormManager;

import javax.swing.*;
import java.awt.*;

public class Register extends Form {

    public Register() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("al center center"));
        createRegister();
    }

    private void createRegister() {
        JPanel panelRegister = new JPanel(new BorderLayout()) {
            @Override
            public void updateUI() {
                super.updateUI();
            }
        };
        panelRegister.setOpaque(false);

        JPanel registerContent = new JPanel(new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]"));

        JLabel lbTitle = new JLabel("Tạo tài khoản mới");
        JLabel lbDescription = new JLabel("Điền thông tin để đăng ký tài khoản");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +12;");

        registerContent.add(lbTitle);
        registerContent.add(lbDescription);

        JTextField txtUsername = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirmPassword = new JPasswordField();
        JCheckBox chAgree = new JCheckBox("Tôi đồng ý với điều khoản và điều kiện");
        JButton cmdRegister = new JButton("Đăng ký", new FlatSVGIcon("icons/arrow-right.svg")) {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };
        cmdRegister.setHorizontalTextPosition(JButton.LEADING);

        // style
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên đăng nhập");
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập email của bạn");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu");
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Xác nhận mật khẩu");

        panelRegister.putClientProperty(FlatClientProperties.STYLE, "[dark]background:tint($Panel.background,1%);");
        registerContent.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        String textFieldStyle = "margin:4,10,4,10;arc:12;";
        txtUsername.putClientProperty(FlatClientProperties.STYLE, textFieldStyle);
        txtEmail.putClientProperty(FlatClientProperties.STYLE, textFieldStyle);
        txtPassword.putClientProperty(FlatClientProperties.STYLE, textFieldStyle + "showRevealButton:true;");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, textFieldStyle + "showRevealButton:true;");

        cmdRegister.setBackground(new Color(0x57, 0x4b, 0xce));
        cmdRegister.setOpaque(true);
        cmdRegister.setContentAreaFilled(true);
        cmdRegister.setBorderPainted(false);
        cmdRegister.setForeground(Color.WHITE);
        cmdRegister.putClientProperty(FlatClientProperties.STYLE, "margin:4,10,4,10;arc:12;");

        registerContent.add(new JLabel("Tên đăng nhập"), "gapy 25");
        registerContent.add(txtUsername);

        registerContent.add(new JLabel("Email"), "gapy 10");
        registerContent.add(txtEmail);

        registerContent.add(new JLabel("Mật khẩu"), "gapy 10");
        registerContent.add(txtPassword);

        registerContent.add(new JLabel("Xác nhận mật khẩu"), "gapy 10");
        registerContent.add(txtConfirmPassword);

        registerContent.add(chAgree, "gapy 10");
        registerContent.add(cmdRegister, "gapy 20");
        registerContent.add(createInfo());

        panelRegister.add(registerContent);
        add(panelRegister);

        // event
        cmdRegister.addActionListener(e -> {
            String userName = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String password = String.valueOf(txtPassword.getPassword());
            String confirmPassword = String.valueOf(txtConfirmPassword.getPassword());

            if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!chAgree.isSelected()) {
                JOptionPane.showMessageDialog(this, "Bạn cần đồng ý với điều khoản!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Không block UI: gọi API trong thread khác
            cmdRegister.setEnabled(false);

            new Thread(() -> {
                AuthApiClient.ApiResult result =
                        AuthApiClient.register(userName, email, password);

                SwingUtilities.invokeLater(() -> {
                    cmdRegister.setEnabled(true);

                    if (result.success()) {
                        JOptionPane.showMessageDialog(this,
                                result.message().isEmpty() ? "Đăng ký thành công!" : result.message(),
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Chuyển về form Login
                        JFrame frame = FormManager.getFrame();
                        frame.getContentPane().removeAll();
                        Login login = new Login();
                        frame.getContentPane().add(login);
                        frame.repaint();
                        frame.revalidate();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                result.message(),
                                "Đăng ký thất bại",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        });
    }

    private JPanel createInfo() {
        JPanel panelInfo = new JPanel(new MigLayout("wrap,al center", "[center]"));
        panelInfo.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        panelInfo.add(new JLabel("Bạn đã có tài khoản?"), "split 2,gapx push n");
        ButtonLink cmdLogin = new ButtonLink("Đăng nhập");
        panelInfo.add(cmdLogin, "gapx n push");

        // event chuyển về form Login
        cmdLogin.addActionListener(e -> {
            // Navigate to Login form
            JFrame frame = FormManager.getFrame();
            frame.getContentPane().removeAll();
            Login login = new Login();
            frame.getContentPane().add(login);
            frame.repaint();
            frame.revalidate();
        });

        return panelInfo;
    }
}
