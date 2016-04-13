package top.leixiao.mindnote.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.regex.Pattern;

import top.leixiao.mindnote.R;
import top.leixiao.mindnote.utils.InputTextNumLimitHelp;
import top.leixiao.mindnote.utils.InputTextNumLimitHelp.TextChangedListener;

public class NoteLabelAddView extends LinearLayout implements OnClickListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "NoteLabelAddView";
    private EditText mInputMsgView;
    private InputTextNumLimitHelp mInputTextNumLimitHelp;
    private ImageView mOkMsgView;
    private OnAddLabelListener mOnAddLabelListener;
    private OnEditorActionListener mOnEditorActionListener;
    private Runnable mShowImeRunnable;
    private TextChangedListener mTextChangedListener;

    class C03741 implements Runnable {
        C03741() {
        }

        public void run() {
            InputMethodManager imm = (InputMethodManager) NoteLabelAddView.this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(NoteLabelAddView.this.mInputMsgView, 0);
            }
        }
    }

    class C03752 implements OnEditorActionListener {
        C03752() {
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == 6) {
                NoteLabelAddView.this.onOkBtnClicked();
            }
            return true;
        }
    }

    public interface OnAddLabelListener {
        void onAddLabel(String str);
    }

    class C05313 implements TextChangedListener {
        C05313() {
        }

        public void onTextChange() {
            NoteLabelAddView.this.onTextChanged();
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.label_custom_edit_button) {
            onOkBtnClicked();
        }
    }

    public NoteLabelAddView(Context context) {
        this(context, null);
    }

    public NoteLabelAddView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteLabelAddView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mShowImeRunnable = new C03741();
        this.mOnEditorActionListener = new C03752();
        this.mTextChangedListener = new C05313();
    }

    public void initWatcher(EditText editText) {
        this.mInputTextNumLimitHelp = new InputTextNumLimitHelp(editText, 30, 15, 30);
        this.mInputTextNumLimitHelp.setTextChangedListener(this.mTextChangedListener);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInputMsgView = (EditText) findViewById(R.id.label_custom_edit_text);
        initWatcher(this.mInputMsgView);
        this.mInputMsgView.setOnEditorActionListener(this.mOnEditorActionListener);
        this.mInputMsgView.requestFocus();
        setImeVisibility(true);
        this.mOkMsgView = (ImageView) findViewById(R.id.label_custom_edit_button);
        this.mOkMsgView.setEnabled(false);
        this.mOkMsgView.setOnClickListener(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mInputMsgView.setOnEditorActionListener(null);
        if (this.mInputTextNumLimitHelp != null) {
            this.mInputTextNumLimitHelp.unRegisterWatcher();
        }
        this.mInputTextNumLimitHelp = null;
        this.mOnAddLabelListener = null;
        this.mOnEditorActionListener = null;
    }

    public void setOnAddLabelListener(OnAddLabelListener listener) {
        this.mOnAddLabelListener = listener;
    }

    private void onTextChanged() {
        updateOkButton();
    }

    private void updateOkButton() {
        this.mOkMsgView.setEnabled(!TextUtils.isEmpty(this.mInputMsgView.getText().toString().trim()) ? true : DEBUG);
    }

    private void onOkBtnClicked() {
//        String labelName = lineSpaceFilter(this.mInputMsgView.getText().toString().trim());
        String labelName = this.mInputMsgView.getText().toString();
        this.mInputMsgView.setText("");
        this.mInputMsgView.setHint("新标签名");
//        this.mInputMsgView.requestFocus();
        setImeVisibility(false);
        if (!TextUtils.isEmpty(labelName) && this.mOnAddLabelListener != null) {
            this.mOnAddLabelListener.onAddLabel(labelName);
        }
    }

    public static String lineSpaceFilter(String source) {
        return Pattern.compile("\\s*|\t|\r|\n").matcher(source).replaceAll("");
    }

    private void setImeVisibility(boolean visible) {
        if (visible) {
            post(this.mShowImeRunnable);
            return;
        }
        removeCallbacks(this.mShowImeRunnable);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(this.mInputMsgView.getWindowToken(), 0);
        }
    }

    public void clearFocus() {
        this.mInputMsgView.clearFocus();
        setImeVisibility(false);
        super.clearFocus();
    }

}
