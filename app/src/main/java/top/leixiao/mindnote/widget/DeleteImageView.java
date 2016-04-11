package top.leixiao.mindnote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * 根据type设置图片隐藏
 */
//删除按钮
public class DeleteImageView extends ImageView {

    public DeleteImageView(Context context) {
        super(context);
    }

    public DeleteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageType(int type) {
        switch (type) {
            case 0 /*0*/:
                setVisibility(GONE);
                return;
            default:
                return;
        }
    }
}
