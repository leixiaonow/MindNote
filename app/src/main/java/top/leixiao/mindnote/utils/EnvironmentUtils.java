package top.leixiao.mindnote.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class EnvironmentUtils {
    public static File[] buildExternalStorageAppCacheDirs(String packageName) {
        File[] fileArr = null;
        try {
            return (File[]) Class.forName("android.os.Environment").getMethod("buildExternalStorageAppCacheDirs", new Class[]{String.class}).invoke(null, packageName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            return null;
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }

    public static File[] buildExternalStorageAppFilesDirs(String packageName) {
        File[] fileArr = null;
        try {
            return (File[]) Class.forName("android.os.Environment").getMethod("buildExternalStorageAppFilesDirs", new Class[]{String.class}).invoke(null, packageName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return fileArr;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return fileArr;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            return fileArr;
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
            return fileArr;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return fileArr;
        }
    }
}
