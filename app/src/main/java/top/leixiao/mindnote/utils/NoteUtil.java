package top.leixiao.mindnote.utils;

import android.content.Context;
import android.os.Environment;
import android.support.v7.appcompat.BuildConfig;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import top.leixiao.mindnote.Config;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.database.NotePaper;

//md5sum() 和copyfile() 有严重问题，没有实现，copyfile已经被删除了，因为没有被使用
//md5sum() 有关的都被删除了，因为没有使用
public class NoteUtil {
    public static final int EDIT_TYPE_CAMERA = -4;
    public static final int EDIT_TYPE_FLOAT = -6;
    public static final int EDIT_TYPE_LIST = -2;
    public static final int EDIT_TYPE_NORMAL = -1;
    public static final int EDIT_TYPE_RECORD = -3;
    public static final int EDIT_TYPE_UPDATE = -5;
    public static final int[] COLOR_BACKGROUND = new int[]{0xfffafafa, 0xfff9eaca, 0xffdff4bc, 0xffcdfded, 0xffc7ecfc, 0xfffddedc, 0xfffcf5c1};
    public static final int[] COLOR_TEXT_HIGH_LIGHT = new int[]{0xffffee32, 0xffffc02b, 0xffff9547, 0xffff6b47, 0xffc5e64d, 0xff1ed0c4, 0xff3fa7de, 0xff3f89de, 0xffffffff, 0xffebebeb, 0xffb3b3b3, 0xff929292};
    public static final int[] COLOR_TEXT_HIGH_LIGHT_RES = new int[]{R.drawable.brush_yellow, R.drawable.brush_light_orange, R.drawable.brush_dark_orange, R.drawable.brush_red, R.drawable.brush_green, R.drawable.brush_sky_blue, R.drawable.brush_light_blue, R.drawable.brush_dark_blue, R.drawable.brush_white, R.drawable.brush_light_gray, R.drawable.brush_med_gray, R.drawable.brush_dark_gray};
    public static final int NOTE_TYPE_LIST = 0;
    public static final int NOTE_TYPE_TEXT = 0;
    public static final String ENCODING = "UTF-8";
    public static final String FILES_ANDROID_DATA = (Environment.getExternalStorageDirectory() + "/Android/data/");
    public static final String FILES_DIR = EnvironmentUtils.buildExternalStorageAppFilesDirs(Config.PACKAGE_NAME)[NOTE_TYPE_TEXT].getPath();
    public static final String JSON_FILE_NAME = "name";
    public static final String JSON_IMAGE_HEIGHT = "height";
    public static final String JSON_IMAGE_WIDTH = "width";
    public static final String JSON_MTIME = "mtime";
    public static final String JSON_SPAN = "span";
    public static final String JSON_STATE = "state";
    public static final String JSON_TEXT = "text";
    public static final String NOTE_SPAN_BACKGROUNDCOLOR = "bc";
    public static final String NOTE_SPAN_END = "end";
    public static final String NOTE_SPAN_FOREGROUNDCOLOR = "fc";
    public static final String NOTE_SPAN_IMAGE = "img";
    public static final String NOTE_SPAN_PARAM = "param";
    public static final String NOTE_SPAN_RELATIVESIZE = "rs";
    public static final String NOTE_SPAN_START = "start";
    public static final String NOTE_SPAN_TYPE = "span";
    public static final String NOTE_SPAN_URL = "url";
    public static final String RECORD_DIV = "/";
    public static final String SAFE_BOX_FILE_PATH = (Environment.getExternalStorageDirectory().getPath() + "/.@meizu_protbox@");
    static final String[] COLOR_ABB;
    static final int COLOR_NUM = 12;
    static final long MILLSECONDS_OF_DAY = 86400000;
    static final long MILLSECONDS_OF_HOUR = 3600000;
    static final String TAG = "NoteUtil";
    static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', NotePaper.BLUE, 'c', 'd', 'e', 'f'};

    static {
        String[] strArr = new String[COLOR_NUM];
        strArr[NOTE_TYPE_TEXT] = "a";
        strArr[1] = "b";
        strArr[2] = "c";
        strArr[3] = "d";
        strArr[4] = "e";
        strArr[5] = "f";
        strArr[6] = "g";
        strArr[7] = "h";
        strArr[8] = "a";
        strArr[9] = "b";
        strArr[10] = "c";
        strArr[11] = "d";
        COLOR_ABB = strArr;
    }

    public static File getFile(String uuid, String name) {
        Log.d(TAG, "getFileName: files_dir:"+FILES_DIR);
        Log.d(TAG, "getFileName: path:"+new File(FILES_DIR, uuid + RECORD_DIV + name).getPath());
        return new File(FILES_DIR, uuid + RECORD_DIV + name);
    }

    public static String getFileName(String uuid, String name) {

        return new File(FILES_DIR, uuid + RECORD_DIV + name).getPath();
    }

    public static int getBackgroundColor(int index) {
        int c = COLOR_BACKGROUND[NOTE_TYPE_TEXT];
        if (index == EDIT_TYPE_NORMAL || index >= COLOR_BACKGROUND.length) {
            return c;
        }
        return COLOR_BACKGROUND[index];
    }

