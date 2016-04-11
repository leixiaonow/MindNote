package top.leixiao.mindnote.utils;

import android.widget.ScrollView;

import java.lang.reflect.InvocationTargetException;

public class ScrollViewUtils {
    public static void setDelayTopOverScrollEnabled(ScrollView sv, boolean enable) {
        try {
            Class.forName("android.widget.ScrollView").getMethod("setDelayTopOverScrollEnabled", new Class[]{Boolean.TYPE}).invoke(sv, new Object[]{Boolean.valueOf(enable)});
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

    public static void setTopShadowEnable(ScrollView sv, boolean enable) {
        try {
            Class.forName("android.widget.ScrollView").getMethod("setTopShadowEnable", new Class[]{Boolean.TYPE}).invoke(sv, new Object[]{Boolean.valueOf(enable)});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
