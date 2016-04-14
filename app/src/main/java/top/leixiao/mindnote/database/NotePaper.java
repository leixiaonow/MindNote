package top.leixiao.mindnote.database;

import android.net.Uri;
import android.provider.BaseColumns;

import top.leixiao.mindnote.Config;


public final class NotePaper {
    public static final String ID = "_id";
    public static final String DEFAULT_SORT_ORDER = "_id";
    public static final String AUTHORITY = Config.URI_AUTHORITY;
    public static final char BLUE = 'b';
    public static final String EXTRAS_POSITION = "notepaper.extra.POSITION";
    public static final boolean FUNCTION_GROUP = true;
    public static final boolean FUNCTION_RESTORE = true;
    public static final char GREEN = 'g';
    public static final char PINK = 'p';
    public static final String PROPERTY_NOTE_GROUP = "debug.note.group";
    public static final String PROPERTY_NOTE_RESTORE = "debug.note.restore";
    public static final char WHITE = 'w';
    public static final char YELLOW = 'y';

    private NotePaper() {
    }

    public static final class Notes implements BaseColumns {

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.notepaper.note";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.notepaper.note";
        public static final Uri CONTENT_URI = Uri.parse("content://" + NotePaper.AUTHORITY + "/notes");

        public static final String UUID = "uuid";
        public static final String TITLE = "title";
        public static final String NOTE = "note";
        public static String FIRST_IMAGE = "first_img";
        public static String FIRST_RECORD = "first_record";

        public static String CATEGORY = "category";
        public static final String CREATE_TIME = "create_time";
        public static final String MODIFIED_DATE = "modified";
        public static final String COLOR = "color";
        public static String TOP = "top";

        public static final String PAPER = "paper";
        public static String FONT_COLOR = "font_color";
        public static String FONT_SIZE = "font_size";

        public static String LABELS="labels";

        public static final String DEFAULT_SORT_ORDER = "top DESC,modified DESC";

        private Notes() {
        }
    }


    public static final class NoteCategory implements BaseColumns {

        public static final String CATEGORY_NAME = "name";
        public static final String CATEGORY_ORDER = "sort";
        public static final String CATEGORY_UUID = "uuid";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.notepaper.category";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.notepaper.category";
        public static final Uri CONTENT_URI = Uri.parse("content://" + NotePaper.AUTHORITY + "/category");
        public static final String DEFAULT_SORT_ORDER = "sort ASC,name ASC";
        public static String DELETE = "deleted";

        private NoteCategory() {
        }
    }

    public static final class NoteFiles implements BaseColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.notepaper.notefile";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.notepaper.notefile";
        public static final Uri CONTENT_URI = Uri.parse("content://" + NotePaper.AUTHORITY + "/notefiles");

        public static final String DEFAULT_SORT_ORDER = "_id";
        public static final String FILE_MTIME = "mtime";
        public static final String FILE_NAME = "name";
        public static final String NOTE_UUID = "note_uuid";
        public static final String TYPE = "type";
        public static final int TYPE_AUDIO = 1;
        public static final int TYPE_IMAGE = 0;
        private NoteFiles() {
        }
    }

        public static final class LabelContent implements BaseColumns {

            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.notepaper.label";
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.notepaper.label";
            public static final Uri CONTENT_URI = Uri.parse("content://" + NotePaper.AUTHORITY + "/labels");
            public static final String CREATE_TABLE_SQL = "CREATE TABLE label_item (_id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT);";

            public static final String DEFAULT_SORT_ORDER = "_id";

            private LabelContent(){

            }

        }

}
