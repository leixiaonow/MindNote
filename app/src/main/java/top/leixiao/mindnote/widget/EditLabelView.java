package top.leixiao.mindnote.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import top.leixiao.mindnote.R;

public class EditLabelView extends ViewGroup {
    private int mHorizonMargin;
    private int mHorizonPadding;
    private int mScreenWidth;
    private int mVerticalMargin;

    public  float sDensity;
    public  int sScreenHeight;
    public  int sScreenWidth;

    public  void initScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        sScreenWidth = Math.min(metrics.widthPixels, metrics.heightPixels);
        sScreenHeight = Math.max(metrics.widthPixels, metrics.heightPixels);
        sDensity = metrics.density;
    }

    public EditLabelView(Context context) {
        this(context, null);
    }

    public EditLabelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditLabelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        initScreenSize(context);
        Resources res = context.getResources();
        this.mScreenWidth = sScreenWidth;
        this.mHorizonPadding = res.getDimensionPixelSize(R.dimen.edit_note_item_margin);
        this.mHorizonMargin = res.getDimensionPixelSize(R.dimen.edit_note_label_item_horizon_margin);
        this.mVerticalMargin = res.getDimensionPixelSize(R.dimen.edit_note_label_item_vertical_margin);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            getChildAt(index).measure(0, 0);
        }
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getWrapHeight());
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int right = this.mScreenWidth - this.mHorizonPadding;
        int row = 0;
        int totalX = l + this.mHorizonPadding;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (totalX == this.mHorizonPadding + l) {
                totalX += width;
            } else {
                totalX += this.mHorizonMargin + width;
            }
            if (totalX > right) {
                row++;
                totalX = (this.mHorizonPadding + l) + width;
            }
            int totalY = (row + 1) * (this.mVerticalMargin + height);
            child.layout(totalX - width, totalY - height, totalX, totalY);
        }
    }

    private int getWrapHeight() {
        int count = getChildCount();
        int right = this.mScreenWidth - this.mHorizonPadding;
        int row = 0;
        int totalX = this.mHorizonPadding;
        int totalY = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (totalX == this.mHorizonPadding) {
                totalX += width;
            } else {
                totalX += this.mHorizonMargin + width;
            }
            if (totalX > right) {
                row++;
                totalX = this.mHorizonPadding + width;
            }
            totalY = (row + 1) * (this.mVerticalMargin + height);
        }
        return this.mVerticalMargin + totalY;
    }
}
