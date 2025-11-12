package com.example.desktop.system;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class AllForms {
    private static AllForms instance;
    private final Map<Class<? extends Form>, Form> formsMap = new HashMap<Class<? extends Form>, Form>();

    private static AllForms getInstance() {
        if (instance == null) {
            instance = new AllForms();
        }
        return instance;
    }

    private AllForms() {
    }

    public static Form getForm(Class<? extends Form> cls) {
        if (getInstance().formsMap.containsKey(cls)) {
            return AllForms.getInstance().formsMap.get(cls);
        }
        try {
            Form form = cls.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            AllForms.getInstance().formsMap.put(cls, form);
            AllForms.formInit(form);
            return form;
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void formInit(Form form) {
        SwingUtilities.invokeLater(() -> form.formInit());
    }
}