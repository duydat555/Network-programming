package com.example.desktop.form;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.system.Form;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class UserManagement extends Form {

    private JTable table;
    private DefaultTableModel tableModel;

    public UserManagement() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 10", "[fill]", "[][grow, fill]"));

        // 1. Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton refreshButton = new JButton("Tải lại");
        refreshButton.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/refresh.svg", 0.8f));

        JButton addUserButton = new JButton("Thêm");
        addUserButton.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/plus.svg", 0.8f));
        addUserButton.setEnabled(false); // (Tạm thời vô hiệu hóa)

        JButton editUserButton = new JButton("Sửa");
        editUserButton.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/edit.svg", 0.8f));
        editUserButton.setEnabled(false); // (Tạm thời vô hiệu hóa)

        toolBar.add(refreshButton);
        toolBar.addSeparator();
        toolBar.add(addUserButton);
        toolBar.add(editUserButton);

        // 2. Bảng dữ liệu
        String[] columnNames = {"ID", "Username", "Email", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // 3. Thêm components vào Form
        add(toolBar, "wrap");
        add(scrollPane);

        // 4. Gán sự kiện
        refreshButton.addActionListener(e -> loadUsers());
    }

    @Override
    public void formInit() {
        // Tự động tải dữ liệu khi form được mở
        System.out.println("User Management initialized, loading users...");
        loadUsers();
    }

    private void loadUsers() {
        // Hiển thị loading (nếu cần)
        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        // Gọi API trong một luồng riêng (SwingWorker)
        SwingWorker<List<AuthApiClient.UserInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AuthApiClient.UserInfo> doInBackground() throws Exception {
                // Gọi API
                return AuthApiClient.getUsers();
            }

            @Override
            protected void done() {
                try {
                    List<AuthApiClient.UserInfo> users = get();
                    // Đổ dữ liệu vào bảng
                    for (AuthApiClient.UserInfo user : users) {
                        tableModel.addRow(new Object[]{
                                user.id(),
                                user.username(),
                                user.email(),
                                user.role()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            UserManagement.this,
                            "Không thể tải danh sách người dùng: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    @Override
    public void formRefresh() {
        loadUsers();
    }
}