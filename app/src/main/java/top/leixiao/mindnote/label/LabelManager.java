package top.leixiao.mindnote.label;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;

import top.leixiao.mindnote.NoteAppImpl;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.common.ThreadPool.Job;
import top.leixiao.mindnote.common.ThreadPool.JobContext;


public class LabelManager {
    private static final int[] DEFAULT_LABEL = new int[]{R.string.default_label_inspiration, R.string.default_label_travel, R.string.default_label_meeting, R.string.default_label_life};
    private static final String TAG = "LabelManager";
    private final NoteAppImpl mApp;
    private int INDEX_CONTENT = 1;
    private int INDEX_ID = 0;
    private final ArrayList<LabelHolder> mLabelCache = new ArrayList<>();
    private Uri mLabelUri = null;
    private ArrayList<LabelDataChangeListener> mListeners = new ArrayList<>();
    private String mOrderBy = "_id DESC";
    private String[] mProjection = new String[]{"_id", "content"};
    private ContentResolver mResolver;

    public interface LabelDataChangeListener {
        void onDataChange();
    }

    public static class LabelHolder {
        public String mContent;
        public int mId;

        public LabelHolder(int id, String content) {
            this.mId = id;
            this.mContent = content;
        }
    }

    class C05011 implements Job<Object> {
        C05011() {
        }

        public Object run(JobContext jc) {
            if (!LabelManager.getIsInitLabel(LabelManager.this.mApp)) {
                LabelManager.this.initDefaultLabel();
                LabelManager.setIsInitLabel(LabelManager.this.mApp, true);
            }
            LabelManager.this.initCache();
            LabelManager.this.notifyListener();
            return null;
        }
    }

    public LabelManager(NoteAppImpl app) {
        this.mApp = app;
        this.mResolver = app.getContentResolver();
    }

    public void init() {
        this.mApp.getThreadPool().submit(new C05011());
    }

    public void addLabelDataChangeListener(LabelDataChangeListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.add(listener);
        }
    }

    public void removeLabelDataChangeListener(LabelDataChangeListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
        }
    }

    public void notifyListener() {
        synchronized (this.mListeners) {
            Iterator i$ = this.mListeners.iterator();
            while (i$.hasNext()) {
                ((LabelDataChangeListener) i$.next()).onDataChange();
            }
        }
    }

    public ArrayList<LabelHolder> getLabelList() {
        ArrayList<LabelHolder> arrayList;
        synchronized (this.mLabelCache) {
            arrayList = new ArrayList(this.mLabelCache);
        }
        return arrayList;
    }

    private void initDefaultLabel() {
        ArrayList<ContentProviderOperation> insertOps = new ArrayList();
        for (int resId : DEFAULT_LABEL) {
            insertOps.add(ContentProviderOperation.newInsert(this.mLabelUri).withValue("content", this.mApp.getString(resId)).build());
        }
        try {
//            this.mResolver.applyBatch(LabelContract.AUTHORITY, insertOps);
        } catch (Exception e) {

        }
    }

    private void initCache() {
        Cursor cursor = this.mResolver.query(this.mLabelUri, this.mProjection, null, null, this.mOrderBy);
        if (cursor == null) {
            return;
        }
        ArrayList<LabelHolder> labels = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                labels.add(new LabelHolder(cursor.getInt(this.INDEX_ID), cursor.getString(this.INDEX_CONTENT)));
            } finally {
                closeSilently(cursor);
            }
        }
        synchronized (this.mLabelCache) {
            this.mLabelCache.addAll(labels);
        }
    }

    public static void closeSilently(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable t) {
            }
        }
    }

    public int addLabel(String content) {
        ContentValues values = new ContentValues();
        values.put("content", content);
        int id = (int) ContentUris.parseId(this.mApp.getContentResolver().insert(this.mLabelUri, values));
        synchronized (this.mLabelCache) {
            this.mLabelCache.add(0, new LabelHolder(id, content));
        }
        notifyListener();
        return id;
    }

    public int getLabelId(String labelContent) {
        int labelId = -1;
        if (TextUtils.isEmpty(labelContent)) {
            return -1;
        }
        int len = getLabelList().size();
        for (int i = 0; i < len; i++) {
            LabelHolder labelHolder = (LabelHolder) getLabelList().get(i);
            if (labelContent.equals(labelHolder.mContent)) {
                labelId = labelHolder.mId;
                break;
            }
        }
        return labelId;
    }

    public void removeLabelById(int id) {
        String[] selectionArgs = new String[]{String.valueOf(id)};
        this.mApp.getContentResolver().delete(this.mLabelUri, "_id=?", selectionArgs);
        removeFromCache(id);
    }

    private void removeFromCache(int id) {
        synchronized (this.mLabelCache) {
            ArrayList<LabelHolder> labels = this.mLabelCache;
            LabelHolder find = null;
            Iterator i$ = labels.iterator();
            while (i$.hasNext()) {
                LabelHolder holder = (LabelHolder) i$.next();
                if (holder.mId == id) {
                    find = holder;
                    break;
                }
            }
            if (find != null) {
                labels.remove(find);
            }
        }
        notifyListener();
    }

    public static boolean getIsInitLabel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("label_init", false);
    }

    public static void setIsInitLabel(Context context, boolean init) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("label_init", init).commit();

    }
}
