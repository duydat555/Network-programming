package com.example.desktop.drawer;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.form.Dashboard;
import com.example.desktop.form.FavoritesForm;
import com.example.desktop.form.ActionGenreForm;
import com.example.desktop.form.AnimationGenreForm;
import com.example.desktop.form.HorrorGenreForm;
import com.example.desktop.form.HistoryGenreForm;
import com.example.desktop.form.RomanceGenreForm;
import com.example.desktop.form.SciFiGenreForm;
import com.example.desktop.system.Form;
import com.example.desktop.system.UserSession;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import raven.extras.AvatarIcon;
import com.example.desktop.system.AllForms;
import com.example.desktop.system.FormManager;
import raven.modal.drawer.DrawerPanel;
import raven.modal.drawer.item.Item;
import raven.modal.drawer.item.MenuItem;
import raven.modal.drawer.menu.MenuAction;
import raven.modal.drawer.menu.MenuEvent;
import raven.modal.drawer.menu.MenuOption;
import raven.modal.drawer.menu.MenuStyle;
import raven.modal.drawer.renderer.DrawerStraightDotLineStyle;
import raven.modal.drawer.simple.SimpleDrawerBuilder;
import raven.modal.drawer.simple.footer.LightDarkButtonFooter;
import raven.modal.drawer.simple.footer.SimpleFooterData;
import raven.modal.drawer.simple.header.SimpleHeaderData;
import raven.modal.option.Option;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class MyDrawerBuilder extends SimpleDrawerBuilder {
    private static MyDrawerBuilder instance;

    public static MyDrawerBuilder getInstance() {
        if (instance == null) {
            instance = new MyDrawerBuilder();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private final int SHADOW_SIZE = 12;

    private MyDrawerBuilder() {
        super(createSimpleMenuOption());
        LightDarkButtonFooter lightDarkButtonFooter = (LightDarkButtonFooter) getFooter();
        lightDarkButtonFooter.addModeChangeListener(isDarkMode -> {
            // event for light dark mode changed
        });
    }

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        System.out.println("=== getSimpleHeaderData() called ===");
        AuthApiClient.UserInfo u = UserSession.getUser();
        System.out.println("UserSession.getUser(): " + u);

        String username = "Guest";
        String email = "Vui lòng đăng nhập";

        if (u != null) {
            username = u.username();
            email = u.email();
            System.out.println("Setting header to: " + username + " / " + email);
        } else {
            System.out.println("User is null, using Guest");
        }

        AvatarIcon icon = new AvatarIcon(new FlatSVGIcon("raven/modal/demo/drawer/image/avatar_male.svg", 100, 100), 50, 50, 3.5f);
        icon.setType(AvatarIcon.Type.MASK_SQUIRCLE);
        icon.setBorder(2, 2);

        changeAvatarIconBorderColor(icon);

        UIManager.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("lookAndFeel")) {
                changeAvatarIconBorderColor(icon);
            }
        });

        return new SimpleHeaderData()
                .setIcon(icon)
                .setTitle(username)
                .setDescription(email);
    }

    private void changeAvatarIconBorderColor(AvatarIcon icon) {
        icon.setBorderColor(new AvatarIcon.BorderColor(UIManager.getColor("Component.accentColor"), 0.7f));
    }

    @Override
    public SimpleFooterData getSimpleFooterData() {
        return new SimpleFooterData()
                .setTitle("Java Swing Drawer")
                .setDescription("Version 1.1.0");
    }

    @Override
    public Option createOption() {
        Option option = super.createOption();
        option.setOpacity(0.3f);
        option.getBorderOption()
                .setShadowSize(new Insets(0, 0, 0, SHADOW_SIZE));
        return option;
    }

    public static MenuOption createSimpleMenuOption() {

        MenuOption simpleMenuOption = new MenuOption();

        MenuItem items[] = new MenuItem[]{
                new Item("Trang chủ", "house.svg", Dashboard.class),
                new Item("Thể loại", "list.svg")
                        .subMenu("Hành động", ActionGenreForm.class)
                        .subMenu("Hoạt hình", AnimationGenreForm.class)
                        .subMenu("Kinh dị", HorrorGenreForm.class)
                        .subMenu("Lịch sử", HistoryGenreForm.class)
                        .subMenu("Tình cảm", RomanceGenreForm.class)
                        .subMenu("Viễn tưởng", SciFiGenreForm.class),
//                new Item("Quốc gia", "map-pinned.svg")
//                        .subMenu("Việt Nam")
//                        .subMenu("Hàn Quốc"),
                new Item("Danh sách yêu thích", "heart.svg", FavoritesForm.class),
//                new Item("Xem tiếp", "history.svg"),
                new Item("Đăng xuất", "log-out.svg")
        };

        simpleMenuOption.setMenuStyle(new MenuStyle() {

            @Override
            public void styleMenuItem(JButton menu, int[] index, boolean isMainItem) {
                boolean isTopLevel = index.length == 1;
                if (isTopLevel) {
                    // adjust item menu at the top level because it's contain icon
                    menu.putClientProperty(FlatClientProperties.STYLE, "" +
                            "margin:-1,0,-1,0;");
                }
            }

            @Override
            public void styleMenu(JComponent component) {
                component.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
            }
        });

        simpleMenuOption.getMenuStyle().setDrawerLineStyleRenderer(new DrawerStraightDotLineStyle());

        simpleMenuOption.addMenuEvent(new MenuEvent() {
            @Override
            public void selected(MenuAction action, int[] index) {
                System.out.println("Drawer menu selected " + Arrays.toString(index));
                Class<?> itemClass = action.getItem().getItemClass();
                int i = index[0];
                if (i == 5) {
                    FormManager.logout();
                    action.consume();
                    return;
                }
                if (itemClass == null || !Form.class.isAssignableFrom(itemClass)) {
                    action.consume();
                    return;
                }
                Class<? extends Form> formClass = (Class<? extends Form>) itemClass;
                FormManager.showForm(AllForms.getForm(formClass));
            }
        });

        simpleMenuOption.setMenus(items)
                .setBaseIconPath("icons")
                .setIconScale(0.9f);

        return simpleMenuOption;
    }


    @Override
    public int getDrawerWidth() {
        return 270 + SHADOW_SIZE;
    }

    @Override
    public int getDrawerCompactWidth() {
        return 80 + SHADOW_SIZE;
    }

    @Override
    public int getOpenDrawerAt() {
        return 1000;
    }

    @Override
    public boolean openDrawerAtScale() {
        return false;
    }

    @Override
    public void build(DrawerPanel drawerPanel) {
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
    }

    private static String getDrawerBackgroundStyle() {
        return "" +
                "[light]background:tint($Panel.background,20%);" +
                "[dark]background:tint($Panel.background,5%);";
    }

}
