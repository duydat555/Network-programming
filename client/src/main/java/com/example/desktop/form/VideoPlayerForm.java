package com.example.desktop.form;

import com.example.desktop.system.Form;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;

public class VideoPlayerForm extends Form {

    private final JFXPanel jfxPanel = new JFXPanel();
    private final String videoUrl;
    private final String movieTitle;

    private WebView webView;
    private WebEngine engine;

    // --- BIẾN XỬ LÝ FULLSCREEN ---
    private boolean isFullscreen = false;
    private JWindow fullScreenWindow;
    private JFXPanel fullScreenJFXPanel; // JFXPanel riêng cho fullscreen
    private WebView fullScreenWebView;
    private WebEngine fullScreenEngine;

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

        // Set up prompt handler to communicate with JavaScript
        engine.setPromptHandler((PromptData param) -> {
            String command = param.getMessage();
            if ("toggleFullscreen".equals(command)) {
                SwingUtilities.invokeLater(() -> {
                    if (isFullscreen) {
                        exitFullscreen();
                    } else {
                        enterFullscreen();
                    }
                });
                return "OK";
            }
            return null;
        });

        loadCustomPlayer(engine);
    }

    // =================================================================
    // LOGIC FULLSCREEN
    // =================================================================

    private void enterFullscreen() {
        System.out.println("Bắt đầu vào Fullscreen...");

        // 1. Tìm cửa sổ gốc (MainFrame) để làm owner
        Window windowAncestor = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (windowAncestor instanceof Frame) ? (Frame) windowAncestor : null;

        // 2. Đánh dấu trạng thái fullscreen
        isFullscreen = true;

        // 3. Lấy thời gian hiện tại của video gốc
        final double[] currentTime = {0};
        final boolean[] isPaused = {true};

        Platform.runLater(() -> {
            String script = "video.currentTime";
            Object time = engine.executeScript(script);
            if (time != null) {
                currentTime[0] = ((Number) time).doubleValue();
            }

            Object pausedState = engine.executeScript("video.paused");
            if (pausedState != null) {
                isPaused[0] = (Boolean) pausedState;
            }

            // TẠM DỪNG video gốc để giảm lag
            engine.executeScript("video.pause()");

            System.out.println("Current video time: " + currentTime[0] + ", paused: " + isPaused[0]);

            // 4. Tạo JFXPanel mới cho fullscreen
            SwingUtilities.invokeLater(() -> {
                fullScreenJFXPanel = new JFXPanel();

                // 5. Tạo JWindow Fullscreen
                fullScreenWindow = new JWindow(parentFrame);
                fullScreenWindow.setBackground(java.awt.Color.BLACK);
                fullScreenWindow.setLayout(new BorderLayout());
                fullScreenWindow.add(fullScreenJFXPanel, BorderLayout.CENTER);

                // Set fullscreen size
                fullScreenWindow.setAlwaysOnTop(true);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                fullScreenWindow.setSize(screenSize);
                fullScreenWindow.setLocation(0, 0);

                // 6. Khởi tạo JavaFX cho fullscreen panel
                Platform.runLater(() -> {
                    fullScreenWebView = new WebView();
                    fullScreenWebView.setContextMenuEnabled(false);

                    Scene scene = new Scene(fullScreenWebView, Color.BLACK);
                    fullScreenJFXPanel.setScene(scene);

                    fullScreenEngine = fullScreenWebView.getEngine();

                    // Set up prompt handler cho fullscreen
                    fullScreenEngine.setPromptHandler((PromptData param) -> {
                        String command = param.getMessage();
                        if ("toggleFullscreen".equals(command)) {
                            SwingUtilities.invokeLater(this::exitFullscreen);
                            return "OK";
                        }
                        return null;
                    });

                    // Load player HTML
                    loadCustomPlayer(fullScreenEngine);

                    // Đợi video load xong rồi seek đến vị trí cũ
                    final double timeToSeek = currentTime[0];
                    final boolean shouldPlay = !isPaused[0];

                    fullScreenEngine.getLoadWorker().stateProperty().addListener((_obs, _oldState, newState) -> {
                        if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                            Platform.runLater(() -> {
                                try {
                                    // Kiểm tra null trước khi thao tác
                                    if (fullScreenEngine == null) {
                                        System.out.println("Fullscreen engine is null, user may have exited fullscreen");
                                        return;
                                    }

                                    // Đợi video element ready
                                    Thread.sleep(800);

                                    // Kiểm tra lại sau khi sleep
                                    if (fullScreenEngine == null) {
                                        return;
                                    }

                                    // Seek đến vị trí cũ
                                    if (timeToSeek > 0) {
                                        fullScreenEngine.executeScript("video.currentTime = " + timeToSeek);

                                        // Đợi video buffer tại vị trí mới
                                        if (fullScreenEngine != null) {
                                            fullScreenEngine.executeScript("""
                                                video.addEventListener('canplay', function onCanPlay() {
                                                    video.removeEventListener('canplay', onCanPlay);
                                                    console.log('Video ready to play at: ' + video.currentTime);
                                                }, { once: true });
                                            """);
                                        }
                                    }

                                    // Tiếp tục phát nếu đang phát
                                    if (shouldPlay) {
                                        // Đợi thêm một chút để video buffer
                                        Thread.sleep(500);

                                        // Kiểm tra null trước khi play
                                        if (fullScreenEngine != null) {
                                            fullScreenEngine.executeScript("video.play()");
                                        }
                                    }

                                    System.out.println("Fullscreen video synced to time: " + timeToSeek);
                                } catch (Exception e) {
                                    System.err.println("Error syncing fullscreen video: " + e.getMessage());
                                }
                            });
                        }
                    });
                });

                // 7. Hiển thị window
                SwingUtilities.invokeLater(() -> {
                    fullScreenWindow.setVisible(true);
                    fullScreenWindow.toFront();
                    fullScreenJFXPanel.requestFocusInWindow();
                });

                System.out.println("Fullscreen window đã hiển thị");
            });
        });
    }

    private void exitFullscreen() {
        System.out.println("Thoát Fullscreen...");

        if (fullScreenWindow == null || fullScreenEngine == null) {
            System.err.println("Fullscreen components already cleaned up");
            isFullscreen = false;
            return;
        }

        // 1. Lấy thời gian hiện tại từ fullscreen video TRƯỚC KHI cleanup
        Platform.runLater(() -> {
            try {
                Object time = fullScreenEngine.executeScript("video.currentTime");
                Object pausedState = fullScreenEngine.executeScript("video.paused");

                final double currentTime = (time != null) ? ((Number) time).doubleValue() : 0;
                final boolean isPaused = (pausedState != null) ? (Boolean) pausedState : true;

                System.out.println("Fullscreen video time before exit: " + currentTime + ", paused: " + isPaused);

                // 2. Cleanup fullscreen window TRƯỚC (để ẩn window ngay)
                SwingUtilities.invokeLater(() -> {
                    if (fullScreenWindow != null) {
                        fullScreenWindow.setVisible(false);
                        fullScreenWindow.remove(fullScreenJFXPanel);
                        fullScreenWindow.dispose();
                        fullScreenWindow = null;
                    }

                    isFullscreen = false;
                    System.out.println("Fullscreen window disposed");
                });

                // 3. Sync time về video gốc SAU KHI đã lấy giá trị
                Platform.runLater(() -> {
                    try {
                        // Kiểm tra engine gốc còn tồn tại không
                        if (engine == null) {
                            System.err.println("Original engine is null, cannot sync time");
                            return;
                        }

                        // Sync thời gian
                        if (currentTime > 0) {
                            engine.executeScript("video.currentTime = " + currentTime);
                        }

                        // Tiếp tục phát nếu đang phát
                        if (!isPaused) {
                            engine.executeScript("video.play()");
                        }

                        System.out.println("Synced time back to original video: " + currentTime);

                        // 4. Cleanup fullscreen JavaFX components SAU CÙNG
                        Platform.runLater(() -> {
                            try {
                                if (fullScreenEngine != null) {
                                    fullScreenEngine.load(null);
                                    fullScreenEngine = null;
                                }
                                if (fullScreenWebView != null) {
                                    fullScreenWebView = null;
                                }
                                if (fullScreenJFXPanel != null) {
                                    fullScreenJFXPanel.setScene(null);
                                    fullScreenJFXPanel = null;
                                }
                                System.out.println("Fullscreen JavaFX resources cleaned up");
                            } catch (Exception cleanupEx) {
                                System.err.println("Error during cleanup: " + cleanupEx.getMessage());
                            }
                        });

                    } catch (Exception e) {
                        System.err.println("Error syncing video time: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("Error getting fullscreen video state: " + e.getMessage());
                // Cleanup anyway
                SwingUtilities.invokeLater(() -> {
                    if (fullScreenWindow != null) {
                        fullScreenWindow.setVisible(false);
                        fullScreenWindow.dispose();
                        fullScreenWindow = null;
                    }
                    isFullscreen = false;
                });
            }
        });
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

                    // Tối ưu HLS.js config để giảm lag
                    if (Hls.isSupported()) {
                        var hls = new Hls({
                            maxBufferLength: 30,
                            maxMaxBufferLength: 60,
                            maxBufferSize: 60 * 1000 * 1000,
                            maxBufferHole: 0.5,
                            lowLatencyMode: false,
                            backBufferLength: 90
                        });
                        hls.loadSource(videoSrc);
                        hls.attachMedia(video);
                        
                        hls.on(Hls.Events.MANIFEST_PARSED, function() {
                            console.log('HLS manifest loaded');
                        });
                    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
                        video.src = videoSrc;
                    }
                    
                    // Preload video để giảm lag khi seek
                    video.preload = 'auto';

                    function togglePlay() {
                        if (video.paused) { video.play(); playIcon.className = 'fas fa-pause'; }
                        else { video.pause(); playIcon.className = 'fas fa-play'; }
                    }

                    function callJavaFullscreen() {
                        prompt('toggleFullscreen');
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
        // Quan trọng: Đảm bảo tắt Window nếu người dùng chuyển trang khi đang Fullscreen
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