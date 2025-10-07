package com.example.desktop;

import com.formdev.flatlaf.FlatDarculaLaf;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class Test {
    private static EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private static JFrame frame;
    private static JLabel timeLabel;
    private static JSlider seekBar;
    private static Timer timer;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Video Player");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.setLayout(new BorderLayout());

            // Initialize VLCJ media player
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            frame.add(mediaPlayerComponent, BorderLayout.CENTER);

            // Control panel (bottom bar like YouTube)
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BorderLayout());
            controlPanel.setPreferredSize(new Dimension(1000, 60));
            controlPanel.setBackground(new Color(20, 20, 20));

            // Playback controls
            JPanel playbackPanel = new JPanel();
            playbackPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            playbackPanel.setOpaque(false);

            JButton openButton = new JButton("Open URL/File");
            JButton rewindButton = new JButton("⏪");
            JButton playButton = new JButton("▶");
            JButton fastForwardButton = new JButton("⏩");
            timeLabel = new JLabel("00:00 / 00:00");
            seekBar = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
            seekBar.setPreferredSize(new Dimension(600, 20));
            seekBar.setBackground(new Color(20, 20, 20));
            seekBar.setForeground(Color.WHITE);
            seekBar.setEnabled(false); // Enable when media is playing

            JButton volumeButton = new JButton("🔊");
            JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
            volumeSlider.setPreferredSize(new Dimension(100, 20));

            // Action listeners
            playButton.addActionListener(e -> {
                if (mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
                    mediaPlayerComponent.mediaPlayer().controls().pause();
                    playButton.setText("▶");
                } else {
                    mediaPlayerComponent.mediaPlayer().controls().play();
                    playButton.setText("⏸");
                    startTimeUpdate();
                    seekBar.setEnabled(true);
                }
            });

            rewindButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000));
            fastForwardButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().skipTime(10000));

            volumeButton.addActionListener(e -> {
                if (volumeSlider.isVisible()) {
                    volumeSlider.setVisible(false);
                } else {
                    volumeSlider.setVisible(true);
                }
            });

            volumeSlider.addChangeListener(e -> {
                mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeSlider.getValue());
            });

            // Open URL/File button action
            openButton.addActionListener(e -> {
                Object[] options = {"Open File", "Open URL"};
                int choice = JOptionPane.showOptionDialog(frame,
                        "Choose input type:", "Open Media",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                if (choice == 0) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                            "Video Files", "mp4", "avi", "mkv", "mov"));
                    int result = fileChooser.showOpenDialog(frame);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        mediaPlayerComponent.mediaPlayer().media().prepare(selectedFile.getAbsolutePath());
                        mediaPlayerComponent.mediaPlayer().controls().play();
                        startTimeUpdate();
                        seekBar.setEnabled(true);
                    }
                } else if (choice == 1) {
                    String url = JOptionPane.showInputDialog(frame, "Enter video URL:",
                            "Open URL", JOptionPane.PLAIN_MESSAGE);
                    if (url != null && !url.trim().isEmpty()) {
                        try {
                            mediaPlayerComponent.mediaPlayer().media().prepare(url);
                            mediaPlayerComponent.mediaPlayer().controls().play();
                            startTimeUpdate();
                            seekBar.setEnabled(true);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(frame,
                                    "Error playing URL: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            // Add components to playback panel
            playbackPanel.add(openButton);
            playbackPanel.add(rewindButton);
            playbackPanel.add(playButton);
            playbackPanel.add(fastForwardButton);
            playbackPanel.add(timeLabel);

            // Add seek bar
            JPanel seekPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            seekPanel.setOpaque(false);
            seekPanel.add(seekBar);

            // Add volume controls
            JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            volumePanel.setOpaque(false);
            volumePanel.add(volumeButton);
            volumePanel.add(volumeSlider);
            volumeSlider.setVisible(false);

            // Add panels to control panel
            controlPanel.add(playbackPanel, BorderLayout.WEST);
            controlPanel.add(seekPanel, BorderLayout.CENTER);
            controlPanel.add(volumePanel, BorderLayout.EAST);

            // Add control panel to frame
            frame.add(controlPanel, BorderLayout.SOUTH);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Initialize timer for updating time and seek bar
            timer = new Timer(1000, e -> {
                long currentTime = mediaPlayerComponent.mediaPlayer().status().time();
                long duration = mediaPlayerComponent.mediaPlayer().media().info().duration();
                updateTimeLabel(currentTime, duration);
                if (duration > 0) {
                    seekBar.setMaximum((int) duration);
                    seekBar.setValue((int) currentTime);
                }
            });
            timer.setRepeats(true);
        });
    }

    private static void startTimeUpdate() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private static void stopTimeUpdate() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    private static void updateTimeLabel(long currentTime, long duration) {
        String current = formatTime(currentTime);
        String total = (duration > 0) ? formatTime(duration) : "00:00";
        timeLabel.setText(current + " / " + total);
    }

    private static String formatTime(long millis) {
        if (millis < 0) return "00:00";
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}