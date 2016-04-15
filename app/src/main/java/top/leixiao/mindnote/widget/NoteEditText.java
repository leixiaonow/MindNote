package top.leixiao.mindnote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.appcompat.BuildConfig;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import top.leixiao.mindnote.NoteEditActivity;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.utils.ReflectUtils;


public class NoteEditText extends EditTextCloud {
    TextWatcher mTextWatch = new TextWatcher() {
        public void afterTextChanged(Editable editable) {
            ((NoteEditActivity) NoteEditText.this.getContext()).setTextChanged();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ((NoteEditActivity) NoteEditText.this.getContext()).setCount(((NoteEditActivity) NoteEditText.this.getContext()).getCount() + (count - before));
        }
    };
    private float mLastDownPositionX;
    private float mLastDownPositionY;
    private ClickableSpan[] mLinks;
    private CheckForLongPress mPendingCheckForLongPress = null;
    private int mTouchSlopSquare;

    public NoteEditText(Context context) {
        super(context);
        init();
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
        ReflectUtils.setKeepSelection(this, true);
        if (getContext() instanceof NoteEditActivity) {
            setOnKeyPreImeListener(((NoteEditActivity) getContext()).getKeyPreImeListener());
            setOnKeyListener(((NoteEditActivity) getContext()).getKeyPreListener());

        }
//        setOnFocusChangeListener(new OnFocusChangeListener() {
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (NoteEditText.this.getContext() instanceof NoteEditActivity) {
//                    NoteEditActivity na = (NoteEditActivity) NoteEditText.this.getContext();
//                    View parentView = (View) v.getParent();
//                    DeleteImageView deleteView = null;
//                    if (parentView != null) {
//                        deleteView = (DeleteImageView) parentView.findViewById(R.id.delete);
//                    }
//                    if (deleteView != null && ((CheckImageView) parentView.findViewById(R.id.check)).getImageType() != 0) {
//                        if (hasFocus) {
//                            deleteView.setVisibility(VISIBLE);
////                            deleteView.setOnClickListener(na.getDeleteClickListener());
//                        } else {
//                            deleteView.setVisibility(INVISIBLE);
//                        }
//                    }
//                }
//            }
//        });
        addTextChangedListener(this.mTextWatch);
        setFilters(new InputFilter[]{new CustomFilter()});
        int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mTouchSlopSquare = touchSlop * touchSlop;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!(getAutoLinkMask() == 0 || ((NoteEditActivity) getContext()).isEditMode())) {
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            switch (action & MotionEventCompat.ACTION_MASK) {
                case 0 /*0*/:
                    this.mLastDownPositionX = x;
                    this.mLastDownPositionY = y;
                    int offset = getOffsetForPosition(this.mLastDownPositionX, this.mLastDownPositionY);
                    CharSequence text = getText();
                    if (text != null && (text instanceof Spanned)) {
                        this.mLinks = (ClickableSpan[]) ((Spanned) text).getSpans(offset, offset, ClickableSpan.class);
                    }
                    if (this.mLinks != null && this.mLinks.length > 0) {
                        if (((Spanned) text).getSpanEnd(this.mLinks[0]) != offset) {
                            this.mPendingCheckForLongPress = new CheckForLongPress();
                            getHandler().postDelayed(this.mPendingCheckForLongPress, (long) ViewConfiguration.getLongPressTimeout());
                            break;
                        }
                        this.mLinks = null;
                        break;
                    }
                case 1 /*1*/:
                    if (this.mLinks != null && this.mLinks.length > 0) {
                        this.mLinks[0].onClick(this);
                        this.mLinks = null;
                        return true;
                    }
                case 2 /*2*/:
                    int scrollX = (int) (x - this.mLastDownPositionX);
                    int scrollY = (int) (y - this.mLastDownPositionY);
                    if ((scrollX * scrollX) + (scrollY * scrollY) > this.mTouchSlopSquare) {
                        getHandler().removeCallbacks(this.mPendingCheckForLongPress);
                        this.mLinks = null;
                        break;
                    }
                    break;
                case 3 /*3*/:
                    getHandler().removeCallbacks(this.mPendingCheckForLongPress);
                    this.mLinks = null;
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    protected void onDraw(Canvas canvas) {
        if (!(getContext() instanceof NoteEditActivity) || !((NoteEditActivity) getContext()).getCaptureState() || getText().length() != 0) {
            super.onDraw(canvas);
        }
    }

    protected void onDetachedFromWindow() {
        ((NoteEditActivity) getContext()).setCount(((NoteEditActivity) getContext()).getCount() - length());
        removeTextChangedListener(this.mTextWatch);
        super.onDetachedFromWindow();
    }

    class CheckForLongPress implements Runnable {
        CheckForLongPress() {
        }

        public void run() {
            if (NoteEditText.this.mLinks != null && NoteEditText.this.mLinks.length > 0) {
                NoteEditText.this.mLinks = null;
            }
        }
    }

    class CustomFilter implements InputFilter {
        CustomFilter() {
        }

        public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
            int keep = (20000 - ((NoteEditActivity) NoteEditText.this.getContext()).getCount()) + (i4 - i3);
            if (keep <= 0) {
                Toast.makeText(NoteEditText.this.getContext(), R.string.words_limit, Toast.LENGTH_SHORT).show();
                return BuildConfig.VERSION_NAME;
            } else if (keep >= i2 - i) {
                return null;
            } else {
                keep += i;
                Toast.makeText(NoteEditText.this.getContext(), R.string.words_limit, Toast.LENGTH_SHORT).show();
                if (Character.isHighSurrogate(charSequence.charAt(keep - 1))) {
                    keep--;
                    if (keep == i) {
                        return BuildConfig.VERSION_NAME;
                    }
                }
                return charSequence.subSequence(i, keep);
            }
        }
    }
}
