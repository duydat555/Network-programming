package com.example.hls_server.gui;

import com.example.hls_server.model.ClientInfo;
import com.example.hls_server.service.ClientMonitor;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerUI extends JFrame {

    private final ClientMonitor clientMonitor;
    private DefaultTableModel tableModel;
    private JTable clientTable;
    private JLabel statusLabel;
    private JTextArea segmentDetailsArea;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ServerUI(ClientMonitor clientMonitor) {
        this.clientMonitor = clientMonitor;
        initComponents();
        startAutoRefresh();
    }

    private void initComponents() {
        // Tiêu đề cửa sổ
        setTitle("HLS Server - Giám sát Client");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Status
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Trạng thái Server"));

        statusLabel = new JLabel("Client hoạt động: 0");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(statusLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Hệ thống Giám sát HLS Server", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);

        // Client table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách Client đang kết nối"));

        // Tên các cột trong bảng
        String[] columns = {"Địa chỉ IP", "Segment hiện tại", "Tổng Request", "Hoạt động cuối", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        clientTable = new JTable(tableModel);
        clientTable.setRowHeight(25);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSegmentDetails();
            }
        });

        // Set độ rộng cột
        clientTable.getColumnModel().getColumn(0).setPreferredWidth(150); // IP
        clientTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Segment
        clientTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Total Request
        clientTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Last Activity
        clientTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status

        // Căn giữa cho các cột số liệu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        clientTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        clientTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane tableScrollPane = new JScrollPane(clientTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(tablePanel);

        // Segment details panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Lịch sử Segment gần đây"));

        segmentDetailsArea = new JTextArea();
        segmentDetailsArea.setEditable(false);
        segmentDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        segmentDetailsArea.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane detailsScrollPane = new JScrollPane(segmentDetailsArea);
        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        splitPane.setBottomComponent(detailsPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Bottom panel - Control buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton clearButton = new JButton("Dọn dẹp Client không hoạt động");
        clearButton.addActionListener(e -> {
            clientMonitor.cleanupInactiveClients();
            refreshClientList();
        });

        JLabel autoRefreshLabel = new JLabel("Tự động làm mới mỗi 2 giây  ");
        autoRefreshLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        bottomPanel.add(autoRefreshLabel);
        bottomPanel.add(clearButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void startAutoRefresh() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Cleanup and refresh
                    clientMonitor.cleanupInactiveClients();
                    refreshClientList();
                });
            }
        }, 1000, 2000); // Refresh mỗi 2 giây
    }

    private void refreshClientList() {
        List<ClientInfo> clients = clientMonitor.getActiveClients();

        SwingUtilities.invokeLater(() -> {
            // Lưu lại dòng đang chọn để sau khi refresh không bị mất selection
            int selectedRow = clientTable.getSelectedRow();
            String selectedIP = null;
            if (selectedRow >= 0) {
                selectedIP = (String) tableModel.getValueAt(selectedRow, 0);
            }

            // Xóa và cập nhật lại bảng
            tableModel.setRowCount(0);

            for (ClientInfo client : clients) {
                // Tính thời gian inactive
                long inactiveSec = client.getInactiveSeconds();
                String statusText = client.isActive() ? "●" : "○ (" + inactiveSec + "s)";

                Object[] row = {
                        client.getIpAddress(),
                        client.getCurrentSegment() != null ? client.getCurrentSegment() : "N/A",
                        client.getTotalRequests(),
                        client.getLastActivity().format(timeFormatter),
                        statusText
                };
                tableModel.addRow(row);
            }

            // Cập nhật trạng thái label
            statusLabel.setText("Client hoạt động: " + clients.size());

            // Khôi phục selection
            if (selectedIP != null) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (selectedIP.equals(tableModel.getValueAt(i, 0))) {
                        clientTable.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }

            updateSegmentDetails();
        });
    }

    private void updateSegmentDetails() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow >= 0) {
            String ipAddress = (String) tableModel.getValueAt(selectedRow, 0);
            List<ClientInfo> clients = clientMonitor.getActiveClients();

            for (ClientInfo client : clients) {
                if (client.getIpAddress().equals(ipAddress)) {
                    StringBuilder details = new StringBuilder();
                    details.append("Địa chỉ IP: ").append(client.getIpAddress()).append("\n");
                    details.append("Tổng Request: ").append(client.getTotalRequests()).append("\n");
                    details.append("Hoạt động cuối: ").append(client.getLastActivity().format(timeFormatter)).append("\n");
                    details.append("Thời gian không hoạt động: ").append(client.getInactiveSeconds()).append(" giây\n");
                    details.append("Trạng thái: ").append(client.isActive() ? "Đang hoạt động ●" : "Không hoạt động ○").append("\n");
                    details.append("\nCác Segment gần đây:\n");
                    details.append("─".repeat(80)).append("\n");

                    List<String> segments = client.getRecentSegments();
                    if (segments.isEmpty()) {
                        details.append("Chưa có segment nào được yêu cầu\n");
                    } else {
                        for (int i = 0; i < segments.size(); i++) {
                            details.append(String.format("%2d. %s\n", i + 1, segments.get(i)));
                        }
                    }

                    segmentDetailsArea.setText(details.toString());
                    return;
                }
            }
        }

        segmentDetailsArea.setText("Chọn một client để xem chi tiết");
    }

    public static void showUI(ClientMonitor clientMonitor) {
        // Kiểm tra xem có chạy ở chế độ headless không
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("===========================================");
            System.out.println("Đang chạy chế độ headless - Giao diện bị tắt");
            System.out.println("Để bật GUI, hãy thêm tùy chọn VM: -Djava.awt.headless=false");
            System.out.println("===========================================");
            return;
        }

        // Thiết lập giao diện FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Không thể thiết lập giao diện FlatLaf: " + e.getMessage());
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                ServerUI ui = new ServerUI(clientMonitor);
                ui.setVisible(true);
                System.out.println("Giao diện Server đã khởi động thành công");
            } catch (Exception e) {
                System.err.println("Không thể khởi động Server UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}