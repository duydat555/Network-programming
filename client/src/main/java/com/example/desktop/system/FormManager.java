package com.example.desktop.system;

import javax.swing.JFrame;

import com.example.desktop.drawer.MyDrawerBuilder;
import com.example.desktop.form.Dashboard;
import raven.modal.Drawer;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import com.example.desktop.form.Login;
import raven.modal.demo.component.About;
import raven.modal.demo.utils.UndoRedo;
import com.example.desktop.drawer.AdminDrawerBuilder;
import com.example.desktop.form.AdminDashboard;

public class FormManager {
    protected static final UndoRedo<Form> FORMS = new UndoRedo<>();
    private static JFrame frame;
    private static MainForm mainForm;
    private static Login login;
    private static boolean drawerInstalled = false;

    public static void install(JFrame f) {
        frame = f;

//        frame.getContentPane().removeAll();
//        frame.getContentPane().add(FormManager.getMainForm());
//        frame.repaint();
//        frame.revalidate();

        install();
        logout();

//        showForm(AllForms.getForm(Dashboard.class));
    }


    private static void install() {
        FormSearch.getInstance().installKeyMap(FormManager.getMainForm());
    }

    public static void showForm(Form form) {
        if (form != FORMS.getCurrent()) {
            FORMS.add(form);
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            mainForm.refresh();
        }
    }

    public static void undo() {
        if (FORMS.isUndoAble()) {
            Form form = FORMS.undo();
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            if (drawerInstalled) {
                Drawer.setSelectedItemClass(form.getClass());
            }
        }
    }

    public static void redo() {
        if (FORMS.isRedoAble()) {
            Form form = FORMS.redo();
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            if (drawerInstalled) {
                Drawer.setSelectedItemClass(form.getClass());
            }
        }
    }

    public static void refresh() {
        if (FORMS.getCurrent() != null) {
            FORMS.getCurrent().formRefresh();
            mainForm.refresh();
        }
    }

    public static void login() {
        System.out.println("=== FormManager.login() called ===");
        System.out.println("UserSession.getUser() before drawer install: " + UserSession.getUser());

        // BỎ IF CHECK: Luôn luôn cài đặt Client Drawer

        // DÙNG MyDrawerBuilder (của Client)
        MyDrawerBuilder.resetInstance();
        MyDrawerBuilder clientDrawer = MyDrawerBuilder.getInstance();
        Drawer.installDrawer(frame, clientDrawer);

        // Tải menu client vào FormSearch
        FormSearch.getInstance().reloadMenus(clientDrawer.getSimpleMenuOption().getMenus());

        drawerInstalled = true; // Đặt lại là true sau khi cài đặt

        Drawer.setVisible(true);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(FormManager.getMainForm());

        // HIỂN THỊ Dashboard (của Client)
        Drawer.setSelectedItemClass(Dashboard.class);
        frame.repaint();
        frame.revalidate();
    }

    public static void logout() {
        // Only set drawer invisible if it was installed
        if (drawerInstalled) {
            Drawer.setVisible(false);
        }
        frame.getContentPane().removeAll();
        Login login = FormManager.getLogin();
        login.formCheck();
        frame.getContentPane().add(login);
        FORMS.clear();
        frame.repaint();
        frame.revalidate();
    }

    public static JFrame getFrame() {
        return frame;
    }

    private static MainForm getMainForm() {
        if (mainForm == null) {
            mainForm = new MainForm();
        }
        return mainForm;
    }

    private static Login getLogin() {
        if (login == null) {
            login = new Login();
        }
        return login;
    }

    public static void showAbout() {
        ModalDialog.showModal(frame, new SimpleModalBorder(new About(), "About"), ModalDialog.createOption().setAnimationEnabled(false));
    }

    public static void showAdminUI() {
        System.out.println("=== FormManager.showAdminUI() called ===");

        // 1. Cài đặt Admin Drawer
        AdminDrawerBuilder.resetInstance();
        AdminDrawerBuilder adminDrawer = AdminDrawerBuilder.getInstance();
        Drawer.installDrawer(frame, adminDrawer);

        // 2. Tải menu admin vào FormSearch
        FormSearch.getInstance().reloadMenus(adminDrawer.getSimpleMenuOption().getMenus());

        drawerInstalled = true; // Đánh dấu là đã cài drawer
        Drawer.setVisible(true); // Hiển thị drawer

        // 3. Sử dụng lại MainForm (giống hệt hàm login())
        frame.getContentPane().removeAll();
        frame.getContentPane().add(FormManager.getMainForm()); // Thêm MainForm

        // 4. Hiển thị form admin mặc định (AdminDashboard)
        // (Bạn cần tạo class này ở bước 4)
        Form defaultAdminForm = AllForms.getForm(AdminDashboard.class);
        showForm(defaultAdminForm); // Dùng showForm để thêm vào MainForm

        // 5. Set mục được chọn trong admin drawer
        Drawer.setSelectedItemClass(AdminDashboard.class);

        // 6. Vẽ lại giao diện
        frame.repaint();
        frame.revalidate();
    }

    public static void showLogin() {
        // Hàm này được gọi bởi nút "Đăng xuất" từ Panel Admin
        // Nó hoạt động tương tự như hàm logout()

        // 1. Tắt Drawer nếu nó đang bật
        if (drawerInstalled) {
            Drawer.setVisible(false);
        }

        // 2. Lấy frame chính
        JFrame frame = getFrame();
        frame.getContentPane().removeAll();

        // 3. Thêm lại form Login
        // Dùng hàm getLogin() có sẵn của bạn để tái sử dụng
        Login loginForm = getLogin();
        loginForm.formCheck(); // Gọi lại formCheck như trong hàm logout()
        frame.getContentPane().add(loginForm);

        // 4. Xóa lịch sử điều hướng (giống hàm logout())
        FORMS.clear();

        // 5. Vẽ lại giao diện
        frame.revalidate();
        frame.repaint();
    }
}
