package com.example.desktop.form;

import com.example.desktop.system.Form;
import javax.swing.JLabel;
import java.awt.BorderLayout;

@raven.modal.demo.utils.SystemForm(name = "Quản lý Người dùng", description = "Quản lý tài khoản người dùng")
public class UserManagement extends Form {

    public UserManagement() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Trang Quản lý Người dùng");
        label.setHorizontalAlignment(JLabel.CENTER);
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void formInit() {
        System.out.println("User Management initialized");
    }
}