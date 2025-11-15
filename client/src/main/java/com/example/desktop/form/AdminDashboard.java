package com.example.desktop.form;

import com.example.desktop.system.Form;
import javax.swing.JLabel;
import java.awt.BorderLayout;

// (Tùy chọn) Thêm annotation này để FormSearch (Ctrl+F) có thể tìm thấy
@raven.modal.demo.utils.SystemForm(name = "Thống kê", description = "Xem thống kê trang admin")
public class AdminDashboard extends Form {

    public AdminDashboard() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Chào mừng đến Trang Thống Kê Admin");
        label.setHorizontalAlignment(JLabel.CENTER);
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void formInit() {
        System.out.println("Admin Dashboard initialized");
    }
}