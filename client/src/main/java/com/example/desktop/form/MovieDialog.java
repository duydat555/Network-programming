package com.example.desktop.form;

import com.example.desktop.api.AuthApiClient;
import static com.example.desktop.form.MovieManagement.MOVIE_DIALOG_ID;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MovieDialog extends JPanel {

    private final JTextField titleField;
    private final JTextArea descriptionArea;
    private final JTextField yearField;
    private final JTextField durationField;
    private final JTextField videoUrlField;
    private final JTextField posterUrlField;
    private final JTextField genreIdsField;
    private final JButton saveButton;
    private final JButton cancelButton;

    private final AuthApiClient.Movie movieToEdit;
    private final Runnable refreshCallback;

    public MovieDialog(AuthApiClient.Movie movieToEdit, Runnable refreshCallback) {
        this.movieToEdit = movieToEdit;
        this.refreshCallback = refreshCallback;

        // Layout: 2 cột
        setLayout(new MigLayout("wrap 2, fillx, insets 15", "[right, 100::][grow, fill]"));

        // Khởi tạo components
        titleField = new JTextField();
        descriptionArea = new JTextArea(5, 30);
        yearField = new JTextField();
        durationField = new JTextField();
        videoUrlField = new JTextField();
        posterUrlField = new JTextField();
        genreIdsField = new JTextField();
        saveButton = new JButton("Lưu");
        cancelButton = new JButton("Hủy");

        // Thêm vào panel
        add(new JLabel("Tên phim:"));
        add(titleField, "wrap");

        add(new JLabel("Mô tả:"));
        add(new JScrollPane(descriptionArea), "wrap");

        add(new JLabel("Năm:"));
        add(yearField, "wrap");

        add(new JLabel("Thời lượng (phút):"));
        add(durationField, "wrap");

        add(new JLabel("Video URL:"));
        add(videoUrlField, "wrap");
        videoUrlField.setToolTipText("Nhập link video chính (mặc định)");

        add(new JLabel("Poster URL:"));
        add(posterUrlField, "wrap");

        add(new JLabel("Thể loại:"));
        add(genreIdsField, "wrap");
        genreIdsField.setToolTipText("Nhập các thể loại cách nhau bằng dấu phẩy, ví dụ: Hành động, Phiêu lưu");

        // Panel cho các nút
        JPanel buttonPanel = new JPanel(new MigLayout("al right", "[][]"));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, "span 2, growx, gaptop 20");

        // Gán sự kiện
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> ModalDialog.closeModal(MOVIE_DIALOG_ID));

        // Tự động điền dữ liệu nếu là chế độ Sửa
        if (movieToEdit != null) {
            populateData();
        }
    }

    private void populateData() {
        titleField.setText(movieToEdit.title());
        descriptionArea.setText(movieToEdit.description());
        yearField.setText(String.valueOf(movieToEdit.year()));
        durationField.setText(String.valueOf(movieToEdit.durationMin()));

        // --- ĐOẠN ĐƯỢC SỬA (QUAN TRỌNG) ---
        // Xử lý lấy Video URL từ List<VideoQuality>
        String displayUrl = "";
        if (movieToEdit.videoQualities() != null && !movieToEdit.videoQualities().isEmpty()) {
            // 1. Cố gắng tìm video có isDefault = true
            Optional<AuthApiClient.VideoQuality> defaultVideo = movieToEdit.videoQualities().stream()
                    .filter(AuthApiClient.VideoQuality::isDefault)
                    .findFirst();

            if (defaultVideo.isPresent()) {
                displayUrl = defaultVideo.get().videoUrl();
            } else {
                // 2. Nếu không có default, lấy video đầu tiên trong danh sách
                displayUrl = movieToEdit.videoQualities().get(0).videoUrl();
            }
        }
        videoUrlField.setText(displayUrl);
        // ----------------------------------

        posterUrlField.setText(movieToEdit.posterUrl());

        // Xử lý Genres
        String genres = "";
        if (movieToEdit.genres() != null) {
            genres = movieToEdit.genres().stream()
                    .collect(Collectors.joining(", "));
        }
        genreIdsField.setText(genres);
    }

    private void onSave() {
        // 1. Lấy dữ liệu từ form
        try {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            // Xử lý trường hợp nhập rỗng cho số
            int year = yearField.getText().isEmpty() ? 0 : Integer.parseInt(yearField.getText());
            int duration = durationField.getText().isEmpty() ? 0 : Integer.parseInt(durationField.getText());
            String videoUrlInput = videoUrlField.getText(); // Người dùng nhập 1 link String
            String posterUrl = posterUrlField.getText();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên phim không được để trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse danh sách thể loại
            List<String> genresList = Arrays.stream(genreIdsField.getText().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // 4. Tạo đối tượng NewMovie
            // LƯU Ý: Đảm bảo AuthApiClient.NewMovie constructor nhận tham số videoUrl là String.
            // Nếu server cần List, logic ở đây cần sửa đổi hoặc constructor NewMovie tự convert String thành List.
            AuthApiClient.NewMovie movieData = new AuthApiClient.NewMovie(
                    title, description, year, duration, videoUrlInput, null, posterUrl, genresList
            );

            // 5. Gọi API trong SwingWorker
            setFieldsEnabled(false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (movieToEdit == null) {
                        return AuthApiClient.createMovie(movieData);
                    } else {
                        return AuthApiClient.updateMovie(movieToEdit.id(), movieData);
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            String message = (movieToEdit == null) ? "Thêm phim thành công!" : "Cập nhật phim thành công!";
                            JOptionPane.showMessageDialog(MovieDialog.this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            if (refreshCallback != null) refreshCallback.run();
                            ModalDialog.closeModal(MOVIE_DIALOG_ID);
                        } else {
                            throw new Exception("Server từ chối yêu cầu (trả về false).");
                        }
                    } catch (Exception e) {
                        String action = (movieToEdit == null) ? "thêm" : "cập nhật";
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(MovieDialog.this, "Lỗi khi " + action + " phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setFieldsEnabled(true);
                    }
                }
            };
            worker.execute();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Năm và Thời lượng phải là số nguyên.", "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        titleField.setEnabled(enabled);
        descriptionArea.setEnabled(enabled);
        yearField.setEnabled(enabled);
        durationField.setEnabled(enabled);
        videoUrlField.setEnabled(enabled);
        posterUrlField.setEnabled(enabled);
        genreIdsField.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }
}