package com.example.desktop.form;

import com.example.desktop.component.ButtonLink;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.DropShadowBorder;
import raven.modal.demo.component.LabelButton;
import raven.modal.demo.model.ModelUser;
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
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên đăng nhập của bạn");
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

        cmdLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:#574bce;" +
                "margin:4,10,4,10;" +
                "arc:12;");

        loginContent.add(new JLabel("Tên đăng nhập"), "gapy 25");
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
            String userName = txtUsername.getText();
            String password = String.valueOf(txtPassword.getPassword());
            ModelUser user = getUser(userName, password);
           // MyDrawerBuilder.getInstance().setUser(user);
            FormManager.login();
        });
    }

    private JPanel createInfo() {
        JPanel panelInfo = new JPanel(new MigLayout("wrap,al center", "[center]"));
        panelInfo.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");

        panelInfo.add(new JLabel("Bạn chưa có tài khoản ?"), "split 2,gapx push n");
        ButtonLink cmdSignUp = new ButtonLink("Đăng ký");
        panelInfo.add(cmdSignUp, "gapx n push");
        panelInfo.add(new JLabel("Bạn không nhớ chi tiết tài khoản của mình?"));
        panelInfo.add(new JLabel("Liên hệ với chúng tôi tại"), "split 2");
        LabelButton lbLink = new LabelButton("help@info.com");

        panelInfo.add(lbLink);

        // event
        lbLink.addOnClick(e -> {

        });
        return panelInfo;
    }

    private void applyShadowBorder(JPanel panel) {
        if (panel != null) {
            panel.setBorder(new DropShadowBorder(new Insets(5, 8, 12, 8), 1, 25));
        }
    }

    private ModelUser getUser(String user, String password) {

        // just testing.
        // input any user and password is admin by default
        // user='staff' password='123' if we want to test validation menu for role staff

        if (user.equals("staff") && password.equals("123")) {
            return new ModelUser("Justin White", "justinwhite@gmail.com", ModelUser.Role.STAFF);
        }
        return new ModelUser("Ra Ven", "raven@gmail.com", ModelUser.Role.ADMIN);
    }
}
