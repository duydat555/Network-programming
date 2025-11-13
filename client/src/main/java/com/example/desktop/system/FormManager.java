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

        // Install drawer here after UserSession is set
        if (!drawerInstalled) {
            // Reset drawer builder instance to force recreation with new user data
            MyDrawerBuilder.resetInstance();
            System.out.println("Installing drawer...");
            Drawer.installDrawer(frame, MyDrawerBuilder.getInstance());
            drawerInstalled = true;
        }

        Drawer.setVisible(true);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(FormManager.getMainForm());
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
}

