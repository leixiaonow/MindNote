package top.leixiao.mindnote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import top.leixiao.mindnote.NoteEditActivity;
import top.leixiao.mindnote.R;

/**
 * 根据type设置当不同的情况下显示不同的图片
 */

public class CheckImageView extends ImageView {
    int mType = 0;

    public CheckImageView(Context context) {
        super(context);
        init();

    }

    public CheckImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //设置图片显示的资源和是否隐藏根据type判断，浮动时
    public void setSmallImageType(int type) {
        this.mType = type;
        switch (this.mType) {
            case 0 /*0*/:
                setImageDrawable(null);
                setVisibility(GONE);
                return;
            case 1 /*1*/:
                setImageResource(R.drawable.ic_tab_check_small_off);
                setVisibility(VISIBLE);
                return;
            case 2 /*2*/:
                setImageResource(R.drawable.ic_tab_check_small_on);
                setVisibility(VISIBLE);
                return;
            default:
                return;
        }
    }

    //设置图片显示的资源和是否隐藏根据type判断，分享时
    public void setShareImageType(int type) {
        this.mType = type;
        switch (this.mType) {
            case 0 /*0*/:
                setImageDrawable(null);
                setVisibility(GONE);
                return;
            case 1 /*1*/:
                setImageResource(R.drawable.ic_tab_share_small_off);
                setVisibility(VISIBLE);
                return;
            case 2 /*2*/:
                setImageResource(R.drawable.ic_tab_share_small_on);
                setVisibility(VISIBLE);
                return;
            default:
                return;
        }
    }

    //返回显示类型type
    public int getImageType() {
        return this.mType;
    }

    //设置图片显示的资源和是否隐藏根据type判断，编辑edit时
    public void setImageType(int type) {
        this.mType = type;
        switch (this.mType) {
            case 0 /*0*/:
                setImageDrawable(null);
                setVisibility(GONE);
                return;
            case 1 /*1*/:
                setImageResource(R.drawable.ic_tab_check_off);
                setVisibility(VISIBLE);
                return;
            case 2 /*2*/:
                setImageResource(R.drawable.ic_tab_check_on);
                setVisibility(VISIBLE);
                return;
            default:
                return;
        }
    }

    void init() {
        OnClickListener listener = null;
        //可以在使用的时候添加监听，从而不再依赖NoteEditActivity
        if (getContext() instanceof NoteEditActivity) {
            listener = ((NoteEditActivity) getContext()).getCheckClickListener();
        }
        setOnClickListener(listener);
    }
}
