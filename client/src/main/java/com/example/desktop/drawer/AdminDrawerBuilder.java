package com.example.desktop.drawer;

import com.example.desktop.api.AuthApiClient;
import com.example.desktop.system.Form;
import com.example.desktop.system.UserSession;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import raven.extras.AvatarIcon;
import com.example.desktop.system.AllForms;
import com.example.desktop.system.FormManager;

// Import các Form admin (bạn sẽ tạo chúng ở bước 4)
import com.example.desktop.form.AdminDashboard;
import com.example.desktop.form.UserManagement;
import com.example.desktop.form.MovieManagement;
import com.example.desktop.form.CategoryManagement;
import com.example.desktop.form.CountryManagement;

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

public class AdminDrawerBuilder extends SimpleDrawerBuilder {
    private static AdminDrawerBuilder instance;

    public static AdminDrawerBuilder getInstance() {
        if (instance == null) {
            instance = new AdminDrawerBuilder();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private final int SHADOW_SIZE = 12;

    private AdminDrawerBuilder() {
        // Dùng MenuOption của Admin
        super(createSimpleMenuOption());
        LightDarkButtonFooter lightDarkButtonFooter = (LightDarkButtonFooter) getFooter();
        lightDarkButtonFooter.addModeChangeListener(isDarkMode -> {
            // event for light dark mode changed
        });
    }

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        AuthApiClient.UserInfo u = UserSession.getUser();
        String username = "Admin";
        String email = "Quản lý hệ thống";

        if (u != null) {
            username = u.username(); // Lấy tên admin đã đăng nhập
            email = u.email();
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
                .setTitle("Admin Control Panel")
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

    // Đây là phần quan trọng nhất: tạo menu cho Admin
    public static MenuOption createSimpleMenuOption() {

        MenuOption simpleMenuOption = new MenuOption();

        // **ĐỊNH NGHĨA CÁC MỤC MENU CỦA ADMIN**
        MenuItem items[] = new MenuItem[]{
                // Bạn cần tạo các class Form này ở bước 4
                new Item("Thống kê", "layout-dashboard.svg", AdminDashboard.class),
                new Item("Quản lý Người dùng", "users.svg", UserManagement.class),
                new Item("Quản lý Phim", "clapperboard.svg", MovieManagement.class),
                new Item("Quản lý Thể loại", "list.svg", CategoryManagement.class),
                new Item("Quản lý Quốc gia", "map-pinned.svg", CountryManagement.class),
                new Item("Đăng xuất", "log-out.svg") // Nút này để quay lại trang Login
        };

        simpleMenuOption.setMenuStyle(new MenuStyle() {
            @Override
            public void styleMenuItem(JButton menu, int[] index, boolean isMainItem) {
                boolean isTopLevel = index.length == 1;
                if (isTopLevel) {
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
                System.out.println("Admin Drawer menu selected " + Arrays.toString(index));
                Class<?> itemClass = action.getItem().getItemClass();
                int i = index[0];

                // **XỬ LÝ NÚT ĐĂNG XUẤT (mục cuối cùng, index 5)**
                if (i == 5) {
                    // Gọi hàm showLogin() để quay lại màn hình đăng nhập
                    FormManager.showLogin();
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
                .setBaseIconPath("icons") // Đảm bảo bạn có các icon này
                .setIconScale(0.9f);

        return simpleMenuOption;
    }

    // Các hàm còn lại giữ nguyên như MyDrawerBuilder
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