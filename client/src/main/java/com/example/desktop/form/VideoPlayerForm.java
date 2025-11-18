package com.example.desktop.form;

import com.example.desktop.system.Form;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VideoPlayerForm extends Form {

    private final JFXPanel jfxPanel = new JFXPanel();
    private final String videoUrl;
    private final String movieTitle;

    private WebView webView;
    private WebEngine engine;

    // --- BIẾN XỬ LÝ FULLSCREEN ---
    private boolean isFullscreen = false;
    private JDialog fullScreenDialog; // Dùng Dialog để đè lên tất cả
    private Container originalParent; // Lưu cha cũ để quay về

    public VideoPlayerForm(String movieTitle, String videoUrl) {
        this.movieTitle = movieTitle;
        this.videoUrl = videoUrl;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(java.awt.Color.BLACK);
        add(jfxPanel, BorderLayout.CENTER);

        // Khởi chạy JavaFX
        Platform.runLater(this::initFX);
    }

    private void initFX() {
        webView = new WebView();
        webView.setContextMenuEnabled(false);

        Scene scene = new Scene(webView, Color.BLACK);
        jfxPanel.setScene(scene);

        engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", new JavaConnector());
            }
        });

        loadCustomPlayer(engine);
    }

    // =================================================================
    // LỚP CẦU NỐI & LOGIC FULLSCREEN (ĐÃ SỬA ĐỔI MẠNH HƠN)
    // =================================================================
    public class JavaConnector {
        public void toggleFullscreen() {
            // Đảm bảo chạy trên luồng giao diện Swing
            SwingUtilities.invokeLater(() -> {
                if (isFullscreen) {
                    exitFullscreen();
                } else {
                    enterFullscreen();
                }
            });
        }
    }

    private void enterFullscreen() {
        System.out.println("Bắt đầu vào Fullscreen...");

        // 1. Tìm cửa sổ gốc (MainFrame) để làm cha cho Dialog (tránh lỗi minimize)
        Window windowAncestor = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (windowAncestor instanceof Frame) ? (Frame) windowAncestor : null;

        // 2. Lưu lại trạng thái cũ
        originalParent = jfxPanel.getParent();
        isFullscreen = true;

        // 3. Gỡ JFXPanel ra khỏi Form hiện tại
        this.remove(jfxPanel);
        this.revalidate();
        this.repaint(); // Vẽ lại để xóa vùng đen cũ

        // 4. Tạo JDialog Fullscreen (Thay vì JFrame)
        fullScreenDialog = new JDialog(parentFrame, true); // true = modal (chặn click bên dưới)
        fullScreenDialog.setUndecorated(true); // Không viền
        fullScreenDialog.setBackground(java.awt.Color.BLACK);
        fullScreenDialog.setLayout(new BorderLayout());
        fullScreenDialog.add(jfxPanel, BorderLayout.CENTER);

        // Cài đặt để Dialog phủ kín màn hình thủ công
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        fullScreenDialog.setSize(toolkit.getScreenSize());
        fullScreenDialog.setLocation(0, 0);
        fullScreenDialog.setAlwaysOnTop(true); // QUAN TRỌNG: Luôn nổi lên trên

        // Xử lý khi người dùng bấm Alt+F4 hoặc đóng dialog
        fullScreenDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitFullscreen();
            }
        });

        // 5. Hiển thị
        fullScreenDialog.setVisible(true);
        jfxPanel.requestFocus(); // Focus lại để nhận phím
    }

    private void exitFullscreen() {
        System.out.println("Thoát Fullscreen...");

        if (fullScreenDialog != null) {
            // 1. Gỡ JFXPanel ra khỏi Dialog
            fullScreenDialog.remove(jfxPanel);

            // 2. Tắt Dialog
            fullScreenDialog.dispose();
            fullScreenDialog = null;
        }

        // 3. Trả JFXPanel về Form cũ
        this.add(jfxPanel, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();

        isFullscreen = false;
        jfxPanel.requestFocus();
    }

    // ... GIỮ NGUYÊN PHẦN HTML/CSS KHÔNG ĐỔI ...
    private void loadCustomPlayer(WebEngine engine) {
        String htmlContent = """
            <html>
            <head>
                <meta charset="UTF-8">
                <script src='https://cdn.jsdelivr.net/npm/hls.js@latest'></script>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
                <style>
                    body { margin: 0; background: #000; overflow: hidden; font-family: 'Segoe UI', sans-serif; user-select: none; }
                    #player-container { position: relative; width: 100%; height: 100vh; display: flex; justify-content: center; align-items: center; background: #000; }
                    video { width: 100%; height: 100%; object-fit: contain; }
                    .title-overlay { position: absolute; top: 25px; left: 30px; color: white; font-size: 24px; font-weight: 600; text-shadow: 0 2px 4px rgba(0,0,0,0.8); opacity: 0.9; pointer-events: none; z-index: 10; }
                    .hide-controls .title-overlay { opacity: 0; transition: opacity 0.5s; }
                    .controls-overlay { position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(to top, rgba(0,0,0,0.95) 0%, rgba(0,0,0,0.6) 40%, transparent 100%); padding: 20px 30px 25px 30px; display: flex; flex-direction: column; gap: 10px; transition: opacity 0.3s ease-in-out; opacity: 1; z-index: 20; }
                    .hide-controls .controls-overlay { opacity: 0; pointer-events: none; }
                    .progress-row { display: flex; align-items: center; gap: 15px; width: 100%; margin-bottom: 5px; }
                    .time-text { color: #e0e0e0; font-size: 13px; font-weight: 500; min-width: 50px; text-align: center; }
                    input[type=range] { -webkit-appearance: none; width: 100%; background: transparent; cursor: pointer; }
                    input[type=range]:focus { outline: none; }
                    input[type=range]::-webkit-slider-runnable-track { width: 100%; height: 4px; cursor: pointer; background: rgba(255,255,255,0.3); border-radius: 2px; transition: height 0.1s; }
                    input[type=range]:hover::-webkit-slider-runnable-track { height: 6px; }
                    input[type=range]::-webkit-slider-thumb { height: 14px; width: 14px; border-radius: 50%; background: #fff; cursor: pointer; -webkit-appearance: none; margin-top: -5px; box-shadow: 0 0 10px rgba(0,0,0,0.5); transform: scale(1); transition: transform 0.1s; }
                    input[type=range]::-webkit-slider-thumb:hover { transform: scale(1.3); }
                    .buttons-row { display: flex; align-items: center; justify-content: space-between; }
                    .left-controls, .right-controls { display: flex; align-items: center; gap: 25px; }
                    .btn { background: none; border: none; color: white; cursor: pointer; font-size: 20px; opacity: 0.85; transition: all 0.2s; padding: 0; }
                    .btn:hover { opacity: 1; transform: scale(1.2); text-shadow: 0 0 10px rgba(255,255,255,0.6); }
                    .btn-play { font-size: 28px; width: 30px; text-align: center; }
                    .volume-group { display: flex; align-items: center; gap: 10px; width: 120px; }
                    #volume-slider { height: 3px; }
                </style>
            </head>
            <body>
                <div id="player-container">
                    <div class="title-overlay">__MOVIE_TITLE__</div>
                    <video id="video" onclick="togglePlay()"></video>
                    <div class="controls-overlay" id="controls">
                        <div class="progress-row">
                            <span id="current-time" class="time-text">00:00</span>
                            <input type="range" id="seek-bar" value="0" min="0" step="0.1">
                            <span id="duration" class="time-text">00:00</span>
                        </div>
                        <div class="buttons-row">
                            <div class="left-controls">
                                <button class="btn btn-play" onclick="togglePlay()"><i id="play-icon" class="fas fa-play"></i></button>
                                <button class="btn" onclick="skip(-10)" title="Lùi 10s"><i class="fas fa-rotate-left"></i></button>
                                <button class="btn" onclick="skip(10)" title="Tiến 10s"><i class="fas fa-rotate-right"></i></button>
                                <div class="volume-group">
                                    <button class="btn" onclick="toggleMute()"><i id="vol-icon" class="fas fa-volume-high"></i></button>
                                    <input type="range" id="volume-slider" min="0" max="1" step="0.1" value="1">
                                </div>
                            </div>
                            <div class="right-controls">
                                <button class="btn" onclick="callJavaFullscreen()" title="Toàn màn hình"><i class="fas fa-expand"></i></button>
                            </div>
                        </div>
                    </div>
                </div>

                <script>
                    var video = document.getElementById('video');
                    var videoSrc = '__VIDEO_URL__'; 
                    var playIcon = document.getElementById('play-icon');
                    var seekBar = document.getElementById('seek-bar');
                    var volSlider = document.getElementById('volume-slider');
                    var container = document.getElementById('player-container');
                    var hideTimer;

                    if (Hls.isSupported()) {
                        var hls = new Hls();
                        hls.loadSource(videoSrc);
                        hls.attachMedia(video);
                    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
                        video.src = videoSrc;
                    }

                    function togglePlay() {
                        if (video.paused) { video.play(); playIcon.className = 'fas fa-pause'; } 
                        else { video.pause(); playIcon.className = 'fas fa-play'; }
                    }

                    function callJavaFullscreen() {
                        if (window.javaApp) {
                            window.javaApp.toggleFullscreen();
                        }
                    }

                    video.addEventListener('timeupdate', function() {
                        if (!isNaN(video.duration)) {
                            seekBar.value = video.currentTime;
                            document.getElementById('current-time').innerText = formatTime(video.currentTime);
                        }
                    });
                    
                    video.addEventListener('loadedmetadata', function() { 
                        seekBar.max = video.duration;
                        document.getElementById('duration').innerText = formatTime(video.duration);
                    });
                    
                    video.addEventListener('ended', function() {
                        playIcon.className = 'fas fa-rotate-right'; 
                        container.classList.remove('hide-controls');
                    });

                    seekBar.addEventListener('input', function() { video.currentTime = this.value; });
                    volSlider.addEventListener('input', function() { video.volume = this.value; });
                    
                    function toggleMute() {
                        video.muted = !video.muted;
                        document.getElementById('vol-icon').className = video.muted ? 'fas fa-volume-xmark' : 'fas fa-volume-high';
                    }

                    function skip(val) { video.currentTime += val; }

                    function formatTime(seconds) {
                        var m = Math.floor(seconds / 60);
                        var s = Math.floor(seconds % 60);
                        return (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s);
                    }

                    container.addEventListener('mousemove', function() {
                        container.classList.remove('hide-controls');
                        clearTimeout(hideTimer);
                        hideTimer = setTimeout(() => { 
                            if(!video.paused) container.classList.add('hide-controls'); 
                        }, 3000);
                    });
                </script>
            </body>
            </html>
            """;

        String finalHtml = htmlContent
                .replace("__VIDEO_URL__", videoUrl)
                .replace("__MOVIE_TITLE__", movieTitle);

        engine.loadContent(finalHtml);
    }

    @Override
    public void removeNotify() {
        // Quan trọng: Đảm bảo tắt Dialog nếu người dùng chuyển trang khi đang Fullscreen
        if (isFullscreen) {
            exitFullscreen();
        }

        Platform.runLater(() -> {
            if (engine != null) engine.load(null);
            if (webView != null) webView.getEngine().loadContent("");
            jfxPanel.setScene(null);
        });
        super.removeNotify();
    }
}