package top.leixiao.mindnote.database;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.v7.appcompat.BuildConfig;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import top.leixiao.mindnote.R;
import top.leixiao.mindnote.database.NotePaper.Notes;
import top.leixiao.mindnote.utils.Constants;
import top.leixiao.mindnote.utils.NoteUtil;

//copydb()没有实现，不插入默认笔记或不用升级，可以不使用
public class NotePaperProvider extends ContentProvider {
    private static final String TAG = "NotePaperProvider";

    public static final String DATABASE_NAME = "note_paper.db";
    public static final String NOTES_TABLE_NAME = "notes";
    public static final String LABEL_TABLE_NAME = "labels";
    public static final String NOTE_LABEL_TABLE_NAME = "notelabels";
    public static final String CATEGORY_TABLE_NAME = "categorys";
    public static final String FILES_TABLE_NAME = "notefiles";


    private static final int FILE_COLUMN_HASH = 3;
    private static final int FILE_COLUMN_ID = 0;
    private static final int FILE_COLUMN_LAST_HASH = 4;
    private static final int FILE_COLUMN_NOTE_ID = 1;
    private static final int FILE_COLUMN_TYPE = 2;


    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;
    private static final int NOTE_LABEL = 4;
    private static final int FILES = 11;
    private static final int FILE_ID = 12;
    private static final int CATEGORY = 13;
    private static final int CATEGORY_ID = 14;
    private static final int LABEL = 15;
    private static final int LABEL_ID = 16;
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private static HashMap<String, String> sNotesProjectionMap = new HashMap<>();
    private static HashMap<String, String> sCategoryProjectionMap = new HashMap<>();
    private static HashMap<String, String> sLabelProjectionMap = new HashMap<>();
    private static HashMap<String, String> sNoteLabelProjectionMap = new HashMap<>();

    static {

        sUriMatcher.addURI(NotePaper.AUTHORITY, NOTES_TABLE_NAME, NOTES);
        sUriMatcher.addURI(NotePaper.AUTHORITY, "notes/#", NOTE_ID);
        sUriMatcher.addURI(NotePaper.AUTHORITY, FILES_TABLE_NAME, FILES);
        sUriMatcher.addURI(NotePaper.AUTHORITY, "notefiles/#", FILE_ID);
        sUriMatcher.addURI(NotePaper.AUTHORITY, CATEGORY_TABLE_NAME, CATEGORY);
        sUriMatcher.addURI(NotePaper.AUTHORITY, "categorys/#", CATEGORY_ID);
        sUriMatcher.addURI(NotePaper.AUTHORITY, LABEL_TABLE_NAME, LABEL);
        sUriMatcher.addURI(NotePaper.AUTHORITY, "labels/#", LABEL_ID);
        sUriMatcher.addURI(NotePaper.AUTHORITY, NOTE_LABEL_TABLE_NAME, NOTE_LABEL);

        sNotesProjectionMap.put(NotePaper.ID, NotePaper.ID);
        sNotesProjectionMap.put(Notes.UUID, Notes.UUID);
        sNotesProjectionMap.put(Notes.TITLE, Notes.TITLE);
        sNotesProjectionMap.put(Notes.NOTE, Notes.NOTE);
        sNotesProjectionMap.put(Notes.FIRST_IMAGE, Notes.FIRST_IMAGE);
        sNotesProjectionMap.put(Notes.FIRST_RECORD, Notes.FIRST_RECORD);
        sNotesProjectionMap.put(Notes.TOP, Notes.TOP);
        sNotesProjectionMap.put(Notes.CATEGORY, Notes.CATEGORY);
        sNotesProjectionMap.put(Notes.COLOR, Notes.COLOR);
        sNotesProjectionMap.put(Notes.CREATE_TIME, Notes.CREATE_TIME);
        sNotesProjectionMap.put(Notes.MODIFIED_DATE, Notes.MODIFIED_DATE);
        sNotesProjectionMap.put(Notes.PAPER, Notes.PAPER);
        sNotesProjectionMap.put(Notes.FONT_COLOR, Notes.FONT_COLOR);
        sNotesProjectionMap.put(Notes.FONT_SIZE, Notes.FONT_SIZE);
        sNotesProjectionMap.put(Notes.LABELS,Notes.LABELS);


        sCategoryProjectionMap.put(NotePaper.ID, NotePaper.ID);
        sCategoryProjectionMap.put("name", "name");

        sLabelProjectionMap.put(NotePaper.ID, NotePaper.ID);
        sLabelProjectionMap.put("content", "content");



    }