    public static String getOutputName(String suffix) {
        Date date = new Date(System.currentTimeMillis());
        return String.format("M%d%02d%02d_%02d%02d%02d_%d.%s", date.getYear() + LunarCalendar.MIN_YEAR, date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds(), date.getTime() % 1000, suffix);
    }

    public static String getTimeString() {
        Date date = new Date(System.currentTimeMillis());
        return String.format("%d%02d%02d_%02d%02d%02d_%d", date.getYear() + LunarCalendar.MIN_YEAR, date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds(), date.getTime() % 1000);
    }

    //有严重问题，注释了


    public static String encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[(l << 1)];
        int j = NOTE_TYPE_TEXT;
        for (int i = NOTE_TYPE_TEXT; i < l; i++) {
            int i2 = j + 1;
            out[j] = hexDigits[(data[i] & 240) >>> 4];
            j = i2 + 1;
            out[i2] = hexDigits[data[i] & 15];
        }
        return new String(out);
    }

    public static String toHexString(byte[] buffer) {
        return encodeHex(buffer);
    }

    //严重问题，注释


    public static String getNameWithoutExt(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dotPosition = fileName.lastIndexOf(".");
        if (dotPosition > 0) {
            return fileName.substring(NOTE_TYPE_TEXT, dotPosition);
        }
        return fileName;
    }

    public static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null && fileList.length > 0) {
                File[] arr$ = fileList;
                int len$ = arr$.length;
                for (int i$ = NOTE_TYPE_TEXT; i$ < len$; i$++) {
                    deleteFile(arr$[i$]);
                }
            }
            file.delete();
            return;
        }
        file.delete();
    }

    public static int getBackgroundColorDrawableRes(int color) {
        for (int index = NOTE_TYPE_TEXT; index < COLOR_TEXT_HIGH_LIGHT.length; index++) {
            if (color == COLOR_TEXT_HIGH_LIGHT[index]) {
                return COLOR_TEXT_HIGH_LIGHT_RES[index];
            }
        }
        return R.drawable.brush_white;
    }

    public static CharSequence getDate(Context context, long time) {
        return formatTimeStampString(context, time);
    }

    public static String formatTimeStampString(Context context, long when) {
        boolean isThisYear;
        boolean isToday;
        boolean isThisWeek = true;
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.set(System.currentTimeMillis());
        boolean is24 = DateFormat.is24HourFormat(context);
        int weekStart = now.yearDay - now.weekDay;
        if (now.year != then.year || then.yearDay > now.yearDay) {
            isThisYear = false;
        } else {
            isThisYear = true;
        }
        if (isThisYear && then.yearDay == now.yearDay) {
            isToday = true;
        } else {
            isToday = false;
        }
        if (!isThisYear || then.yearDay < weekStart || then.yearDay >= now.yearDay) {
            isThisWeek = false;
        }
        if (isToday) {
            if (is24) {
                return then.format(context.getString(R.string.pattern_hour_minute));
            }
            return then.format(context.getString(R.string.pattern_hour_minute_12));
        } else if (isThisWeek) {
            return then.format(context.getString(R.string.pattern_week));
        } else {
            if (isThisYear) {
                return then.format(context.getString(R.string.pattern_month_day));
            }
            return then.format(context.getString(R.string.pattern_year_month_day));
        }
    }

    public static CharSequence formatTimeInListForOverSeaUser(Context context, long time, boolean simple) {
        GregorianCalendar now = new GregorianCalendar();
        if (time < MILLSECONDS_OF_HOUR) {
            return BuildConfig.VERSION_NAME;
        }
        GregorianCalendar today = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        long in24h = time - today.getTimeInMillis();
        if (in24h < 0 || in24h >= MILLSECONDS_OF_DAY) {
            long in48h = (time - today.getTimeInMillis()) + MILLSECONDS_OF_DAY;
            if (in48h >= 0 && in48h < MILLSECONDS_OF_DAY) {
                return context.getString(R.string.fmt_pre_yesterday) + HanziToPinyin.Token.SEPARATOR + DateFormat.getTimeFormat(context).format(Long.valueOf(time));
            }
            GregorianCalendar target = new GregorianCalendar();
            target.setTimeInMillis(time);
            if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
                return DateUtils.formatDateTime(context, time, 24);
            }
            return DateFormat.getDateFormat(context).format(time);
        }
        return BuildConfig.VERSION_NAME + DateFormat.getTimeFormat(context).format(time);
    }

    public static String getFileListString(ArrayList<String> list) {
        if (list.size() <= 0) {
            return null;
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Object name : list) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(name);
        }
        return sb.toString();
    }

    public static boolean checkTimeSpace(Context context, long time, int days) {
        GregorianCalendar now = new GregorianCalendar();
        if (time < MILLSECONDS_OF_HOUR) {
            return true;
        }
        GregorianCalendar today = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        if (time <= today.getTimeInMillis() - (((long) days) * MILLSECONDS_OF_DAY) || time > today.getTimeInMillis() + MILLSECONDS_OF_DAY) {
            return true;
        }
        return false;
    }
}
