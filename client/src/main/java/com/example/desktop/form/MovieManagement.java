package com.example.desktop.form;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.system.Form;
import com.example.desktop.system.FormManager;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Location;
import raven.modal.option.ModalBorderOption;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class MovieManagement extends Form {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton editButton;
    private JButton deleteButton;

    private List<AuthApiClient.Movie> currentMovies; // Lưu danh sách phim hiện tại
    public static final String MOVIE_DIALOG_ID = "movie_dialog";

    public MovieManagement() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 10", "[fill]", "[][grow, fill]"));

        // 1. Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton refreshButton = new JButton("Tải lại");
        refreshButton.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/refresh.svg", 0.8f));

        JButton addButton = new JButton("Thêm");
        addButton.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/plus.svg", 0.8f));

        // Khởi tạo nút Sửa và Xóa (đã khai báo ở trên)
        editButton = new JButton("Sửa");
        editButton.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/edit.svg", 0.8f));
        editButton.setEnabled(false); // Vô hiệu hóa ban đầu

        deleteButton = new JButton("Xóa");
        deleteButton.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/trash.svg", 0.8f));
        deleteButton.setEnabled(false); // Vô hiệu hóa ban đầu

        toolBar.add(refreshButton);
        toolBar.addSeparator();
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);

        // 2. Bảng dữ liệu
        String[] columnNames = {"ID", "Tên phim", "Năm", "Thời lượng (phút)", "Thể loại"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // 3. Thêm components
        add(toolBar, "wrap");
        add(scrollPane);

        // 4. Gán sự kiện
        refreshButton.addActionListener(e -> loadMovies());
        addButton.addActionListener(e -> openMovieDialog(null)); // null = Thêm mới
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> onDeleteMovie());

        // Thêm sự kiện lắng nghe việc chọn hàng trên bảng
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = table.getSelectedRow() != -1;
                editButton.setEnabled(rowSelected);
                deleteButton.setEnabled(rowSelected);
            }
        });
    }

    @Override
    public void formInit() {
        System.out.println("Movie Management initialized, loading movies...");
        loadMovies();
    }

    private void loadMovies() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        editButton.setEnabled(false); // Tắt nút khi đang tải lại
        deleteButton.setEnabled(false);

        SwingWorker<List<AuthApiClient.Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AuthApiClient.Movie> doInBackground() throws Exception {
                return AuthApiClient.getMovies();
            }

            @Override
            protected void done() {
                try {
                    currentMovies = get(); // Lưu lại danh sách phim
                    for (AuthApiClient.Movie movie : currentMovies) {
                        String genres = String.join(", ", movie.genres());
                        tableModel.addRow(new Object[]{
                                movie.id(),
                                movie.title(),
                                movie.year(),
                                movie.durationMin(),
                                genres
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            MovieManagement.this,
                            "Không thể tải danh sách phim: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Mở dialog để Thêm (movie == null) hoặc Sửa (movie != null)
     */
    private void openMovieDialog(AuthApiClient.Movie movie) {

        MovieDialog movieDialog = new MovieDialog(movie, () -> {
            loadMovies(); // callback sau khi lưu
        });

        String title = (movie == null) ? "Thêm phim mới" : "Chỉnh sửa phim";

        ModalBorderOption borderOption = new ModalBorderOption(); // chỉ tạo border, không set location

        // Hiển thị modal
        ModalDialog.showModal(
                FormManager.getFrame(),
                new SimpleModalBorder(movieDialog, title, borderOption),
                ModalDialog.createOption(), // mặc định sẽ căn giữa
                MOVIE_DIALOG_ID
        );
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return; // Không có gì được chọn

        // Lấy phim tương ứng từ danh sách currentMovies
        // (An toàn hơn là lấy ID từ bảng và tìm)
        Long id = (Long) tableModel.getValueAt(selectedRow, 0); // Lấy ID từ cột 0
        AuthApiClient.Movie movieToEdit = currentMovies.stream()
                .filter(m -> m.id() == id)
                .findFirst()
                .orElse(null);

        if (movieToEdit != null) {
            openMovieDialog(movieToEdit); // Gọi dialog ở chế độ Sửa
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu phim.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDeleteMovie() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);

        // Xác nhận xóa
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phim '" + title + "' (ID: " + id + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Thực hiện xóa trong SwingWorker
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return AuthApiClient.deleteMovie(id);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(MovieManagement.this, "Xóa phim thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        loadMovies(); // Tải lại bảng
                    } else {
                        throw new Exception("Server từ chối yêu cầu.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MovieManagement.this, "Lỗi khi xóa phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}