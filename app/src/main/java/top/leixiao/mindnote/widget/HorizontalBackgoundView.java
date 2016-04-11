package top.leixiao.mindnote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import top.leixiao.mindnote.NoteEditActivity;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.utils.NoteUtil;
import top.leixiao.mindnote.utils.ReflectUtils;


public class HorizontalBackgoundView extends HorizontalScrollView {
    ColorView mCurrentBackView;
    OnClickListener mBackgroundImgClickListener = new OnClickListener() {
        public void onClick(View v) {
            ColorView iv = (ColorView) v;
            if (v != HorizontalBackgoundView.this.mCurrentBackView) {
                if (HorizontalBackgoundView.this.mCurrentBackView != null) {
                    HorizontalBackgoundView.this.mCurrentBackView.setSelected(false);
                }
                HorizontalBackgoundView.this.mCurrentBackView = iv;
                iv.setSelected(true);
                if (v.getContext() instanceof NoteEditActivity) {
                    ((NoteEditActivity) v.getContext()).onBackgroundChanged(((Integer) iv.getTag()).intValue());
                }
            }
        }
    };
    int mItemWidth;

    public HorizontalBackgoundView(Context context) {
        super(context);
        init();
    }

    public HorizontalBackgoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalBackgoundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
        this.mItemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.color_cell_size);
        int space = getContext().getResources().getDimensionPixelSize(R.dimen.color_cell_margin);
        LayoutParams p = new LayoutParams(-2, -1);
        LinearLayout lineView = new LinearLayout(getContext());
        lineView.setOrientation(LinearLayout.HORIZONTAL);
        addView(lineView, p);
        int size = NoteUtil.COLOR_BACKGROUND.length;
        for (int index = 0; index < size; index++) {
            ColorView iv = new ColorView(getContext());
            iv.setColor(NoteUtil.COLOR_BACKGROUND[index]);
            iv.setSelected(false);
            iv.setTag(Integer.valueOf(index));
            iv.setOnClickListener(this.mBackgroundImgClickListener);
            LayoutParams p1 = new LayoutParams(this.mItemWidth, this.mItemWidth);
            p1.gravity = 16;
            lineView.addView(iv, p1);
            MarginLayoutParams mlp = (MarginLayoutParams) iv.getLayoutParams();
            mlp.rightMargin = space;
            if (index == size - 1) {
                mlp.rightMargin = 0;
            }
            iv.setLayoutParams(mlp);
        }
    }

    public void setFocusBackground(int id) {
        ViewGroup child = (ViewGroup) getChildAt(0);
        if (child != null) {
            if (id >= child.getChildCount() || id < 0) {
                id = 0;
            }
            ColorView iv = (ColorView) child.getChildAt(id);
            iv.setSelected(true);
            this.mCurrentBackView = iv;
            if (id >= 5) {
                ReflectUtils.setScrollX(this, this.mItemWidth * id);
            }
        }
    }
}
