package top.leixiao.mindnote.adapter.viewholder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import top.leixiao.mindnote.R;
import top.leixiao.mindnote.mainWidget.AmigoCheckBox;
import top.leixiao.mindnote.mainWidget.MultiTextView;
import top.leixiao.mindnote.mainWidget.NoteCardBottomView;

/**
 * Created by LeiXiao on 2016/3/18.
 */
public class MyViewHolder extends RecyclerView.ViewHolder {



    public CardView cardView;
    public LinearLayout note_item_container;
    public ImageView note_item_image;
    public NoteCardBottomView note_item_card_bottom_view;
    public ImageView note_item_reminder;
    public TextView note_item_title;
    public AmigoCheckBox note_item_checkbox;
    public TextView note_item_time;
    public MultiTextView note_item_content;
    public View note_item_content_onclick_view;

    public MyViewHolder(View view) {
        super(view);

        cardView = (CardView) view.findViewById(R.id.card_view);
        note_item_container= (LinearLayout)view.findViewById(R.id.note_item_container);
        note_item_image=(ImageView) view.findViewById(R.id.note_item_image);
        note_item_card_bottom_view=(NoteCardBottomView) view.findViewById(R.id.note_item_card_bottom_view);
        note_item_reminder=(ImageView) view.findViewById(R.id.note_item_reminder);
        note_item_title=(TextView) view.findViewById(R.id.note_item_title);
        note_item_checkbox=(AmigoCheckBox) view.findViewById(R.id.note_item_checkbox);
        note_item_time=(TextView) view.findViewById(R.id.note_item_time);
        note_item_content=(MultiTextView) view.findViewById(R.id.note_item_content);
        note_item_content_onclick_view= view.findViewById(R.id.note_item_content_onclick_view);

    }

}