    private DatabaseHelper mOpenHelper;

    private static void createTriggers(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS files_cleanup");
    }

    private static String getNoteFilePath(String uuid) {
        return NoteUtil.FILES_DIR + NoteUtil.RECORD_DIV + uuid;
    }

    static void deleteSdcardFiles(String uuid) {
        Log.d(TAG, "begin remove " + uuid);
        String fileName = getNoteFilePath(uuid);
        File file = new File(fileName);
        if (file.exists()) {
            NoteUtil.deleteFile(file);
        } else {
            Log.d(TAG, "file not found " + fileName);
        }
        Log.d(TAG, "end remove " + fileName);
    }

    public boolean onCreate() {
        this.mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String orderBy;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(NOTES_TABLE_NAME);
        int match = sUriMatcher.match(uri);
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Notes.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        switch (match) {
            case NOTES /*1*/:
                qb.setProjectionMap(sNotesProjectionMap);
                break;
            case NOTE_ID /*2*/:
                qb.setProjectionMap(sNotesProjectionMap);
                qb.appendWhere("_id=" + ContentUris.parseId(uri));
                break;
            case FILES /*11*/:
                qb.setTables(FILES_TABLE_NAME);
//                qb.setProjectionMap(sFilesProjectionMap);
                orderBy = null;
                break;
            case FILE_ID /*12*/:
                qb.setTables(FILES_TABLE_NAME);
//                qb.setProjectionMap(sFilesProjectionMap);
                qb.appendWhere("_id=" + ContentUris.parseId(uri));
                orderBy = null;
                break;
            case CATEGORY /*13*/:
                qb.setTables(CATEGORY_TABLE_NAME);
                qb.setProjectionMap(sCategoryProjectionMap);
                orderBy = NotePaper.ID;
                break;
            case CATEGORY_ID /*14*/:
                qb.setTables(CATEGORY_TABLE_NAME);
                qb.setProjectionMap(sCategoryProjectionMap);
                qb.appendWhere("_id=" + ContentUris.parseId(uri));
                orderBy = NotePaper.ID;
                break;
            case LABEL:
                qb.setTables(LABEL_TABLE_NAME);
                qb.setProjectionMap(sLabelProjectionMap);
                orderBy=NotePaper.ID;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Cursor c = qb.query(this.mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case NOTES /*1*/:
                return Notes.CONTENT_TYPE;
            case NOTE_ID /*2*/:
                return Notes.CONTENT_ITEM_TYPE;
            case FILES /*11*/:
//                return NoteFiles.CONTENT_TYPE;
//            case FILE_ID /*12*/:
//                return NoteFiles.CONTENT_ITEM_TYPE;
//            case CATEGORY /*13*/:
//                return NoteCategory.CONTENT_TYPE;
//            case CATEGORY_ID /*14*/:
//                return NoteCategory.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        ContentValues values;
        long rowId;
        Uri noteUri;
        switch (sUriMatcher.match(uri)) {
            case NOTES /*1*/:
                if (initialValues != null) {
                    values = new ContentValues(initialValues);
                } else {
                    values = new ContentValues();
                }
                Long now = System.currentTimeMillis();
                if (!values.containsKey(Notes.MODIFIED_DATE)) {
                    values.put(Notes.MODIFIED_DATE, now);
                }
                if (!values.containsKey(Notes.NOTE)) {
                    values.put(Notes.NOTE, BuildConfig.VERSION_NAME);
                }
                if (!values.containsKey(Notes.PAPER)) {
                    values.put(Notes.PAPER, FILE_COLUMN_ID);
                }
                if (!values.containsKey(Notes.UUID)) {
                    Log.d(TAG, "insert no uuid ");
                    values.put(Notes.UUID, UUID.randomUUID().toString());
                }
                if (!values.containsKey(Notes.TOP)) {
                    values.put(Notes.TOP, FILE_COLUMN_ID);
                }

                rowId = db.insert(NOTES_TABLE_NAME, Notes.NOTE, values);
                if (rowId >= 0) {
                    noteUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    return noteUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case FILES /*11*/:
                rowId = db.insert(FILES_TABLE_NAME, null, initialValues);
                if (rowId >= 0) {
                    if (initialValues.getAsInteger(Constants.JSON_KEY_TYPE) == 0) {
                    }
                    return ContentUris.withAppendedId(uri, rowId);
                }
                throw new SQLException("Failed to insert file into " + uri);
            case CATEGORY /*13*/:
                rowId = db.insert(CATEGORY_TABLE_NAME, null, initialValues);
                if (rowId >= 0) {
                    Uri tag_uri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(tag_uri, null);
                    return tag_uri;
                }
                throw new SQLException("Failed to insert file into " + uri);
            case LABEL:
                Log.d(TAG, "insert label ");
                rowId=db.insert(LABEL_TABLE_NAME,null,initialValues);
                if (rowId >= 0) {
                    Uri labelUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(labelUri, null);
                    return labelUri;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return null;
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        int type = sUriMatcher.match(uri);
        String str;
        String[] strArr;
        Cursor c;
        String uuid;
        switch (type) {
            case NOTES /*1*/:
                str = NOTES_TABLE_NAME;
                strArr = new String[NOTES];
                strArr[FILE_COLUMN_ID] = Notes.UUID;
                c = db.query(str, strArr, where, whereArgs, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        uuid = c.getString(FILE_COLUMN_ID);
                        count = db.delete(FILES_TABLE_NAME, "note_uuid=\"" + uuid + "\"", null);
                        deleteSdcardFiles(uuid);
                    }
                    c.close();
                    count = db.delete(NOTES_TABLE_NAME, where, whereArgs);
                    break;
                }
                count = FILE_COLUMN_ID;
                break;
            case NOTE_ID /*2*/:
                long noteId = ContentUris.parseId(uri);
                str = NOTES_TABLE_NAME;
                strArr = new String[NOTES];
                strArr[FILE_COLUMN_ID] = Notes.UUID;
                c = db.query(str, strArr, "_id=" + noteId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : BuildConfig.VERSION_NAME), whereArgs, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        uuid = c.getString(FILE_COLUMN_ID);
//                        db.delete(FILES_TABLE_NAME, "note_uuid=\"" + uuid + "\"", null);
                        deleteSdcardFiles(uuid);
                    }
                    c.close();
                    count = db.delete(NOTES_TABLE_NAME, "_id=" + noteId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : BuildConfig.VERSION_NAME), whereArgs);
                    break;
                }
                count = FILE_COLUMN_ID;
                break;
            case LABEL_ID:
                count = db.delete(LABEL_TABLE_NAME, "_id=" + ContentUris.parseId(uri), whereArgs);
            case CATEGORY /*13*/:
                count = db.delete(CATEGORY_TABLE_NAME, where, whereArgs);
                break;
            case CATEGORY_ID /*14*/:
                count = db.delete(CATEGORY_TABLE_NAME, "_id=" + ContentUris.parseId(uri) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : BuildConfig.VERSION_NAME), whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        if (count > 0 && (type == NOTES || type == NOTE_ID || type == FILES || type == FILE_ID)) {
        }
        return count;
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        int type = sUriMatcher.match(uri);
        long accountID;
        switch (type) {
            case NOTES /*1*/:
                count = db.update(NOTES_TABLE_NAME, values, where, whereArgs);
                break;
            case NOTE_ID /*2*/:
                count = db.update(NOTES_TABLE_NAME, values, "_id=" + ContentUris.parseId(uri) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : BuildConfig.VERSION_NAME), whereArgs);
                break;
            case FILES /*11*/:
                count = db.update(FILES_TABLE_NAME, values, where, whereArgs);
                break;
            case FILE_ID /*12*/:
                count = db.update(FILES_TABLE_NAME, values, "_id=" + ContentUris.parseId(uri) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : BuildConfig.VERSION_NAME), whereArgs);
                break;
            case CATEGORY /*13*/:
                count = db.update(CATEGORY_TABLE_NAME, values, where, whereArgs);
                break;
            case CATEGORY_ID /*14*/:
                count = db.update(CATEGORY_TABLE_NAME, values, "_id=" + ContentUris.parseId(uri) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : BuildConfig.VERSION_NAME), whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if (count <= 0) {
            return count;
        }
        if (type != NOTES && type != NOTE_ID && type != FILES && type != FILE_ID) {
            return count;
        }
        return count;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        ContentProviderResult[] results = super.applyBatch(operations);
        db.setTransactionSuccessful();
        db.endTransaction();
        getContext().getContentResolver().notifyChange(Notes.CONTENT_URI, null);
        File parent = new File(NoteUtil.FILES_DIR);
        if (!parent.exists()) {
            return results;
        }
        getContext().sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(parent)));
        return results;
    }

    ContentValues generatInbuiltSyncNoteData(SQLiteDatabase db) {
        String uuid = "inbuilt_note_0";
        int count = FILE_COLUMN_ID;
        String str = NOTES_TABLE_NAME;
        String[] strArr = new String[1];
        strArr[0] = NotePaper.ID;
        String[] strArr2 = new String[1];
        strArr2[0] = uuid;
        Cursor c = db.query(str, strArr, "uuid=?", strArr2, null, null, null);
        if (c != null) {
            count = c.getCount();
            c.close();
        }
        if (count != 0) {
            return null;
        }
        String note = "\u5982\u679c\u4f60\u540c\u65f6\u662fFlyme2.0\u53ca\u4e4b\u524d\u56fa\u4ef6\u7528\u6237\uff0c\u4ee5\u4e0b\u4fe1\u606f\u5bf9\u4f60\u975e\u5e38\u91cd\u8981\u3002\n\n\u5982\u4f60\u6240\u89c1\uff0cFlyme3.0\u4fbf\u7b7e\uff0c\u662f\u4e00\u6b21\u91cd\u5927\u5347\u7ea7\uff0c\u6d82\u9e26\u72ec\u7acb\u4e3a\u753b\u677f\uff0c\u5e76\u91cd\u65b0\u8bbe\u8ba1\u4e86\u51e0\u4e4e\u6240\u6709\u90e8\u5206\uff0c\u56e0\u6b64\u6570\u636e\u540c\u6b65\u662f\u4e00\u4e2a\u91cd\u8981\u6311\u6218\u3002\n\n\u9996\u6b21\u4f7f\u7528Flyme3.0\u540c\u6b65\u65f6\uff0c\u6211\u4eec\u4f1a\u5c06\u4e4b\u524d\u7684\u4fbf\u7b7e\uff0c\u5168\u90e8\u5347\u7ea7\u4e3aFlyme3.0\u683c\u5f0f\uff0c\u540c\u65f6\u6d82\u9e26\u4f1a\u8f6c\u6362\u4e3a\u6807\u51c6\u56fe\u7247\u3002\n\n\u5347\u7ea7\u5b8c\u6210\u540e\uff0cFlyme3.0\u4fbf\u7b7e\u5c06\u4e0d\u518d\u4e0e\u4e4b\u524d\u56fa\u4ef6\u540c\u6b65\u3002\u76f8\u540c\u56fa\u4ef6\u4e4b\u95f4\u7684\u540c\u6b65\uff0c\u53ca\u624b\u673a\u4e0e\u4e91\u670d\u52a1\u7684\u540c\u6b65\u4e0d\u53d7\u5f71\u54cd\u3002";
        try {
            JSONArray ja = new JSONArray();
            JSONObject jo = new JSONObject();
            jo.put(NoteUtil.JSON_STATE, FILE_COLUMN_ID);
            jo.put(NoteUtil.JSON_TEXT, note);
            ja.put(jo);
            note = ja.toString();
            Long now = System.currentTimeMillis();
            ContentValues cv = new ContentValues();
            cv.put(Notes.UUID, uuid);
            cv.put(Notes.NOTE, note);
            cv.put(Notes.CREATE_TIME, now);
            cv.put(Notes.MODIFIED_DATE, now);
            cv.put(Notes.PAPER, FILE_COLUMN_ID);
            cv.put(Notes.TITLE, "\u4fbf\u7b7e\u540c\u6b65\u8bf4\u660e");
            return cv;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //暂时不用，注释了


    public static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;
        public DatabaseHelper(Context context) {
            super(context, NotePaperProvider.DATABASE_NAME, null, 1);
            this.mContext = context;
        }

        private void createCategoryTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE categorys (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT )");
            db.execSQL("insert into categorys (name) " + "values (" + "'"+ this.mContext.getResources().getString(R.string.add_category) + "'" + ")");
            db.execSQL("insert into categorys (name) " + "values (" + "'"+ this.mContext.getResources().getString(R.string.default_catogery) + "'" + ")");
            db.execSQL("insert into categorys (name) " + "values (" + "'"+ this.mContext.getResources().getString(R.string.default_catogery_travl) + "'" + ")");
            db.execSQL("insert into categorys (name) " + "values (" + "'"+ this.mContext.getResources().getString(R.string.default_catogery_life) + "'" + ")");
            db.execSQL("insert into categorys (name) " + "values (" + "'"+ this.mContext.getResources().getString(R.string.default_catogery_work) + "'" + ")");
        }

        private void createLabelTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE labels (_id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT)");
            db.execSQL("insert into labels (content) " + "values (" + "'" + this.mContext.getResources().getString(R.string.default_tag_work) + "'"+")");
            db.execSQL("insert into labels (content) " + "values (" + "'" + this.mContext.getResources().getString(R.string.default_tag_life) + "'"+")");
            db.execSQL("insert into labels (content) " + "values (" + "'" + this.mContext.getResources().getString(R.string.default_tag_temp) + "'"+")");
        }

        public void onCreate(SQLiteDatabase db) {
            Log.d(NotePaperProvider.TAG, "onCreate:notes");
            db.execSQL("CREATE TABLE notes (_id INTEGER PRIMARY KEY AUTOINCREMENT,uuid TEXT,note TEXT,create_time INTEGER,modified INTEGER,paper INTEGER,title TEXT," + Notes.FONT_COLOR + " INTEGER," + Notes.COLOR + " INTEGER," + Notes.FONT_SIZE + " INTEGER," + Notes.FIRST_IMAGE + " TEXT," + Notes.FIRST_RECORD + " TEXT," + Notes.TOP + " INTEGER DEFAULT 0," + Notes.LABELS + " TEXT,"+ Notes.CATEGORY + " INTEGER DEFAULT 0" + ")");
            createCategoryTable(db);
            createLabelTable(db);
            //插入内建的笔记，可以注释掉
//            insertBuiltInNoteData(db, "0");
        }



        void insertBuiltInNoteData(SQLiteDatabase db, String dirty) {
//            String country = Locale.getDefault().getCountry();
//            Log.d(NotePaperProvider.TAG, "get current country: " + country);
//            if ("CN".equalsIgnoreCase(country)) {
//                String note = null;
//                String uuid = "inbuilt_note_1";
//                String name1 = "ai_1.jpg";
//                String name2 = "ai_2.jpg";
//                String name3 = "ai_3.jpg";
//                String name4 = "ai_4.jpg";
//                String name5 = "ai_5.jpg";
//                if (copydb(R.raw.ai_1, uuid, name1) && copydb(R.raw.ai_2, uuid, name2) && copydb(R.raw.ai_3, uuid, name3) && copydb(R.raw.ai_4, uuid, name4) && copydb(R.raw.ai_5, uuid, name5)) {
//                    String first_img = BuildConfig.VERSION_NAME;
//                    ArrayList<String> list = new ArrayList<>();
//                    list.add(name1);
//                    list.add(name2);
//                    list.add(name3);
//                    list.add(name4);
//                    list.add(name5);
//                    String fileList = NoteUtil.getFileListString(list);
//                    try {
//                        JSONArray ja = new JSONArray();
//                        JSONObject jo = new JSONObject();
//                        jo.put(NoteUtil.JSON_STATE, NotePaperProvider.LIVE_FOLDER_NOTES);
//                        jo.put(NoteUtil.JSON_FILE_NAME, name1);
//                        jo.put(NoteUtil.JSON_IMAGE_HEIGHT, 500);
//                        jo.put(NoteUtil.JSON_IMAGE_WIDTH, 920);
//                        ja.put(jo);
//                        first_img = jo.toString();
//                        JSONObject jo1 = new JSONObject();
//                        jo1.put(NoteUtil.JSON_STATE, NotePaperProvider.FILE_COLUMN_ID);
//                        jo1.put(NoteUtil.JSON_TEXT, "\u652f\u6301\u6587\u672c\u3001\u6e05\u5355\u3001\u5f55\u97f3\u4e0e\u56fe\u7247\u63d2\u5165\u529f\u80fd\uff0c\u4e00\u4efd\u8bb0\u5f55\uff0c\u591a\u79cd\u65b9\u6cd5\u3002");
//                        ja.put(jo1);
//                        JSONObject jo2 = new JSONObject();
//                        jo2.put(NoteUtil.JSON_STATE, NotePaperProvider.LIVE_FOLDER_NOTES);
//                        jo2.put(NoteUtil.JSON_FILE_NAME, name2);
//                        jo2.put(NoteUtil.JSON_IMAGE_HEIGHT, 500);
//                        jo2.put(NoteUtil.JSON_IMAGE_WIDTH, 920);
//                        ja.put(jo2);
//                        JSONObject jo21 = new JSONObject();
//                        jo21.put(NoteUtil.JSON_STATE, NotePaperProvider.FILE_COLUMN_ID);
//                        jo21.put(NoteUtil.JSON_TEXT, "\u6587\u5b57\u5927\u5c0f\u3001\u80cc\u666f\u989c\u8272\u968f\u5fc3\u8c03\u6574\uff0c\u7b80\u5355\u8bb0\u5f55\u4e5f\u53ef\u5c42\u6b21\u5206\u660e\uff0c\u91cd\u70b9\u7a81\u51fa\u3002");
//                        ja.put(jo21);
//                        JSONObject jo3 = new JSONObject();
//                        jo3.put(NoteUtil.JSON_STATE, NotePaperProvider.LIVE_FOLDER_NOTES);
//                        jo3.put(NoteUtil.JSON_FILE_NAME, name3);
//                        jo3.put(NoteUtil.JSON_IMAGE_HEIGHT, 500);
//                        jo3.put(NoteUtil.JSON_IMAGE_WIDTH, 920);
//                        ja.put(jo3);
//                        JSONObject jo31 = new JSONObject();
//                        jo31.put(NoteUtil.JSON_STATE, NotePaperProvider.FILE_COLUMN_ID);
//                        jo31.put(NoteUtil.JSON_TEXT, "\u6307\u5b9a\u4fbf\u7b7e\u7f6e\u9876\uff0c\u91cd\u8981\u7684\u4fbf\u7b7e\uff0c\u5c31\u662f\u5728\u91cd\u8981\u7684\u4f4d\u7f6e\u3002");
//                        ja.put(jo31);
//                        JSONObject jo4 = new JSONObject();
//                        jo4.put(NoteUtil.JSON_STATE, NotePaperProvider.LIVE_FOLDER_NOTES);
//                        jo4.put(NoteUtil.JSON_FILE_NAME, name4);
//                        jo4.put(NoteUtil.JSON_IMAGE_HEIGHT, 500);
//                        jo4.put(NoteUtil.JSON_IMAGE_WIDTH, 920);
//                        ja.put(jo4);
//                        JSONObject jo41 = new JSONObject();
//                        jo41.put(NoteUtil.JSON_STATE, NotePaperProvider.FILE_COLUMN_ID);
//                        jo41.put(NoteUtil.JSON_TEXT, "\u4fbf\u7b7e\u5206\u7ec4\uff0c\u4ece\u6b64\u4fbf\u7b7e\u601d\u8def\u4e0e\u4f60\u5927\u8111\u540c\u6b65\u3002");
//                        ja.put(jo41);
//                        JSONObject jo5 = new JSONObject();
//                        jo5.put(NoteUtil.JSON_STATE, NotePaperProvider.LIVE_FOLDER_NOTES);
//                        jo5.put(NoteUtil.JSON_FILE_NAME, name5);
//                        jo5.put(NoteUtil.JSON_IMAGE_HEIGHT, 500);
//                        jo5.put(NoteUtil.JSON_IMAGE_WIDTH, 920);
//                        ja.put(jo5);
//                        JSONObject jo51 = new JSONObject();
//                        jo51.put(NoteUtil.JSON_STATE, NotePaperProvider.FILE_COLUMN_ID);
//                        jo51.put(NoteUtil.JSON_TEXT, "\u4fbf\u7b7e\u52a0\u5bc6\uff0c\u9690\u79c1\u4fe1\u606f\u4e0d\u6015\u5fd8\u6389\uff0c\u4e5f\u4e0d\u62c5\u5fc3\u88ab\u522b\u4eba\u770b\u5230\u3002");
//                        ja.put(jo51);
//                        note = ja.toString();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    Long now = System.currentTimeMillis();
//                    db.execSQL("insert into notes (uuid,note,title,create_time,modified,paper," + Notes.FIRST_IMAGE + ","  + ","  + ") " + "values (" + "'" + uuid + "'" + "," + "'" + note + "'" + "," + "'" + "\u5168\u65b0\u8bbe\u8ba1\u7684Flyme\u4fbf\u7b7e" + "'" + "," + now + "," + now + "," + "0" + "," + "'" + first_img + "'"  + ")");
//                    File imgFile1 = NoteUtil.getFile(uuid, name1);
//                    File imgFile2 = NoteUtil.getFile(uuid, name2);
//                    File imgFile3 = NoteUtil.getFile(uuid, name3);
//                    File imgFile4 = NoteUtil.getFile(uuid, name4);
//                    File imgFile5 = NoteUtil.getFile(uuid, name5);
                    //    Log.d(NotePaperProvider.TAG, "name5: " + name5 + " md5: " + NoteUtil.md5sum(imgFile5.getPath()));
//                    db.execSQL("insert into notefiles (note_uuid,name,md5,type,mtime," + NoteFiles.DIRTY + ") " + "values (" + "'" + uuid + "'" + "," + "'" + name1 + "'" + "," + "'" + "0f981deb84ea215335173d28223df37b" + "'" + "," + NotePaperProvider.FILE_COLUMN_ID + "," + imgFile1.lastModified() + "," + dirty + ")");
//                    db.execSQL("insert into notefiles (note_uuid,name,md5,type,mtime," + NoteFiles.DIRTY + ") " + "values (" + "'" + uuid + "'" + "," + "'" + name2 + "'" + "," + "'" + "60331a4dcf37e878f63dd7365576e525" + "'" + "," + NotePaperProvider.FILE_COLUMN_ID + "," + imgFile2.lastModified() + "," + dirty + ")");
//                    db.execSQL("insert into notefiles (note_uuid,name,md5,type,mtime," + NoteFiles.DIRTY + ") " + "values (" + "'" + uuid + "'" + "," + "'" + name3 + "'" + "," + "'" + "cac68e8542e397ac0eace66bb3ab85c3" + "'" + "," + NotePaperProvider.FILE_COLUMN_ID + "," + imgFile3.lastModified() + "," + dirty + ")");
//                    db.execSQL("insert into notefiles (note_uuid,name,md5,type,mtime," + NoteFiles.DIRTY + ") " + "values (" + "'" + uuid + "'" + "," + "'" + name4 + "'" + "," + "'" + "c03146e871ffb3d9d9276ee5ed34a1e8" + "'" + "," + NotePaperProvider.FILE_COLUMN_ID + "," + imgFile4.lastModified() + "," + dirty + ")");
//                    db.execSQL("insert into notefiles (note_uuid,name,md5,type,mtime," + NoteFiles.DIRTY + ") " + "values (" + "'" + uuid + "'" + "," + "'" + name5 + "'" + "," + "'" + "055e8882040aa2ee0278a1cf95d01557" + "'" + "," + NotePaperProvider.FILE_COLUMN_ID + "," + imgFile5.lastModified() + "," + dirty + ")");
//                    this.mContext.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(new File(NoteUtil.FILES_DIR, uuid))));
//                }
//            }
        }

        //有严重问题，注释了
        private boolean copydb(int rawFile, String uuid, String name) {
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            is = this.mContext.getResources().openRawResource(rawFile);
            File pDataDir = new File(NoteUtil.FILES_ANDROID_DATA);

            if (!pDataDir.exists()) {
                Log.d(NotePaperProvider.TAG, "Android data dir not exist.");
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileOutputStream == null) {
                    return false;
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;

            }

            File sdcardFile = NoteUtil.getFile(uuid, name);
            if (sdcardFile.getParentFile().exists() || sdcardFile.getParentFile().mkdirs()) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(sdcardFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[0x00000400];
                while (true) {
                    int count = 0;
                    try {
                        count = is.read(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (count <= 0) {
                        break;
                    }
                    try {
                        fos.write(buffer, NotePaperProvider.FILE_COLUMN_ID, count);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            }
            return false;
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            NotePaperProvider.createTriggers(db);
        }
        @Override
        //不用升级，可以把内容注释掉，不使用insertBuiltInNoteData（）方法，应为其引用的方法没有实现
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }
}
