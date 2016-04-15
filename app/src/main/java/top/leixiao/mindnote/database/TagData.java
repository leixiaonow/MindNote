package top.leixiao.mindnote.database;

import android.database.Cursor;

public class TagData {
    public static final long GROUP_ALL_ID = -1;
    public static boolean FUN_ENCRYPT = true;
    public int mCount;
    public long mId = GROUP_ALL_ID;
    public String mName;
    public String mNewName;
    public int mOrder;
    public String mUUId;

    public TagData(TagData td) {
        super();//我加的
        this.mId = td.mId;
        this.mUUId = td.mUUId;
        this.mName = td.mName;
        this.mNewName = td.mNewName;
        this.mOrder = td.mOrder;
        this.mCount = td.mCount;
    }

    //我加的，因为getTag方法需要一个无参数的构造方法
    public TagData() {
        super();
    }

    //从Cursor得到TagData
    public static TagData getTag(Cursor c) {
        if (c == null) {
            return null;
        }
        TagData data = new TagData();
        data.mId = c.getLong(c.getColumnIndex("_id"));
        data.mUUId = c.getString(c.getColumnIndex("uuid"));
        String string = c.getString(c.getColumnIndex("name"));
        data.mName = string;
        data.mNewName = string;
        data.mOrder = c.getInt(c.getColumnIndex("sort"));
        return data;
    }
}
