package com.example.desktop.component;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatMenuArrowIcon;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import net.miginfocom.swing.MigLayout;
import raven.modal.Drawer;
import raven.modal.ModalDialog;
import raven.modal.component.ModalContainer;
import raven.modal.demo.icons.SVGIconUIColor;
import raven.modal.demo.menu.MyMenuValidation;
import com.example.desktop.system.Form;
import raven.modal.demo.utils.DemoPreferences;
import raven.modal.demo.utils.SystemForm;

public class FormSearchPanel extends JPanel {
    private LookAndFeel oldTheme = UIManager.getLookAndFeel();
    private final int SEARCH_MAX_LENGTH = 50;
    private final Map<SystemForm, Class<? extends Form>> formsMap;
    private final List<Item> listItems = new ArrayList();
    private JTextField textSearch;
    private JPanel panelResult;

    public FormSearchPanel(Map<SystemForm, Class<? extends Form>> formsMap) {
        this.formsMap = formsMap;
        this.init();
    }

    private void init() {
        this.setLayout(new MigLayout("fillx,insets 0,wrap", "[fill,500]"));
        this.textSearch = new JTextField();
        this.panelResult = new JPanel(new MigLayout("insets 3 10 3 10,fillx,wrap", "[fill]"));
        this.textSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        this.textSearch.putClientProperty("JTextField.leadingIcon", new FlatSVGIcon("raven/modal/demo/icons/search.svg", 0.4F));
        this.textSearch.putClientProperty("FlatLaf.style", "border:3,3,3,3;background:null;showClearButton:true;");
        this.add(this.textSearch, "gap 17 17 0 0");
        this.add(new JSeparator(), "height 2!");
        JScrollPane scrollPane = new JScrollPane(this.panelResult);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setHorizontalScrollBarPolicy(31);
        scrollPane.getVerticalScrollBar().putClientProperty("FlatLaf.style", "trackArc:$ScrollBar.thumbArc;thumbInsets:0,3,0,3;trackInsets:0,3,0,3;width:12;");
        this.add(scrollPane);
        this.installSearchField();
    }

    public final void formCheck() {
        if (this.oldTheme != UIManager.getLookAndFeel()) {
            this.oldTheme = UIManager.getLookAndFeel();
            SwingUtilities.updateComponentTreeUI(this);
        }

    }

