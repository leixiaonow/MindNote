package top.leixiao.mindnote.utils;

import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.InvocationTargetException;

public class InputMethodManagerUtils {
    public static InputMethodManager getInstance() {
        try {
            return (InputMethodManager) Class.forName("android.view.inputmethod.InputMethodManager").getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
        return null;
    }

    public static InputMethodManager peekInstance() {
        try {
            return (InputMethodManager) Class.forName("android.view.inputmethod.InputMethodManager").getDeclaredMethod("peekInstance", new Class[0]).invoke(null, new Object[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
        return null;
    }

    public static boolean isSoftInputShown(InputMethodManager imm) {
        boolean ret = false;
        try {
            ret = ((Boolean) Class.forName("android.view.inputmethod.InputMethodManager").getDeclaredMethod("isSoftInputShown", new Class[0]).invoke(imm, new Object[0])).booleanValue();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
        return ret;
    }

    public static void addInputShownChangeListener(InputMethodManager imm, Object l) {
        try {
            Class<?> immImplClass = Class.forName("android.view.inputmethod.InputMethodManager");
            Class<?> cls = Class.forName("android.view.inputmethod.InputMethodManager$InputShownChangeListener");
            immImplClass.getDeclaredMethod("addInputShownChangeListener", new Class[]{cls}).invoke(imm, new Object[]{l});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
    }

    public static void removeInputShownChangeListener(InputMethodManager imm, Object l) {
        try {
            Class<?> immImplClass = Class.forName("android.view.inputmethod.InputMethodManager");
            Class<?> cls = Class.forName("android.view.inputmethod.InputMethodManager$InputShownChangeListener");
            immImplClass.getDeclaredMethod("removeInputShownChangeListener", new Class[]{cls}).invoke(imm, new Object[]{l});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
    }
}
