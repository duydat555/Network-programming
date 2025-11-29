package com.example.desktop.form;

import com.example.desktop.system.Form;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VideoPlayerForm extends Form {

    private final JFXPanel jfxPanel = new JFXPanel();

    // Khởi tạo chuỗi rỗng để tránh NullPointerException
    private String videoUrl = "";
    private String movieTitle = "";

    private WebView webView;
    private WebEngine engine;

    // --- BIẾN XỬ LÍ FULLSCREEN ---
    private boolean isFullscreen = false;
    private JWindow fullScreenWindow;
    private JFXPanel fullScreenJFXPanel;
    private WebView fullScreenWebView;
    private WebEngine fullScreenEngine;

    // --- BIẾN LƯU TRẠNG THÁI VIDEO ---
    private double savedCurrentTime = 0;

    // =================================================================
    // 1. CONSTRUCTOR RỖNG (BẮT BUỘC ĐỂ SỬA LỖI NOSUCHMETHODEXCEPTION)
    // =================================================================
    public VideoPlayerForm() {
        init();
    }

    // Constructor có tham số (giữ lại để dùng nếu cần thiết)
    public VideoPlayerForm(String movieTitle, String videoUrl) {
        this.movieTitle = movieTitle;
        this.videoUrl = videoUrl;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(java.awt.Color.BLACK);
        // Add JFXPanel một lần duy nhất tại đây
        add(jfxPanel, BorderLayout.CENTER);

        // Tắt tính năng tự động tắt của JavaFX khi đóng cửa sổ cuối cùng
        Platform.setImplicitExit(false);

        Platform.runLater(this::initFX);
    }

    private void initFX() {
        webView = new WebView();
        webView.setContextMenuEnabled(false);
        // Tạo Scene với nền đen
        Scene scene = new Scene(webView, Color.BLACK);
        jfxPanel.setScene(scene);
        engine = webView.getEngine();
        setupEngine(engine, false);
    }

    /**
     * Phương thức này được FormManager gọi khi Form hiển thị.
     * Đây là nơi an toàn nhất để bắt đầu phát video.
     */
    @Override
    public void formOpen() {
        super.formOpen();
        // Luôn reload lại player khi form mở ra
        Platform.runLater(() -> {
            if (engine != null) {
                loadCustomPlayer(engine);
            }
        });
    }

    /**
     * 2. PHƯƠNG THỨC CẬP NHẬT VIDEO MỚI (QUAN TRỌNG KHI TÁI SỬ DỤNG FORM)
     */
    public void setMovie(String title, String url) {
        this.movieTitle = title;
        this.videoUrl = url;
        this.savedCurrentTime = 0; // Reset thời gian
    }

    @Override
    public void removeNotify() {
        // Khi rời khỏi form (Back/Home), dừng video để giải phóng tài nguyên
        if (isFullscreen) exitFullscreen();
        stopVideo();
        super.removeNotify();
    }

    private void stopVideo() {
        Platform.runLater(() -> {
            if (engine != null) {
                // Load null để dừng video và xóa khỏi bộ nhớ RAM
                engine.load(null);
            }
        });
    }

    // =================================================================
    // ENGINE SETUP & FULLSCREEN
    // =================================================================

    private void setupEngine(WebEngine eng, boolean isFullScreenEngine) {
        eng.setPromptHandler((PromptData param) -> {
            String command = param.getMessage();
            if ("toggleFullscreen".equals(command)) {
                SwingUtilities.invokeLater(() -> {
                    if (isFullscreen) exitFullscreen();
                    else enterFullscreen();
                });
                return "OK";
            }
            return null;
        });
    }

    private void enterFullscreen() {
        Window windowAncestor = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (windowAncestor instanceof Frame) ? (Frame) windowAncestor : null;

        isFullscreen = true;

        Platform.runLater(() -> {
            double currentTime = 0;
            boolean isPaused = true;
            try {
                Object timeObj = engine.executeScript("video.currentTime");
                Object pausedObj = engine.executeScript("video.paused");
                if (timeObj instanceof Number) currentTime = ((Number) timeObj).doubleValue();
                if (pausedObj instanceof Boolean) isPaused = (Boolean) pausedObj;
                engine.executeScript("video.pause()");
            } catch (Exception e) { e.printStackTrace(); }

            final double startAt = currentTime;
            final boolean shouldPlay = !isPaused;

            SwingUtilities.invokeLater(() -> {
                fullScreenJFXPanel = new JFXPanel();
                fullScreenWindow = new JWindow(parentFrame);
                fullScreenWindow.setBackground(java.awt.Color.BLACK);
                fullScreenWindow.setLayout(new BorderLayout());
                fullScreenWindow.add(fullScreenJFXPanel, BorderLayout.CENTER);
                fullScreenWindow.setAlwaysOnTop(true);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                fullScreenWindow.setSize(screenSize);
                fullScreenWindow.setLocation(0, 0);

                fullScreenWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        if(isFullscreen) exitFullscreen();
                    }
                });

                Platform.runLater(() -> {
                    fullScreenWebView = new WebView();
                    fullScreenWebView.setContextMenuEnabled(false);
                    fullScreenJFXPanel.setScene(new Scene(fullScreenWebView, Color.BLACK));
                    fullScreenEngine = fullScreenWebView.getEngine();
                    setupEngine(fullScreenEngine, true);
                    loadCustomPlayer(fullScreenEngine);

                    fullScreenEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    Platform.runLater(() -> {
                                        if (fullScreenEngine == null) return;
                                        fullScreenEngine.executeScript("video.currentTime = " + startAt);
                                        if (shouldPlay) {
                                            fullScreenEngine.executeScript("video.addEventListener('seeked', function() { video.play(); }, {once:true});");
                                        }
                                    });
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                    });
                });

                fullScreenWindow.setVisible(true);
                fullScreenWindow.toFront();
            });
        });
    }

    private void exitFullscreen() {
        if (fullScreenWindow == null || fullScreenEngine == null) {
            isFullscreen = false;
            return;
        }

        Platform.runLater(() -> {
            double currentTime = 0;
            boolean isPaused = true;
            try {
                Object timeObj = fullScreenEngine.executeScript("video.currentTime");
                Object pausedObj = fullScreenEngine.executeScript("video.paused");
                if (timeObj instanceof Number) currentTime = ((Number) timeObj).doubleValue();
                if (pausedObj instanceof Boolean) isPaused = (Boolean) pausedObj;
            } catch (Exception e) { e.printStackTrace(); }

            final double syncTime = currentTime;
            final boolean shouldPlay = !isPaused;

            SwingUtilities.invokeLater(() -> {
                if (fullScreenWindow != null) {
                    fullScreenWindow.dispose();
                    fullScreenWindow = null;
                }
                isFullscreen = false;
            });

            Platform.runLater(() -> {
                if (fullScreenEngine != null) {
                    fullScreenEngine.load(null);
                    fullScreenEngine = null;
                }
                fullScreenWebView = null;
                fullScreenJFXPanel = null;

                if (engine != null) {
                    loadCustomPlayer(engine); // Reload lại player nhỏ
                    // Sync time sau khi reload xong
                    engine.getLoadWorker().stateProperty().addListener((obs, o, n) -> {
                        if (n == Worker.State.SUCCEEDED) {
                            Platform.runLater(() -> {
                                try {
                                    engine.executeScript("video.currentTime = " + syncTime);
                                    if (shouldPlay) engine.executeScript("video.play()");
                                } catch (Exception ignored){}
                            });
                        }
                    });
                }
            });
        });
    }

    // =================================================================
    // HTML PLAYER LOADER
    // =================================================================
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
                    
                    /* Overlay Title */
                    .title-overlay { position: absolute; top: 25px; left: 30px; color: white; font-size: 24px; font-weight: 600; text-shadow: 0 2px 4px rgba(0,0,0,0.8); opacity: 0.9; pointer-events: none; z-index: 10; transition: opacity 0.5s; }
                    .hide-controls .title-overlay { opacity: 0; }
                    
                    /* Controls Bar */
                    .controls-overlay { position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(to top, rgba(0,0,0,0.9) 0%, rgba(0,0,0,0.5) 50%, transparent 100%); padding: 20px 30px 25px 30px; display: flex; flex-direction: column; gap: 10px; transition: opacity 0.3s ease-in-out; opacity: 1; z-index: 20; }
                    .hide-controls .controls-overlay { opacity: 0; pointer-events: none; }
                    
                    .progress-row { display: flex; align-items: center; gap: 15px; width: 100%; margin-bottom: 5px; }
                    .time-text { color: #e0e0e0; font-size: 13px; font-weight: 500; min-width: 60px; text-align: center; font-variant-numeric: tabular-nums; }
                    
                    input[type=range] { -webkit-appearance: none; width: 100%; background: transparent; cursor: pointer; height: 4px; border-radius: 2px; }
                    input[type=range]:focus { outline: none; }
                    input[type=range]::-webkit-slider-runnable-track { width: 100%; height: 4px; cursor: pointer; border-radius: 2px; border: none; }
                    input[type=range]::-webkit-slider-thumb { -webkit-appearance: none; height: 14px; width: 14px; border-radius: 50%; background: #fff; cursor: pointer; margin-top: -5px; box-shadow: 0 0 5px rgba(0,0,0,0.5); transform: scale(0); transition: transform 0.1s; }
                    .controls-overlay:hover input[type=range]::-webkit-slider-thumb { transform: scale(1); }
                    input[type=range]::-webkit-slider-thumb:hover { transform: scale(1.3); }
                    
                    .buttons-row { display: flex; align-items: center; justify-content: space-between; }
                    .left-controls, .right-controls { display: flex; align-items: center; gap: 20px; }
                    .btn { background: none; border: none; color: white; cursor: pointer; font-size: 18px; opacity: 0.85; transition: all 0.2s; padding: 5px; min-width: 30px; }
                    .btn:hover { opacity: 1; transform: scale(1.1); text-shadow: 0 0 10px rgba(255,255,255,0.5); }
                    .btn-play { font-size: 24px; width: 35px; }
                    
                    .volume-group { display: flex; align-items: center; gap: 10px; width: 130px; }
                    #volume-slider { height: 4px; }
                </style>
            </head>
            <body>
                <div id="player-container">
                    <div class="title-overlay">__MOVIE_TITLE__</div>
                    <video id="video" onclick="togglePlay()" autoplay></video>
                    <div class="controls-overlay" id="controls">
                        <div class="progress-row">
                            <span id="current-time" class="time-text">00:00:00</span>
                            <input type="range" id="seek-bar" class="slider-progress" value="0" min="0" step="0.1">
                            <span id="duration" class="time-text">00:00:00</span>
                        </div>
                        <div class="buttons-row">
                            <div class="left-controls">
                                <button class="btn btn-play" onclick="togglePlay()"><i id="play-icon" class="fas fa-play"></i></button>
                                <button class="btn" onclick="skip(-10)"><i class="fas fa-rotate-left"></i></button>
                                <button class="btn" onclick="skip(10)"><i class="fas fa-rotate-right"></i></button>
                                <div class="volume-group">
                                    <button class="btn" onclick="toggleMute()"><i id="vol-icon" class="fas fa-volume-high"></i></button>
                                    <input type="range" id="volume-slider" class="slider-vol" min="0" max="1" step="0.05" value="1">
                                </div>
                            </div>
                            <div class="right-controls">
                                <button class="btn" onclick="callJavaFullscreen()"><i class="fas fa-expand"></i></button>
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
                    var currentTimeLabel = document.getElementById('current-time');
                    var durationLabel = document.getElementById('duration');
                    var hideTimer;

                    if (Hls.isSupported()) {
                        var hls = new Hls({
                            maxBufferLength: 30,
                            maxBufferSize: 60 * 1000 * 1000,
                        });
                        hls.loadSource(videoSrc);
                        hls.attachMedia(video);
                        // Start playing when manifest is parsed
                        hls.on(Hls.Events.MANIFEST_PARSED, function() {
                           video.play();
                        });
                    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
                        video.src = videoSrc;
                        video.addEventListener('loadedmetadata', function() {
                           video.play();
                        });
                    }
                    
                    function togglePlay() {
                        if (video.paused) video.play();
                        else video.pause();
                    }

                    video.addEventListener('play', function() {
                        playIcon.className = 'fas fa-pause';
                        container.classList.remove('hide-controls');
                        scheduleHideControls();
                    });
                    
                    video.addEventListener('pause', function() {
                        playIcon.className = 'fas fa-play';
                        container.classList.remove('hide-controls');
                    });

                    video.addEventListener('ended', function() {
                        playIcon.className = 'fas fa-rotate-right';
                        container.classList.remove('hide-controls');
                    });

                    video.addEventListener('timeupdate', function() {
                        if (!isNaN(video.duration)) {
                            seekBar.value = video.currentTime;
                            currentTimeLabel.innerText = formatTime(video.currentTime);
                            updateSliderBackground(seekBar, video.currentTime, video.duration);
                        }
                    });

                    video.addEventListener('loadedmetadata', function() {
                        seekBar.max = video.duration;
                        durationLabel.innerText = formatTime(video.duration);
                        updateSliderBackground(seekBar, 0, video.duration);
                        updateSliderBackground(volSlider, video.volume, 1);
                    });

                    seekBar.addEventListener('input', function() {
                        var val = this.value;
                        video.currentTime = val;
                        updateSliderBackground(this, val, this.max);
                    });
                    
                    volSlider.addEventListener('input', function() {
                        video.volume = this.value;
                        updateSliderBackground(this, this.value, this.max);
                        updateVolumeIcon();
                    });

                    function toggleMute() {
                        video.muted = !video.muted;
                        updateVolumeIcon();
                    }

                    function updateVolumeIcon() {
                        var icon = document.getElementById('vol-icon');
                        if (video.muted || video.volume === 0) icon.className = 'fas fa-volume-xmark';
                        else if (video.volume < 0.5) icon.className = 'fas fa-volume-low';
                        else icon.className = 'fas fa-volume-high';
                    }

                    function skip(val) { video.currentTime += val; }

                    function callJavaFullscreen() { prompt('toggleFullscreen'); }

                    function formatTime(seconds) {
                        if(isNaN(seconds)) return "00:00:00";
                        var date = new Date(0);
                        date.setSeconds(seconds);
                        var timeString = date.toISOString().substr(11, 8);
                        return timeString; 
                    }

                    function updateSliderBackground(el, val, max) {
                        var percentage = (val / max) * 100;
                        el.style.background = `linear-gradient(to right, #fff ${percentage}%, rgba(255,255,255,0.3) ${percentage}%)`;
                    }

                    function scheduleHideControls() {
                        clearTimeout(hideTimer);
                        hideTimer = setTimeout(() => {
                            if(!video.paused) container.classList.add('hide-controls');
                        }, 3000);
                    }

                    container.addEventListener('mousemove', function() {
                        container.classList.remove('hide-controls');
                        scheduleHideControls();
                    });
                    
                    updateSliderBackground(volSlider, 1, 1);
                </script>
            </body>
            </html>
            """;

        String finalHtml = htmlContent
                .replace("__VIDEO_URL__", videoUrl)
                .replace("__MOVIE_TITLE__", movieTitle);

        engine.loadContent(finalHtml);
    }
}