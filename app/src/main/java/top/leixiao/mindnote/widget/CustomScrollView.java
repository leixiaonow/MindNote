package top.leixiao.mindnote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Selection;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import top.leixiao.mindnote.NoteEditActivity;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.utils.InputMethodManagerUtils;
import top.leixiao.mindnote.utils.NoteUtil;
import top.leixiao.mindnote.utils.ReflectUtils;


public class CustomScrollView extends ScrollView {
    final int BOUND = 80;
    final int OFFSET = 20;
    final String TAG = "CustomScrollView";
    boolean DEBUG = false;
    boolean first = true;
//    View mDragLine;
    int mDragPosition;
    View mDragView;
    int mDragY;
    LinearLayout mEditParent;
    View mEmptyView;
    boolean mFocusFlag;
    int mLowerBound;
    int mOffset;
    int mTopOffset;
    int mUpperBound;
    private boolean mRestoreSwitch = true;
    private Runnable mScrollRunnable = new Runnable() {
        public void run() {
            int speed = 0;
            int curY = CustomScrollView.this.mDragY;
            if (curY > CustomScrollView.this.mLowerBound) {
                speed = curY - CustomScrollView.this.mLowerBound > 40 ? 20 : 8;
            } else if (curY < CustomScrollView.this.mUpperBound) {
                speed = CustomScrollView.this.mLowerBound - curY > 40 ? -20 : -8;
            }
            if (speed != 0) {
                int oldScrollY = CustomScrollView.this.getScrollY();
                CustomScrollView.this.scrollBy(0, speed);
                CustomScrollView.this.updateDrag();
                CustomScrollView.this.removeCallbacks(CustomScrollView.this.mScrollRunnable);
                if (oldScrollY != CustomScrollView.this.getScrollY()) {
                    CustomScrollView.this.postDelayed(this, 40);
                    return;
                }
                return;
            }
            CustomScrollView.this.removeCallbacks(CustomScrollView.this.mScrollRunnable);
        }
    };

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mEmptyView = findViewById(R.id.empty);
        this.mEditParent = (LinearLayout) findViewById(R.id.edit_parent);
//        this.mDragLine = findViewById(R.id.drag_line);
        this.mOffset = getContext().getResources().getDimensionPixelSize(R.dimen.edit_text_line_space) + getContext().getResources().getDimensionPixelSize(R.dimen.edit_text_bottom_margin);
        View editparent = findViewById(R.id.edit_parent);
        if (editparent != null) {
            editparent.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int count = ((ViewGroup) v).getChildCount();
                    if (count >= 1) {
                        View last = ((ViewGroup) v).getChildAt(count - 1);
                        if (last != null && NoteUtil.JSON_TEXT.equals(last.getTag()) && last.getBottom() < v.getMeasuredHeight()) {
                            Point pt = new Point();
                            ReflectUtils.getLastTouchPoint(v, pt);
                            Rect r = new Rect();
                            last.getGlobalVisibleRect(r);
                            if (pt.y >= r.bottom) {
                                NoteEditText edit = (NoteEditText) last.findViewById(R.id.text);
                                edit.requestFocus();
                                Selection.setSelection(edit.getText(), edit.getText().length());
                                InputMethodManager imm = InputMethodManagerUtils.peekInstance();
                                imm.viewClicked(edit);
                                imm.showSoftInput(edit, 0);
                            }
                        }
                    }
                }
            });
        }
    }

    public boolean dispatchDragEvent(DragEvent event) {
        int dragAction = event.getAction();
        Object obj = event.getLocalState();
        if (obj == null || !(obj instanceof ListDragLocalState)) {
            return false;
        }
        if (this.DEBUG && event.getAction() != 2) {
            Log.d("CustomScrollView", "drag event: " + event.getAction());
        }
        this.mDragY = (int) event.getY();
        switch (event.getAction()) {
            case 1 /*1*/:
                this.mDragView = ((ListDragLocalState) obj).getDragView();
                this.mFocusFlag = ((ListDragLocalState) obj).getFocusFlag();
                Log.i("CustomScrollView", "mfocusFlag = " + this.mFocusFlag);
                ((ListDragLocalState) obj).getDragView().setVisibility(INVISIBLE);
                View frame = findViewById(R.id.frame_parent);
                View pparent = frame.findViewById(R.id.parent);
                int frameTop = frame.getTop();
                int pparentTop = pparent.getTop();
                this.mTopOffset = (frameTop + pparentTop) + this.mEditParent.getTop();
                this.mDragPosition = -1;
                this.mLowerBound = (getBottom() - getPaddingBottom()) - 80;
                this.mUpperBound = (getTop() + getPaddingTop()) + 80;
                if (this.DEBUG) {
                    Log.d("CustomScrollView", "mTopOffset: " + this.mTopOffset);
                    break;
                }
                break;
            case 2 /*2*/:
                updateDrag();
                if (this.mDragY <= this.mLowerBound && this.mDragY >= this.mUpperBound) {
                    removeCallbacks(this.mScrollRunnable);
                    break;
                }
                Handler handler = getHandler();
                if (handler == null || !ReflectUtils.hasCallbacks(handler, this.mScrollRunnable)) {
                    postDelayed(this.mScrollRunnable, 50);
                    break;
                }
                break;
            case 3 /*3*/:
                dropTo(this.mDragPosition, ((ListDragLocalState) obj).getDragView());
                break;
            case 4 /*4*/:
            case 100:
                Log.d("TAG", "drag end");
                ((ListDragLocalState) obj).getDragView().setVisibility(VISIBLE);
                if (this.mFocusFlag) {
                    View edit = ((ListDragLocalState) obj).getDragView().findViewById(R.id.text);
                    if (edit != null) {
                        edit.requestFocus();
                        InputMethodManager imm = InputMethodManagerUtils.peekInstance();
                        imm.viewClicked(edit);
                        imm.showSoftInput(edit, 0);
                        Log.i("CustomScrollView", "showSoftInput");
                    }
                }
//                this.mDragLine.setVisibility(GONE);
                removeCallbacks(this.mScrollRunnable);
                break;
            case 5 /*5*/:
                updateDrag();
                break;
            case 6 /*6*/:
                this.mDragPosition = -1;
//                this.mDragLine.setVisibility(GONE);
                removeCallbacks(this.mScrollRunnable);
                break;
        }
        return true;
    }

    void dropTo(int position, View v) {
        if (position != -1) {
            int count = this.mEditParent.getChildCount();
            int oldPos = -1;
            for (int index = 0; index < count; index++) {
                if (this.mEditParent.getChildAt(index) == v) {
                    oldPos = index;
                    break;
                }
            }
            if (oldPos < position) {
                position--;
            }
            this.mEditParent.removeViewAt(oldPos);
            this.mEditParent.addView(v, position);
            ((NoteEditActivity) getContext()).setTextChanged();
        }
    }

    int findDragPosition(int posY) {
        int y = ((getScrollY() + posY) - this.mTopOffset) - 40;
        if (this.DEBUG) {
            Log.d("CustomScrollView", "getScrollY: " + getScrollY() + " Y: " + y + " mDragPosition: " + this.mDragPosition);
        }
        if (y + 20 < 0 || y > this.mEditParent.getHeight() + 20) {
            return -1;
        }
        View child;
        int index;
        int count = this.mEditParent.getChildCount();
        int start = 0;
        if (this.mDragPosition != -1 && this.mDragPosition < count) {
            child = this.mEditParent.getChildAt(this.mDragPosition);
            if (child.getTop() - 20 <= y && child.getBottom() - 20 >= y) {
                return this.mDragPosition;
            }
            if (y < child.getTop() - 20) {
                index = this.mDragPosition;
                while (index >= 0) {
                    View c = this.mEditParent.getChildAt(index);
                    if (c.getTop() - 20 > y || c.getBottom() - 20 < y) {
                        index--;
                    } else if (c == this.mDragView) {
                        return -1;
                    } else {
                        return index;
                    }
                }
            }
            start = this.mDragPosition + 1;
        }
        index = start;
        while (index < count) {
            child = this.mEditParent.getChildAt(index);
            if (child.getTop() - 20 > y || child.getBottom() - 20 < y) {
                if (index == count - 1 && child.getBottom() + 20 >= y) {
                    if (child == this.mDragView) {
                        return -1;
                    }
                    if (child.getTop() - 20 <= y) {
                        return index + 1;
                    }
                }
                index++;
            } else if (child == this.mDragView) {
                return -1;
            } else {
                return index;
            }
        }
        return -1;
    }

    void updateDrag() {
        int position = findDragPosition(this.mDragY);
        if (this.DEBUG) {
            Log.d("CustomScrollView", "focus: " + position);
        }
        if (position != this.mDragPosition) {
            this.mDragPosition = position;
            int count = this.mEditParent.getChildCount();
            if (this.mDragPosition != -1) {
                int top;
                if (this.mDragPosition == count) {
                    top = (this.mEditParent.getTop() + this.mEditParent.getChildAt(this.mDragPosition - 1).getBottom()) - (this.mOffset / 2);
                } else {
                    top = (this.mEditParent.getTop() + this.mEditParent.getChildAt(this.mDragPosition).getTop()) - (this.mOffset / 2);
                }
//                this.mDragLine.getLayoutParams();
//                MarginLayoutParams mlp = (MarginLayoutParams) this.mDragLine.getLayoutParams();
//                mlp.topMargin = top;
//                this.mDragLine.setLayoutParams(mlp);
//                this.mDragLine.setVisibility(VISIBLE);
                return;
            }
//            this.mDragLine.setVisibility(GONE);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ph = MeasureSpec.getSize(heightMeasureSpec);
        int offset = 0 + (getPaddingTop() + getPaddingBottom());
        View frame = findViewById(R.id.frame_parent);
        ((LinearLayout) frame.findViewById(R.id.parent)).setMinimumHeight(ph - (offset + ((((MarginLayoutParams) frame.getLayoutParams()).topMargin + frame.getPaddingTop()) + frame.getPaddingBottom())));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if ((getContext() instanceof NoteEditActivity) && oldh <= h) {
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        boolean scroll;
        rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
        int delta = computeScrollDeltaToGetChildRectOnScreen(rectangle);
        if (delta != 0) {
            scroll = true;
        } else {
            scroll = false;
        }
        if (delta > 0) {
            delta += 50;
        } else if (delta < 0) {
            delta -= 50;
        }
        if (scroll) {
            if (immediate) {
                scrollBy(0, delta);
            } else {
                smoothScrollBy(0, delta);
            }
        }
        return scroll;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 24 || keyCode == 25) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}
