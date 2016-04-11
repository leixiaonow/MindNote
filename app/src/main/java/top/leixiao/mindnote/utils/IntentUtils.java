package top.leixiao.mindnote.utils;

import android.content.Intent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class IntentUtils {
    public static final int MEIZU_FLAG_CHOOSE_OPEN_METHOD = 256;
    public static final int MEIZU_FLAG_CHOOSE_OPEN_METHOD_VALUE = 512;

    public static String getMzActionFolderSend() {
        String ret = "meizu.intent.action.FOLDER_SEND";
        try {
            Field sendField = Intent.class.getDeclaredField("MZ_ACTION_FOLDER_SEND");
            sendField.setAccessible(true);
            return (String) sendField.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return ret;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return ret;
        }
    }

    public static String getMzActionHandoverSend() {
        String ret = "meizu.intent.action.HANDOVER_SEND";
        try {
            Field sendField = Intent.class.getDeclaredField("MZ_ACTION_HANDOVER_SEND");
            sendField.setAccessible(true);
            return (String) sendField.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return ret;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return ret;
        }
    }

    public static String getExtraRequestResultFromChooserActivity() {
        String ret = "android.intent.extra.requset_result";
        try {
            Field resultField = Intent.class.getDeclaredField("EXTRA_REQUEST_RESULT_FROM_CHOOSERACTIVITY");
            resultField.setAccessible(true);
            return (String) resultField.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return ret;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return ret;
        }
    }

    public static String getMzActionFolderSendMultiple() {
        String ret = "meizu.intent.action.FOLDER_SEND_MULTIPLE";
        try {
            Field sendField = Intent.class.getDeclaredField("MZ_ACTION_FOLDER_SEND_MULTIPLE");
            sendField.setAccessible(true);
            return (String) sendField.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return ret;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return ret;
        }
    }

    public static Intent addMeizuFlags(Intent intent, int flags) {
        Intent ret = intent;
        try {
            return (Intent) Intent.class.getMethod("addMeizuFlags", new Class[]{Integer.TYPE}).invoke(intent, new Object[]{Integer.valueOf(flags)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return ret;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return ret;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return ret;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return ret;
        }
    }

    public static int getMzFlagChooseOpenMethod() {
        int ret = MEIZU_FLAG_CHOOSE_OPEN_METHOD;
        try {
            Field sendField = Intent.class.getDeclaredField("MEIZU_FLAG_CHOOSE_OPEN_METHOD");
            sendField.setAccessible(true);
            ret = sendField.getInt(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
        return ret;
    }

    public static int getMzFlagChooseOpenMethodValue() {
        int ret = MEIZU_FLAG_CHOOSE_OPEN_METHOD_VALUE;
        try {
            Field sendField = Intent.class.getDeclaredField("MEIZU_FLAG_CHOOSE_OPEN_METHOD_VALUE");
            sendField.setAccessible(true);
            ret = sendField.getInt(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
        return ret;
    }
}
