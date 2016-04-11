package top.leixiao.mindnote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * 自定义View
 * 监听笔记内容如果为空，当按返回键时关闭编辑页面
 */

public class EditTextClose extends EditText {
    private OnKeyPreImeCheckContentEmptyListener mOnKeyPreImeListener;

    public EditTextClose(Context context) {
        super(context);
    }

    public EditTextClose(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextClose(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        //如果没有笔记内容，直接关闭页面，否则调用父类，只关闭键盘
        //这里onKeyPreIme检测笔记内容是否为空，不为空时才执行return super.onKeyPreIme(keyCode, event);
        if (this.mOnKeyPreImeListener == null || !this.mOnKeyPreImeListener.onKeyPreImeCheckContentEmpty(this, keyCode, event)) {
            return super.onKeyPreIme(keyCode, event);
        }
        return true;
    }

    public void setOnKeyPreImeListener(OnKeyPreImeCheckContentEmptyListener l) {
        this.mOnKeyPreImeListener = l;
    }

    public interface OnKeyPreImeCheckContentEmptyListener {
        boolean onKeyPreImeCheckContentEmpty(View view, int i, KeyEvent keyEvent);
    }
}
