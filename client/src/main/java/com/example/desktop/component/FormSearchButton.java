package com.example.desktop.component;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

public class FormSearchButton extends JButton {
    public FormSearchButton() {
        super("Tìm kiếm nhanh...", new FlatSVGIcon("raven/modal/demo/icons/search.svg", 0.4F));
        this.init();
    }

    private void init() {
        this.setLayout(new MigLayout("insets 0,al trailing,filly", "", "[center]"));
        this.setHorizontalAlignment(10);
        this.putClientProperty("FlatLaf.style", "margin:5,7,5,10;arc:10;borderWidth:0;focusWidth:0;innerFocusWidth:0;[light]background:shade($Panel.background,10%);[dark]background:tint($Panel.background,10%);[light]foreground:tint($Button.foreground,40%);[dark]foreground:shade($Button.foreground,30%);");
        JLabel label = new JLabel("Ctrl F");
        label.putClientProperty("FlatLaf.style", "[light]foreground:tint($Button.foreground,40%);[dark]foreground:shade($Button.foreground,30%);");
        this.add(label);
    }
}