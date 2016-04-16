package top.leixiao.mindnote.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.util.ArrayList;

import top.leixiao.mindnote.MainActivity;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.adapter.viewholder.MyViewHolder;
import top.leixiao.mindnote.database.NoteData;
import top.leixiao.mindnote.utils.HanziToPinyin;
import top.leixiao.mindnote.utils.NoteUtil;

/**
 * Created by LeiXiao on 2016/3/17.
 */
public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    Context context;
    public ArrayList<NoteData> notes;
    private OnItemClickLitener mOnItemClickLitener;

    public MainRecyclerViewAdapter(Context context, ArrayList<NoteData> notes) {

        this.context = context;
        this.notes = notes;

    }

    //在MainActivity中调用这个方法，传入一个自己写的OnItemClickLitener接口的实现对象
    //保存到mOnItemClickLitener中
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.notes_row, parent, false));
        return holder;
    }

    //根据position加载数据，并设置传入的ViewHolder中各种从item布局加载的View的属性
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NoteData note = notes.get(position);

        holder.note_item_title.setText(note.mTitle);
//        NoteItemImage nti = NoteData.getFirstImage(note.mFirstImg);
//        if (nti != null) {
//            File file = NoteUtil.getFile(note.mUUId, nti.mFileName);
//            holder.note_item_image.setImageURI(Uri.fromFile(file));
//            holder.note_item_image.setVisibility(View.VISIBLE);
//        }else {
//            holder.note_item_image.setVisibility(View.GONE);
//        }
            holder.note_item_image.setVisibility(View.GONE);
        if (((MainActivity)context).isActionMode){
            holder.note_item_checkbox.setVisibility(View.VISIBLE);
        }
        else {
            holder.note_item_checkbox.setVisibility(View.INVISIBLE);
        }

        if (note.isSelected) {
            holder.note_item_checkbox.setChecked(true);

        } else {
            holder.note_item_checkbox.setChecked(false);
        }
        String modifyTime = context.getResources().getString(R.string.last_modified) + HanziToPinyin.Token.SEPARATOR + NoteUtil.getDate(context, note.mModifyTime);
        holder.note_item_time.setText(String.valueOf(modifyTime));


        // 在加载holder时，如果设置了回调，则为每个itemView设置点击事件，和长按事件
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    //
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);//实现接口的方法中实现了对要操作的数据的引用

        void onItemLongClick(View view, int position);
    }




}