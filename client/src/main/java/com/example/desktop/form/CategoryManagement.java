package com.example.desktop.form;

import com.example.desktop.system.Form;
import javax.swing.JLabel;
import java.awt.BorderLayout;

@raven.modal.demo.utils.SystemForm(name = "Quản lý Thể loại", description = "Quản lý các thể loại phim")
public class CategoryManagement extends Form {

    public CategoryManagement() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Trang Quản lý Thể loại");
        label.setHorizontalAlignment(JLabel.CENTER);
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void formInit() {
        System.out.println("Category Management initialized");
    }
}