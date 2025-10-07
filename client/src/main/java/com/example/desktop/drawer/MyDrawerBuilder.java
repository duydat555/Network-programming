package com.example.desktop.drawer;

import com.example.desktop.SwingApp;
import com.example.desktop.form.Dashboard;
import raven.drawer.DrawerOption;
import raven.drawer.component.SimpleDrawerBuilder;
import raven.drawer.component.footer.SimpleFooterData;
import raven.drawer.component.header.SimpleHeaderData;
import raven.drawer.component.menu.MenuAction;
import raven.drawer.component.menu.MenuEvent;
import raven.drawer.component.menu.MenuValidation;
import raven.drawer.component.menu.SimpleMenuOption;
import raven.drawer.component.menu.data.Item;
import raven.drawer.component.menu.data.MenuItem;
import raven.swing.AvatarIcon;

import java.util.ArrayList;
import java.util.List;

public class MyDrawerBuilder extends SimpleDrawerBuilder {

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        return new SimpleHeaderData()
                .setIcon(new AvatarIcon(getClass().getResource("/images/c8bhTxVxhN4DMrJeCnu5usutbY5.jpg"), 60, 60, 999))
                .setTitle("Lê Duy Đạt")
                .setDescription("ledat16072005@gmail.com");
    }

    @Override
    public SimpleMenuOption getSimpleMenuOption() {
        String icons[] = {
                "house.svg",
                "list.svg",
                "list-ordered.svg",
                "map-pin-house.svg",
                "log-out.svg"};

        List<MenuItem> menuItems = new ArrayList<>();
        int iconIndex = 0;

//        menuItems.add(new Item.Label("MAIN"));
        menuItems.add(new Item("Trang chủ", icons[iconIndex++]));

//        menuItems.add(new Item.Label("WEB APP"));
        menuItems.add(new Item("Chủ đề", icons[iconIndex++])
                .subMenu("Marvel")
                .subMenu("DC")
                .subMenu("4k")
                .subMenu("Lồng tiếng"));

       // menuItems.add(new Item.Label("COMPONENT"));
        menuItems.add(new Item("Loại phim", icons[iconIndex++])
                .subMenu("Phim lẻ")
                .subMenu("Phim bộ"));
        menuItems.add(new Item("Quốc gia", icons[iconIndex++])
                .subMenu("Mỹ")
                .subMenu("Hàn Quốc")
                .subMenu("Trung Quốc")
                .subMenu("Việt Nam"));

        menuItems.add(new Item("Đăng xuất", icons[iconIndex++]));

        return new SimpleMenuOption()
                .setMenus(menuItems.toArray(new MenuItem[0]))
                .setBaseIconPath("icons")
                .setIconScale(0.75f)
                .addMenuEvent(new MenuEvent() {
                    @Override
                    public void selected(MenuAction action, int[] index) {
                        if (index.length > 0 && index[0] == 0) {
                            // WindowsTabbed.getInstance().addTab("Test Form", new TestForm());
                            System.out.println("Dashboard selected");
                            SwingApp.showFormStatic(new Dashboard());
                        } else if (index.length > 0 && index[0] == 1) {
                            if (index.length > 1) {
                                System.out.println("Marvel");

                            }
                        }
                      //  System.out.println("Menu selected " + index[0] + (index.length > 1 ? " " + index[1] : ""));
                    }
                })
                .setMenuValidation(new MenuValidation() {
                    public boolean menuValidation(int index, int subIndex) {
                        return true;
                    }
                });
    }

    @Override
    public SimpleFooterData getSimpleFooterData() {
        return new SimpleFooterData()
                .setTitle("Java Swing Drawer")
                .setDescription("Version 1.1.0");
    }

    @Override
    public int getDrawerWidth() {
        return 275;
    }

}
