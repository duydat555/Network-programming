package com.example.desktop.system;

import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Form
        extends JPanel {
    private LookAndFeel oldTheme = UIManager.getLookAndFeel();

    public Form() {
        this.init();
    }

    private void init() {
    }

    public void formInit() {
    }

    public void formOpen() {
    }

    public void formRefresh() {
    }

    protected final void formCheck() {
        if (this.oldTheme != UIManager.getLookAndFeel()) {
            this.oldTheme = UIManager.getLookAndFeel();
            SwingUtilities.updateComponentTreeUI(this);
        }
    }
}

