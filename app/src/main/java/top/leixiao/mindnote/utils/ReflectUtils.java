package top.leixiao.mindnote.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.MediaRecorder;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {
    public static int getOrientFromInputStream(Context context, Uri uri) {
        int i = 1;
        try {
            Method b = Class.forName("android.media.ThumbnailUtils").getDeclaredMethod("getOrientFromInputStream", new Class[]{Context.class, Uri.class});
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            i = (Integer) b.invoke(null, context, uri);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return i;
    }

    public static boolean hasCallbacks(Handler handler, Runnable r) {
        boolean z = false;
        try {
            Method b = Class.forName("android.os.Handler").getDeclaredMethod("hasCallbacks", new Class[]{Runnable.class});
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            z = (Boolean) b.invoke(handler, r);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return z;
    }

    public static int getDuration(MediaRecorder recorder) {
        int i = 0;
        try {
            Method b = Class.forName("android.media.MediaRecorder").getDeclaredMethod("getDuration", new Class[0]);
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            i = (Integer) b.invoke(recorder);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return i;
    }

    public static int pause(MediaRecorder recorder) {
        try {
            Method b = Class.forName("android.media.MediaRecorder").getDeclaredMethod("pause", new Class[0]);
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            b.invoke(recorder);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int resume(MediaRecorder recorder) {
        try {
            Method b = Class.forName("android.media.MediaRecorder").getDeclaredMethod("resume", new Class[0]);
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            b.invoke(recorder);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void setKeepSelection(TextView tv, boolean enabled) {
        try {
            Method b = Class.forName("android.widget.TextView").getDeclaredMethod("setKeepSelection", Boolean.TYPE);
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            b.invoke(tv, enabled);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTransformedTouchPointInView(ViewGroup parent, float x, float y, View child, PointF outLocalPoint) {
        boolean z = false;
        try {
            Method b = Class.forName("android.view.ViewGroup").getDeclaredMethod("isTransformedTouchPointInView", new Class[]{Float.TYPE, Float.TYPE, View.class, PointF.class});
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            z = (Boolean) b.invoke(parent, x, y, child, outLocalPoint);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return z;
    }

    public static void getLastTouchPoint(View view, Point outLocalPoint) {
        try {
            Method b = Class.forName("android.view.View").getDeclaredMethod("getViewRootImpl", new Class[0]);
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            Object o = b.invoke(view, new Object[0]);
            b = Class.forName("android.view.ViewRootImpl").getDeclaredMethod("getLastTouchPoint", new Class[]{Point.class});
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            b.invoke(o, new Object[]{outLocalPoint});
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void setScrollX(View view, int x) {
        try {
            Field field = Class.forName("android.widget.View").getDeclaredField("mScrollX");
            field.setAccessible(true);
            field.set(view, x);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static boolean copyFile(File srcFile, File destFile) {
        boolean z = false;
        try {
            Method b = Class.forName("android.os.FileUtils").getDeclaredMethod("copyFile", new Class[]{File.class, File.class});
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            z = (Boolean) b.invoke(null, srcFile, destFile);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return z;
    }

    public static int getStaticIntValue(String className, String fieldName, int defaultValue) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return defaultValue;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return defaultValue;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return defaultValue;
        } catch (NoSuchFieldException e4) {
            e4.printStackTrace();
            return defaultValue;
        }
    }

    public static String getStaticStringValue(String className, String fieldName, String defaultValue) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static void setWidth(AlertDialog dlg, int width) {
        try {
            Method b = Class.forName("android.app.AlertDialog").getDeclaredMethod("setWidth", Integer.TYPE);
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            b.invoke(dlg, width);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static boolean isMzNfcP2pConnected(NfcAdapter na) {
        boolean z = false;
        try {
            Method b = Class.forName("android.nfc.NfcAdapter").getDeclaredMethod("isMzNfcP2pConnected", new Class[0]);
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            z = ((Boolean) b.invoke(na, new Object[0])).booleanValue();
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return z;
    }

    public static void setMeiZuFlag(LayoutParams lp, int value) {
        try {
            Field field = Class.forName("android.view.WindowManager$LayoutParams").getDeclaredField("meizuFlags");
            field.setAccessible(true);
            field.set(lp, field.getInt(lp) | value);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void setShowGravity(Object menu, int gravity) {
        try {
            Method b = Class.forName("com.android.internal.view.menu.MenuItemImpl").getDeclaredMethod("setShowGravity", new Class[]{Integer.TYPE});
            if (!b.isAccessible()) {
                b.setAccessible(true);
            }
            b.invoke(menu, gravity);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void setIncludeBottomLineSpacing(TextView tv, boolean enable) {
        try {
            Class.forName("android.widget.TextView").getMethod("setIncludeBottomLineSpacing", new Class[]{Boolean.TYPE}).invoke(tv, new Object[]{Boolean.valueOf(enable)});
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
        }
    }

    public static void setStatusBarDarkIcon(Activity activity, boolean dark) {
        try {
            LayoutParams lp = activity.getWindow().getAttributes();
            Field meizuFlags = lp.getClass().getDeclaredField("meizuFlags");
            meizuFlags.setAccessible(true);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= IntentUtils.MEIZU_FLAG_CHOOSE_OPEN_METHOD_VALUE;
            } else {
                value &= -513;
            }
            meizuFlags.setInt(lp, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
