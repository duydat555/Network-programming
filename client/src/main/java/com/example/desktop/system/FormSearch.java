package com.example.desktop.system;

import raven.modal.ModalDialog;
import raven.modal.demo.component.EmptyModalBorder;
import com.example.desktop.component.FormSearchPanel;
import com.example.desktop.drawer.MyDrawerBuilder;
import raven.modal.demo.utils.SystemForm;
import raven.modal.drawer.item.Item;
import raven.modal.drawer.item.MenuItem;
import raven.modal.option.Location;
import raven.modal.option.Option;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormSearch {

    private static FormSearch instance;
    public static final String ID = "search";
    private final Map<SystemForm, Class<? extends Form>> formsMap;
    private FormSearchPanel searchPanel;

    public static FormSearch getInstance() {
        if (instance == null) {
            instance = new FormSearch();
        }
        return instance;
    }

    // THAY ĐỔI: Constructor
    private FormSearch() {
        formsMap = new HashMap<>();
        // Tải menu client làm mặc định khi khởi động
        reloadMenus(MyDrawerBuilder.getInstance().getSimpleMenuOption().getMenus());
    }

    // HÀM MỚI: Thêm hàm này
    public void reloadMenus(MenuItem[] menuItems) {
        formsMap.clear(); // Xóa menu cũ

        List<Class<?>> formClass = new ArrayList<>();
        getMenuClass(menuItems, formClass); // Gọi hàm non-static

        @SuppressWarnings("unchecked")
        Class<? extends Form>[] formClasses = formClass.toArray(new Class[0]);

        for (Class<? extends Form> cls : formClasses) {
            if (cls.isAnnotationPresent(SystemForm.class)) {
                SystemForm f = cls.getAnnotation(SystemForm.class);
                formsMap.put(f, cls);
            }
        }

        // Reset search panel để nó nạp map mới
        searchPanel = null;
    }

    // XÓA: private Class<? extends Form>[] getClassForms() { ... }

    // THAY ĐỔI: Chuyển hàm này thành non-static (xóa "static")
    private void getMenuClass(MenuItem[] menuItems, List<Class<?>> formClass) {
        for (MenuItem menu : menuItems) {
            if (menu.isMenu()) {
                Item item = (Item) menu;
                if (item.getItemClass() != null) {
                    formClass.add(item.getItemClass());
                }
                if (item.isSubmenuAble()) {
                    getMenuClass(item.getSubMenu().toArray(new Item[0]), formClass);
                }
            }
        }
    }

    public void installKeyMap(JComponent component) {
        ActionListener key = _ -> showSearch();
        component.registerKeyboardAction(key, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void showSearch() {
        if (ModalDialog.isIdExist(ID)) {
            return;
        }
        Option option = ModalDialog.createOption();
        option.setAnimationEnabled(false);
        option.getLayoutOption().setMargin(20, 10, 10, 10).setLocation(Location.CENTER, Location.TOP);
        ModalDialog.showModal(FormManager.getFrame(), new EmptyModalBorder(getSearchPanel(), (_, action) -> {
            if (action == EmptyModalBorder.OPENED) {
                searchPanel.searchGrabFocus();
            }
        }), option, ID);
    }

    private JPanel getSearchPanel() {
        if (searchPanel == null) {
            Map<SystemForm, Class<? extends Form>> ravenFormsMap = new HashMap<>();
            for (Map.Entry<SystemForm, Class<? extends Form>> entry : formsMap.entrySet()) {
                @SuppressWarnings("unchecked")
                Class<? extends Form> clazz = entry.getValue();
                ravenFormsMap.put(entry.getKey(), clazz);
            }
            searchPanel = new FormSearchPanel(ravenFormsMap);
        }
        searchPanel.formCheck();
        searchPanel.clearSearch();
        ComponentOrientation orientation = FormManager.getFrame().getComponentOrientation();
        if (orientation.isLeftToRight() != searchPanel.getComponentOrientation().isLeftToRight()) {
            searchPanel.applyComponentOrientation(orientation);
        }
        return searchPanel;
    }
}
