package top.leixiao.mindnote.utils;

public class RecordUtil {
    public static String timeConvert(long seconds) {
        int s = ((int) seconds) % 60;
        int m = (int) ((seconds / 60) % 60);
        if (((int) seconds) / 3600 > 0) {
            return String.format("%02d:%02d:%02d", ((int) seconds) / 3600, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }
}
