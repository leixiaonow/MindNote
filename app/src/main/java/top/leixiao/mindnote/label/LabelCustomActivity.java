package top.leixiao.mindnote.label;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import top.leixiao.mindnote.NoteAppImpl;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.database.NotePaper;
import top.leixiao.mindnote.widget.NoteLabelAddView;
public class LabelCustomActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "LabelCustomActivity";
    private ListView mListView;
    private ListAdapter mAdapter;
    private ArrayList mLabels;
    private NoteLabelAddView mNoteLabelAddView;
    private LayoutInflater mInflater;
    private final ArrayList<LabelHolder> mLabelCache = new ArrayList<>();
    private ContentResolver mResolver;
    private Uri mLabelUri = Uri.parse("content://"+NotePaper.AUTHORITY+"/labels");
    private String[] mProjection = new String[]{"_id", "content"};
//    private String[] mProjection = new String[]{"_id"};
    private String mOrderBy = "_id DESC";
    private int INDEX_CONTENT = 1;
    private int INDEX_ID = 0;
    private TextView mDelete;
    private TextView mselect;


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.label_select_cancel:
                return;
            case R.id.label_select_sure:
                Log.d(TAG, "onClick: "+"it't quite good");

//                if (this.mLabelChangedListener != null) {
//                    this.mLabelChangedListener.onLabelChanged(this.mSelectLabelIds);
//                }
                Iterator i$ = ((NoteAppImpl) getApplication()).mSelectLabelIds.iterator();

                while (i$.hasNext()) {
                    String labelName = getLabelContentById((Integer) i$.next());
                    if (labelName != null) {
                        Log.d(TAG, "onClick: "+labelName);

                    }
                }

                Log.d(TAG, "onClick: "+((NoteAppImpl)getApplication()).mSelectLabelIds.size());
                setResult(-1,null);
                finish();
                return;
            default:
                return;
        }

    }




    public String getLabelContentById(int id) {
        Iterator i$ = this.mLabels.iterator();
        while (i$.hasNext()) {
            LabelHolder holder = (LabelHolder) i$.next();
            if (holder.mId == id) {
                return holder.mContent;
            }
        }
        return null;
    }

    private class LabelSelectorAdapter extends BaseAdapter {
        private CheckListener mCheckListener = new CheckListener();

        private class CheckListener implements OnClickListener {
            private CheckListener() {
            }

            public void onClick(View v) {
                ViewHolder viewHolder = (ViewHolder) v.getTag();
                int id = viewHolder.labelId;
                if (viewHolder.checkBox.isChecked()) {
                    ((NoteAppImpl) getApplication()).mSelectLabelIds.remove(Integer.valueOf(id));
                } else {
                    ((NoteAppImpl) getApplication()).mSelectLabelIds.add(Integer.valueOf(id));
                }
                viewHolder.checkBox.toggle();
            }
        }

        public int getCount() {
            return mLabels.size();
        }

        public String getItem(int i) {
            return ((LabelHolder) mLabels.get(i)).mContent;
        }

        public long getItemId(int i) {
            return (long) ((LabelHolder) mLabels.get(i)).mId;
        }

        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.label_selector_item, null);
                viewHolder = new ViewHolder();
                viewHolder.contentView = convertView;
                viewHolder.textView = (TextView) convertView.findViewById(R.id.label_select_list_item_text);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.label_select_list_item_checkbox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textView.setText(getItem(i));
            viewHolder.contentView.setOnClickListener(this.mCheckListener);
            int labelId = (int) getItemId(i);
            viewHolder.labelId = labelId;
            viewHolder.checkBox.setChecked(isSelected(labelId));
            return convertView;
        }
    }

    public interface OnLabelChangedListener {
        void onLabelChanged(ArrayList<Integer> arrayList);

        void onUpdate();
    }

    private static class ViewHolder {
        public CheckBox checkBox;
        public View contentView;
        public int labelId;
        public TextView textView;
        private ViewHolder() {
        }
    }

    private boolean isSelected(int labelId) {
        return ((NoteAppImpl) getApplication()).mSelectLabelIds.contains(labelId);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.label_custom_content_layout);
        tintImageViewDrawable(R.id.label_custom_edit_button, R.drawable.label_custom_add_sure, R.color.label_custom_edit_sure_button);
        this.mResolver = getContentResolver();
        initData();
        initView();
