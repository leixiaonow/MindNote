package top.leixiao.mindnote;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;

import top.leixiao.mindnote.adapter.MainRecyclerViewAdapter;
import top.leixiao.mindnote.catogery.CatogetyCustomActivity.CatogeryHolder;
import top.leixiao.mindnote.database.NoteData;
import top.leixiao.mindnote.database.NotePaper;
import top.leixiao.mindnote.utils.Constants;

public class MainActivity extends AppCompatActivity {
    private static final int NEW_NOTE_RESULT_CODE = 4;
    private static final int EDIT_NOTE_RESULT_CODE = 5;
    private Boolean isAddOpen = false;
    private Boolean isGridView = false;
    public Boolean isActionMode = false;
    private TextView emptyListTextView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionsMenu menuMultipleActions;
    private FloatingActionButton actionButton_JiShi;
    private FloatingActionButton actionButton_ZhaoPian;
    private ArrayList<Integer> selectedPositions;//被选中的位置
    private ArrayList<NoteData> notesData;//笔记数据
    private ActionMode.Callback actionModeCallback;//callback的声明
    private ActionMode actionMode;
    private MainRecyclerViewAdapter mAdapter;
    private Cursor mCursor;
    private MenuItem mMenuView;
    private MenuItem mMenuSet;
    ListView mDrawerMenuListView;
    View drawerRootView;
    private String[] mProjection = new String[]{"_id", "name"};
    String mOrderBy="_id";
    ListAdapter mDrawerListAdapter;
    int mCategory=-1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        initNoteData();
        initCatogeryData();
        selectedPositions = new ArrayList<>();

