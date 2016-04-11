package top.leixiao.mindnote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import top.leixiao.mindnote.R;

/**
 * Created by LeiXiao on 2016/4/1.
 */
public class TextItem extends FrameLayout {

    CheckImageView checkImageView;
    DeleteImageView deleteImageView;
    EditTextClose editTextClose;
    public TextItem(Context context) {
        super(context);
    }

    public TextItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.text_item,this);
        checkImageView =(CheckImageView)findViewById(R.id.check_image_view);
        deleteImageView =(DeleteImageView)findViewById(R.id.delete_image_view);
        editTextClose =(EditTextClose)findViewById(R.id.edit_text_close);
        checkImageView.setImageType(1);
    }

    public TextItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