//        setResult();
    }

    private void initCache() {

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResult(-1,null);
        super.onBackPressed();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void initData() {

        Cursor cursor = this.mResolver.query(this.mLabelUri, this.mProjection, null, null, this.mOrderBy);
        if (cursor == null) {
            return;
        }
        ArrayList<LabelHolder> labels = new ArrayList<>();
        while (cursor.moveToNext()) {
                labels.add(new LabelHolder(cursor.getInt(this.INDEX_ID), cursor.getString(this.INDEX_CONTENT)));
        }
        cursor.close();

        synchronized (this.mLabelCache) {
            this.mLabelCache.addAll(labels);
        }
        this.mLabels = getLabelList();
        ((NoteAppImpl)this.getApplication()).mlabels=mLabels;
                this.mInflater = LayoutInflater.from(getApplicationContext());
        this.mAdapter = new LabelSelectorAdapter();
//      this.mAdapter = new LabelCustomAdapter();

    }

    public ArrayList<LabelHolder> getLabelList() {
        ArrayList<LabelHolder> arrayList = null;
        synchronized (this.mLabelCache) {
            arrayList = new ArrayList<>(this.mLabelCache);
        }
        return arrayList;
    }

    private void initView() {
        this.mNoteLabelAddView = (NoteLabelAddView) findViewById(R.id.note_label_add_view);
        this.mListView = (ListView) findViewById(R.id.label_custom_list);
        this.mListView.setAdapter(this.mAdapter);
        this.mDelete=(TextView) findViewById(R.id.label_select_cancel);
        this.mselect=(TextView) findViewById(R.id.label_select_sure);
        mDelete.setOnClickListener(this);
        mselect.setOnClickListener(this);

        this.mNoteLabelAddView.setOnAddLabelListener(new MyOnAddLabelListener());
    }

    private void tintImageViewDrawable(int imageViewId, int iconId, int colorsId) {
        Drawable tintIcon = DrawableCompat.wrap(ContextCompat.getDrawable(this, iconId));
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(this, colorsId));
        ((ImageView) findViewById(imageViewId)).setImageDrawable(tintIcon);
    }

    private void setResult() {
        setResult(-1, null);
    }

    private void saveCustomLabel(String labelName) {

            if (containLabel(labelName)) {
                Toast.makeText(this, R.string.label_custom_exist, Toast.LENGTH_SHORT).show();
            }else {
                mLabels.add(new LabelHolder(mLabels.size()+1,labelName));
                insertLabelToDataBase(labelName);
            }
    }

    public void insertLabelToDataBase(String labelName){
        ContentValues initialValues=new ContentValues();
        initialValues.put("content",labelName);
         this.mResolver.insert(this.mLabelUri,initialValues);

    }

    private boolean containLabel(String content) {
        Iterator i$ = this.mLabels.iterator();
        while (i$.hasNext()) {
            if (TextUtils.equals(((LabelHolder) i$.next()).mContent, content)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlreadyHaveLabel(int id){
        Log.d(TAG, "isAlreadyHaveLabel: "+id);
        return ((NoteAppImpl) getApplication()).mSelectLabelIds.contains(id);
    }


    public static class LabelHolder {
        public String mContent;
        public int mId;

        public LabelHolder(int id, String content) {
            this.mId = id;
            this.mContent = content;
        }
    }


    class MyOnAddLabelListener implements NoteLabelAddView.OnAddLabelListener {
        MyOnAddLabelListener() {
        }
        public void onAddLabel(String newLabelName) {
            saveCustomLabel(newLabelName);
        }
    }




}