        emptyListTextView = (TextView) findViewById(android.R.id.empty);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        actionButton_ZhaoPian = (FloatingActionButton) findViewById(R.id.multiple_actions);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.notes);

        isGridView = false;
        if (isGridView) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }


        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter = new MainRecyclerViewAdapter(this, notesData));
        updateView();

        //添加监听事件，简直是太棒了
        mAdapter.setOnItemClickLitener(new MainRecyclerViewAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

                if (!isActionMode) {
                    Intent intent=new Intent(MainActivity.this,NoteEditActivity.class);
                    intent.putExtra("id",notesData.get(position).mId);
                    intent.putExtra("type",-5);
                    startActivityForResult(intent,EDIT_NOTE_RESULT_CODE);
                } else {
                    if (selectedPositions.contains(position)) {
                        notesData.get(position).isSelected=false;
                        selectedPositions.remove((Object) position);
                        mAdapter.notifyDataSetChanged();
                        if (selectedPositions.size() == 0) {
                            actionMode.finish();
                            isActionMode = false;
                            return;
                        }
                        actionMode.setTitle(String.valueOf(selectedPositions.size()));

                    } else {
                        selectedPositions.add(position);
                        notesData.get(position).isSelected=true;
                        actionMode.setTitle(String.valueOf(selectedPositions.size()));
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

                if (!isActionMode) {
                    isActionMode = true;
                    notesData.get(position).isSelected=true;
                    mAdapter.notifyDataSetChanged();
                    selectedPositions.add(position);
                    //进入ActionMode
                    actionMode = startSupportActionMode(actionModeCallback);
                    actionMode.setTitle(String.valueOf(selectedPositions.size()));
                }
            }
        });


        //切换Layout
        actionButton_ZhaoPian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.setData(NotePaper.Notes.CONTENT_URI);
                intent.putExtra(Constants.JSON_KEY_TYPE, -1);
                intent.putExtra("id", -1);
                intent.putExtra("pos", -1);
                intent.putExtra("category",mCategory);
                startActivityForResult(intent,NEW_NOTE_RESULT_CODE);
            }
        });

        actionModeCallback = new ActionMode.Callback() {

            //开始ActionMode的时候，调用setListOnItemClickListenersWhenActionMode（）方法
            //添加ActionMode的时候的点击事件。同时加载ActionMode下的导航菜单
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //    setListOnItemClickListenersWhenActionMode();
                mode.getMenuInflater().inflate(R.menu.context_note, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            //当点击导航菜单时的方法
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        if (!selectedPositions.isEmpty()) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(getString(R.string.delete_notes_alert, selectedPositions.size()))
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteNotes(selectedPositions);
                                            mode.finish();
                                            isActionMode = false;
                                        }
                                    })
                                    .show();
                        } else mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            //当退出ActionMode的时候调用的方法
            //设置不是ActionMode的时候的点击事件
            //重置SelectedListItems
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //   setListOnItemClickListenersWhenNoActionMode();
//                updataRecyclor();
                isActionMode=false;
                mAdapter.notifyDataSetChanged();
                resetSelectedListItems();
            }
        };

        mDrawerMenuListView=(ListView) findViewById(R.id.left_drawer_listview);
        drawerRootView= findViewById(R.id.left_drawer);

        mDrawerListAdapter=new ListAdapter(this);
        mDrawerMenuListView.setAdapter(mDrawerListAdapter);
        mDrawerMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){
                    return;
                }
                if (position==1){
                    mCategory=-1;
                    initNoteData();
                    mAdapter.notes=notesData;
                    updateView();
                    mAdapter.notifyDataSetChanged();
                    return;
                }
                mCategory=((NoteAppImpl)getApplication()).mCatogetys.get(position).mId;
                initNoteDataByCategory(mCategory);
                mAdapter.notes=notesData;
                updateView();
                mAdapter.notifyDataSetChanged();
            }
        });
        mDrawerMenuListView.setSelected(true);
        mDrawerMenuListView.setSelection(1);
    }
    private void initNoteDataByCategory(int category){

        this.mCursor = this.getContentResolver().query(NotePaper.Notes.CONTENT_URI, NoteData.NOTES_PROJECTION, "category="+category, null, NotePaper.Notes.DEFAULT_SORT_ORDER);
        notesData = new ArrayList<>();
        if (mCursor == null) {
            return;
        }
        while (mCursor.moveToNext()) {
            notesData.add(NoteData.getItem(mCursor));
        }
        mCursor.close();
    }

    private void initNoteData() {
        this.mCursor = this.getContentResolver().query(NotePaper.Notes.CONTENT_URI, NoteData.NOTES_PROJECTION, null, null, NotePaper.Notes.DEFAULT_SORT_ORDER);
        notesData = new ArrayList<>();

        if (mCursor == null) {
            return;
        }
        while (mCursor.moveToNext()) {
            notesData.add(NoteData.getItem(mCursor));
        }
        mCursor.close();
    }

    void initCatogeryData(){
        Cursor cursor = getContentResolver().query(NotePaper.NoteCategory.CONTENT_URI, this.mProjection, null, null, this.mOrderBy);
        if (cursor == null) {
            return;
        }
        ArrayList<CatogeryHolder> catogerys = new ArrayList<>();
        while (cursor.moveToNext()) {
            catogerys.add(new CatogeryHolder(cursor.getInt(0), cursor.getString(1)));
        }
        cursor.close();
        ((NoteAppImpl)this.getApplication()).mCatogetys = new ArrayList<>(catogerys);

    }


    private void resetSelectedListItems() {
        for (NoteData note : notesData)
            note.isSelected=false;
        selectedPositions.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void deleteNotes(ArrayList<Integer> selectedPositions) {
        ArrayList<NoteData> toRemoveList = new ArrayList<>(selectedPositions.size());
        for (int position : selectedPositions) {
            NoteData note = notesData.get(position);
            toRemoveList.add(note);
            Uri noteUri = ContentUris.withAppendedId(NotePaper.Notes.CONTENT_URI, note.mId);//得到笔记的唯一路径，ContentProvider需要
            getContentResolver().delete(noteUri,null,null);
        }

        for (NoteData noteToRemove : toRemoveList) notesData.remove(noteToRemove);
        updateView();
        updataRecyclor();
    }


    private void updateNote(Intent data) {
//        Note updatedNote = ViewNoteActivity.getExtraUpdatedNote(data);
//        noteDAO.update(updatedNote);
//        for (Note note : notesData) {
//
//            if (note.getId().equals(updatedNote.getId())) {
//                note.setTitle(updatedNote.getTitle());
//                note.setContent(updatedNote.getContent());
//                note.setUpdatedAt(updatedNote.getUpdatedAt());
//            }
//        }
//        mAdapter.notifyDataSetChanged();
    }

    //开始运行时，若没有笔记就让recyclerView 消失显示一句话
    private void updateView() {
        if (notesData.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }
    }

    //创建选项菜单 OptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.main, menu);
        this.mMenuView = menu.findItem(R.id.action_chang_view);
        this.mMenuSet = menu.findItem(R.id.action_set);
        Log.d("menu", "onCreateOptionsMenu: isgridView"+isGridView);
        if(isGridView){
            mMenuView.setTitle(R.string.main_list);
        }else {
            mMenuView.setTitle(R.string.main_grid);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chang_view:
                if (isGridView) {
                    isGridView = false;
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    item.setTitle(R.string.main_grid);

                } else {
                    isGridView = true;
                    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                    item.setTitle(R.string.main_list);

                }
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    //视图返回时调用
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ok", "onActivityResult requestCode: "+requestCode);
        Log.d("ok", "onActivityResult resultCode: "+resultCode);
                Log.d("result_ok", "onActivityResult: "+NEW_NOTE_RESULT_CODE);
        updataRecyclor();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updataRecyclor(){
        isActionMode=false;
        initNoteDataByCategory(mCategory);
        updateView();
        mAdapter.notes=notesData;
        mAdapter.notifyDataSetChanged();
    }


    private void addNote(Intent data) {
//        Note note = EditNoteActivity.getExtraNote(data);
//        noteDAO.insert(note);
//        notesData.add(0, note);//插入首部
//        updateView();
//        recyclerView.scrollToPosition(0);
//        mAdapter.notifyItemInserted(0);//通知首部插入数据
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private class ListAdapter extends BaseAdapter{


        public Context mContext;
        public LayoutInflater mInflater;

        public ListAdapter(Context context){
            super();
            mContext=context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return ((NoteAppImpl)getApplication()).mCatogetys.size();
        }

        @Override
        public Object getItem(int position) {
            return ((NoteAppImpl)getApplication()).mCatogetys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null){
                convertView = mInflater.inflate(R.layout.drawer_list_item_layout, null);
                holder = new Holder();
                holder.textView = (TextView)convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
            }else{
                holder = (Holder)convertView.getTag();
            }
            holder.textView.setText(((NoteAppImpl)getApplication()).mCatogetys.get(position).mName);
            return convertView;
        }
    }

    static class Holder {
        TextView textView;
    }

}
