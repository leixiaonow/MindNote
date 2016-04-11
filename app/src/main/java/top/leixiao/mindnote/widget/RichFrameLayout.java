package top.leixiao.mindnote.widget;

import android.content.Context;
import android.support.v7.appcompat.BuildConfig;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import top.leixiao.mindnote.NoteEditActivity;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.utils.InputMethodManagerUtils;


public class RichFrameLayout extends LinearLayout {
    String mFileName;
    EditText mFocusEdit;
    String mUUID;
    //按键监听，当按键是0或是27的时候删除自己
    OnKeyListener mEditKeyPreListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != 0 || keyCode != 67) {
                return false;
            }
            RichFrameLayout.this.deleteRichLayout();
            return true;
        }
    };


    public RichFrameLayout(Context context) {
        super(context);
    }

    public RichFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RichFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFocusEdit = (EditText) findViewById(R.id.edit);
        this.mFocusEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    RichFrameLayout.this.getChildAt(0).setSelected(true);
                } else {
                    RichFrameLayout.this.getChildAt(0).setSelected(false);
                }
            }
        });
        this.mFocusEdit.setOnKeyListener(this.mEditKeyPreListener);
        InputFilter inputFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (RichFrameLayout.this.getContext() instanceof NoteEditActivity) {
                    ((NoteEditActivity) RichFrameLayout.this.getContext()).insertInRichItem(RichFrameLayout.this, source.subSequence(start, end));
                }
                return BuildConfig.VERSION_NAME;
            }
        };
        this.mFocusEdit.setFilters(new InputFilter[]{inputFilter});
    }

    public void onFocus() {
        this.mFocusEdit.requestFocus();
        InputMethodManager imm = InputMethodManagerUtils.peekInstance();
        imm.viewClicked(this.mFocusEdit);
        imm.showSoftInput(this.mFocusEdit, 0);
    }

    public String getFileName() {
        return this.mFileName;
    }

    public String getUUID() {
        return this.mUUID;
    }

    public boolean isFocused() {
        return this.mFocusEdit.hasFocus();
    }

    //为图片或录音设置数据，调用相应对象的setUUIDandName(uuid, name)方法
    public void setUUIDandName(String uuid, String name) {
        this.mUUID = uuid;
        this.mFileName = name;
        if ("image".equals(getTag())) {
            ((ScaleImageView) findViewById(R.id.image)).setUUIDandName(uuid, name);
        } else if ("record".equals(getTag())) {
            ((RecordLinearLayout) findViewById(R.id.recordLayout)).setUUIDandName(uuid, name);
        }
    }

    //为图片设置尺寸
    public void setSize(int width, int height) {
        if ("image".equals(getTag())) {
            ((ScaleImageView) findViewById(R.id.image)).setSize(width, height);
        }
    }

    //执行从传入的Context中删除自己
    public void deleteRichLayout() {
        //noinspection ConstantConditions
        ((NoteEditActivity) getContext()).removeFocusView(this);
    }
}
