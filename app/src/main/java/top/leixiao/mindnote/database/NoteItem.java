package top.leixiao.mindnote.database;

public class NoteItem {
    public static final int STATE_CHECK_OFF = 1;
    public static final int STATE_CHECK_ON = 2;
    public static final int STATE_COMMON = 0;
    public static final int STATE_IMAGE = 3;
    public static final int STATE_RECORD = 4;
    public int mState = STATE_COMMON;
}
