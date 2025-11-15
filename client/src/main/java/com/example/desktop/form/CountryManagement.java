package com.example.desktop.form;

import com.example.desktop.system.Form;
import javax.swing.JLabel;
import java.awt.BorderLayout;

@raven.modal.demo.utils.SystemForm(name = "Quản lý Quốc gia", description = "Quản lý các quốc gia")
public class CountryManagement extends Form {

    public CountryManagement() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Trang Quản lý Quốc gia");
        label.setHorizontalAlignment(JLabel.CENTER);
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void formInit() {
        System.out.println("Country Management initialized");
    }
}