    private void installSearchField() {
        this.textSearch.setDocument(new PlainDocument() {
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (this.getLength() + str.length() <= 50) {
                    super.insertString(offs, str, a);
                }

            }
        });
        this.textSearch.getDocument().addDocumentListener(new DocumentListener() {
            private String text;

            public void insertUpdate(DocumentEvent e) {
                this.search();
            }

            public void removeUpdate(DocumentEvent e) {
                this.search();
            }

            public void changedUpdate(DocumentEvent e) {
                this.search();
            }

            private void search() {
                String st = FormSearchPanel.this.textSearch.getText().trim().toLowerCase();
                if (!st.equals(this.text)) {
                    this.text = st;
                    FormSearchPanel.this.panelResult.removeAll();
                    FormSearchPanel.this.listItems.clear();
                    if (st.isEmpty()) {
                        FormSearchPanel.this.showRecentResult();
                    } else {
                        for(Map.Entry<SystemForm, Class<? extends Form>> entry : FormSearchPanel.this.formsMap.entrySet()) {
                            SystemForm s = (SystemForm)entry.getKey();
                            if ((s.name().toLowerCase().contains(st) || s.description().toLowerCase().contains(st) || this.checkTags(s.tags(), st)) && MyMenuValidation.validation((Class)entry.getValue())) {
                                Item item = FormSearchPanel.this.new Item(s, (Class)entry.getValue(), false, false);
                                FormSearchPanel.this.panelResult.add(item);
                                FormSearchPanel.this.listItems.add(item);
                            }
                        }

                        if (!FormSearchPanel.this.listItems.isEmpty()) {
                            FormSearchPanel.this.setSelected(0);
                        } else {
                            FormSearchPanel.this.panelResult.add(FormSearchPanel.this.createNoResult(st));
                        }

                        FormSearchPanel.this.panelResult.repaint();
                        FormSearchPanel.this.updateLayout();
                    }
                }

            }

            private boolean checkTags(String[] tags, String st) {
                return tags.length == 0 ? false : Arrays.stream(tags).anyMatch((s) -> s.contains(st));
            }
        });
        this.textSearch.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case 10:
                        FormSearchPanel.this.showForm();
                        break;
                    case 38:
                        FormSearchPanel.this.move(true);
                        break;
                    case 40:
                        FormSearchPanel.this.move(false);
                }

            }
        });
    }

    private void updateLayout() {
        Container container = SwingUtilities.getAncestorOfClass(ModalContainer.class, this);
        if (container != null) {
            container.revalidate();
        }

    }

    private void showForm() {
        int index = this.getSelectedIndex();
        if (index != -1) {
            ((Item)this.listItems.get(index)).showForm();
        }

    }

    private void setSelected(int index) {
        for(int i = 0; i < this.listItems.size(); ++i) {
            ((Item)this.listItems.get(i)).setSelected(index == i);
        }

    }

    private int getSelectedIndex() {
        for(int i = 0; i < this.listItems.size(); ++i) {
            if (((Item)this.listItems.get(i)).isSelected()) {
                return i;
            }
        }

        return -1;
    }

    private void move(boolean up) {
        if (!this.listItems.isEmpty()) {
            int index = this.getSelectedIndex();
            int size = this.listItems.size();
            if (index == -1) {
                if (up) {
                    index = this.listItems.size() - 1;
                } else {
                    index = 0;
                }
            } else if (up) {
                index = index == 0 ? size - 1 : index - 1;
            } else {
                index = index == size - 1 ? 0 : index + 1;
            }

            this.setSelected(index);
        }
    }

    private void showRecentResult() {
        List<Item> recentSearch = this.getRecentSearch(false);
        List<Item> favoriteSearch = this.getRecentSearch(true);
        this.panelResult.removeAll();
        this.listItems.clear();
        if (recentSearch != null && !recentSearch.isEmpty()) {
            this.panelResult.add(this.createLabel("Recent"));

            for(Item item : recentSearch) {
                this.panelResult.add(item);
                this.listItems.add(item);
            }
        }

        if (favoriteSearch != null && !favoriteSearch.isEmpty()) {
            this.panelResult.add(this.createLabel("Favorite"));

            for(Item item : favoriteSearch) {
                this.panelResult.add(item);
                this.listItems.add(item);
            }
        }

        if (this.listItems.isEmpty()) {
            this.panelResult.add(new NoRecentResult());
        } else {
            this.setSelected(0);
        }

        this.updateLayout();
    }

    private JLabel createLabel(String title) {
        JLabel label = new JLabel(title);
        label.putClientProperty("FlatLaf.style", "font:bold +1;border:5,15,5,15;");
        return label;
    }

    private List<Item> getRecentSearch(boolean favorite) {
        String[] recentSearch = DemoPreferences.getRecentSearch(favorite);
        if (recentSearch == null) {
            return null;
        } else {
            List<Item> list = new ArrayList();

            for(String s : recentSearch) {
                Class<? extends Form> classForm = this.getClassForm(s);
                if (MyMenuValidation.validation((Class)classForm)) {
                    Item item = this.createRecentItem(s, favorite);
                    if (item != null) {
                        list.add(item);
                    }
                }
            }

            return list;
        }
    }

    private Class<? extends Form> getClassForm(String name) {
        for(Map.Entry<SystemForm, Class<? extends Form>> entry : this.formsMap.entrySet()) {
            if (((SystemForm)entry.getKey()).name().equals(name)) {
                return (Class)entry.getValue();
            }
        }

        return null;
    }

    private Item createRecentItem(String name, boolean favorite) {
        for(Map.Entry<SystemForm, Class<? extends Form>> entry : this.formsMap.entrySet()) {
            if (((SystemForm)entry.getKey()).name().equals(name)) {
                return new Item((SystemForm)entry.getKey(), (Class)entry.getValue(), true, favorite);
            }
        }

        return null;
    }

    private Component createNoResult(String text) {
        JPanel panel = new JPanel(new MigLayout("insets 15 5 15 5,al center,gapx 1"));
        JLabel label = new JLabel("Không có kết quả cho \"");
        JLabel labelEnd = new JLabel("\"");
        label.putClientProperty("FlatLaf.style", "foreground:$Label.disabledForeground;");
        labelEnd.putClientProperty("FlatLaf.style", "foreground:$Label.disabledForeground;");
        JLabel labelText = new JLabel(text);
        panel.add(label);
        panel.add(labelText);
        panel.add(labelEnd);
        return panel;
    }

    public void clearSearch() {
        if (!this.textSearch.getText().isEmpty()) {
            this.textSearch.setText("");
        } else {
            this.showRecentResult();
        }

    }

    public void searchGrabFocus() {
        this.textSearch.grabFocus();
    }

    private static class NoRecentResult extends JPanel {
        public NoRecentResult() {
            this.init();
        }

        private void init() {
            this.setLayout(new MigLayout("insets 15 5 15 5,al center"));
            JLabel label = new JLabel("Không có tìm kiếm gần đây");
            label.putClientProperty("FlatLaf.style", "foreground:$Label.disabledForeground;font:bold;");
            this.add(label);
        }
    }

    private class Item extends JButton {
        private final SystemForm data;
        private final Class<? extends Form> form;
        private final boolean isRecent;
        private final boolean isFavorite;
        private Component itemSource;

        public Item(SystemForm data, Class<? extends Form> form, boolean isRecent, boolean isFavorite) {
            this.data = data;
            this.form = form;
            this.isRecent = isRecent;
            this.isFavorite = isFavorite;
            this.init();
        }

        private void init() {
            this.setFocusable(false);
            this.setHorizontalAlignment(10);
            this.setLayout(new MigLayout("insets 3 3 3 0,filly,gapy 2", "[]push[]"));
            this.putClientProperty("FlatLaf.style", "background:null;arc:10;borderWidth:0;focusWidth:0;innerFocusWidth:0;[light]selectedBackground:lighten($Button.selectedBackground,9%)");
            JLabel labelDescription = new JLabel(this.data.description());
            labelDescription.putClientProperty("FlatLaf.style", "foreground:$Label.disabledForeground;");
            this.add(new JLabel(this.data.name()), "cell 0 0");
            this.add(labelDescription, "cell 0 1");
            if (!this.isRecent) {
                this.add(new JLabel(new FlatMenuArrowIcon()), "cell 1 0,span 1 2");
            } else {
                this.add(this.createRecentOption(), "cell 1 0,span 1 2");
            }

            this.addActionListener((e) -> {
                if (this.itemSource == null) {
                    this.clearSelected();
                    this.setSelected(true);
                    this.showForm();
                } else if (this.itemSource.getName().equals("remove")) {
                    this.removeRecent();
                } else if (this.itemSource.getName().equals("favorite")) {
                    this.addFavorite();
                }

            });
        }

        private void clearSelected() {
            for(Component com : this.getParent().getComponents()) {
                if (com instanceof JButton) {
                    ((JButton)com).setSelected(false);
                }
            }

        }

        protected void showForm() {
            ModalDialog.closeModal("search");
            Drawer.setSelectedItemClass(this.form);
            if (!this.isFavorite) {
                DemoPreferences.addRecentSearch(this.data.name(), false);
            }

        }

        protected Component createRecentOption() {
            JPanel panel = new JPanel(new MigLayout("insets n 0 n 0,fill,gapx 2", "", "[fill]"));
            panel.setOpaque(false);
            JButton cmdRemove = this.createButton("remove", "clear.svg", 0.35F, "Label.foreground", 0.9F);
            if (!this.isFavorite) {
                JButton cmdFavorite = this.createButton("favorite", "favorite.svg", 0.4F, "Component.accentColor", 0.9F);
                panel.add(cmdFavorite);
            } else {
                JLabel label = new JLabel(new SVGIconUIColor("raven/modal/demo/icons/favorite_filled.svg", 0.4F, "Component.accentColor", 0.8F));
                label.putClientProperty("FlatLaf.style", "border:3,3,3,3;");
                panel.add(label);
            }

            panel.add(new JSeparator(1), "gapy 5 5");
            panel.add(cmdRemove);
            return panel;
        }

        private JButton createButton(String name, String icon, float scale, final String hoverKey, float alpha) {
            final SVGIconUIColor svgIcon = new SVGIconUIColor("raven/modal/demo/icons/" + icon, scale, "Label.disabledForeground", alpha);
            JButton button = new JButton(svgIcon);
            button.setName(name);
            button.setFocusable(false);
            button.setContentAreaFilled(false);
            button.setCursor(Cursor.getPredefinedCursor(12));
            button.setModel(this.getModel());
            button.putClientProperty("FlatLaf.style", "margin:3,3,3,3;");
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    svgIcon.setColorKey(hoverKey);
                    Item.this.itemSource = (Component)e.getSource();
                }

                public void mouseExited(MouseEvent e) {
                    svgIcon.setColorKey("Label.disabledForeground");
                    Item.this.itemSource = null;
                }
            });
            return button;
        }

        protected void removeRecent() {
            DemoPreferences.removeRecentSearch(this.data.name(), this.isFavorite);
            FormSearchPanel.this.panelResult.remove(this);
            FormSearchPanel.this.listItems.remove(this);
            if (FormSearchPanel.this.listItems.isEmpty()) {
                FormSearchPanel.this.panelResult.removeAll();
                FormSearchPanel.this.panelResult.add(new NoRecentResult());
            } else if (this.getCount(this.isFavorite) == 0) {
                if (this.isFavorite) {
                    FormSearchPanel.this.panelResult.remove(FormSearchPanel.this.panelResult.getComponentCount() - 1);
                } else {
                    FormSearchPanel.this.panelResult.remove(0);
                }
            }

            FormSearchPanel.this.updateLayout();
        }

        protected void addFavorite() {
            DemoPreferences.addRecentSearch(this.data.name(), true);
            int[] index = this.getFirstFavoriteIndex();
            FormSearchPanel.this.panelResult.remove(this);
            FormSearchPanel.this.listItems.remove(this);
            Item item = FormSearchPanel.this.new Item(this.data, this.form, this.isRecent, true);
            if (index == null) {
                FormSearchPanel.this.panelResult.add(FormSearchPanel.this.createLabel("Favorite"));
                FormSearchPanel.this.panelResult.add(item);
                FormSearchPanel.this.listItems.add(item);
            } else {
                FormSearchPanel.this.panelResult.remove(this);
                FormSearchPanel.this.listItems.remove(this);
                FormSearchPanel.this.panelResult.add(item, index[1] - 1);
                FormSearchPanel.this.listItems.add(index[0] - 1, item);
            }

            if (this.getCount(false) == 0) {
                FormSearchPanel.this.panelResult.remove(0);
            }

            FormSearchPanel.this.updateLayout();
        }

        private int getCount(boolean favorite) {
            int count = 0;

            for(Item item : FormSearchPanel.this.listItems) {
                if (item.isFavorite == favorite) {
                    ++count;
                }
            }

            return count;
        }

        private int[] getFirstFavoriteIndex() {
            for(int i = 0; i < FormSearchPanel.this.listItems.size(); ++i) {
                if (((Item)FormSearchPanel.this.listItems.get(i)).isFavorite) {
                    return new int[]{i, FormSearchPanel.this.panelResult.getComponentZOrder((Component)FormSearchPanel.this.listItems.get(i))};
                }
            }

            return null;
        }
    }
}