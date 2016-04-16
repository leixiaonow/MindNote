package top.leixiao.mindnote.mainWidget;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckBox;

import top.leixiao.mindnote.changecolors.ChameleonColorManager;


public class AmigoCheckBox extends CheckBox {
    private static final int STATE_CHECKED = 1;
    private static final int STATE_DEFAULT = 0;
    private static final String TAG = "AmigoCheckBox";
    private Drawable mButtonDrawable;
    private int mState;

    public AmigoCheckBox(Context context) {
        this(context, null);
    }

    public AmigoCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 16842860);
    }

    public AmigoCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, STATE_DEFAULT);
    }

    public AmigoCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        this.mState = -1;
    }

    protected void drawableStateChanged() {
        if (ChameleonColorManager.isNeedChangeColor() && this.mButtonDrawable != null) {
            changeButtonDrawable();
        }
        super.drawableStateChanged();
    }

    private void changeButtonDrawable() {
        if (stateIsChecked(getDrawableState())) {
            if (this.mState != STATE_CHECKED) {
                this.mButtonDrawable.setColorFilter(ChameleonColorManager.getAccentColor_G1(), Mode.SRC_IN);
                this.mState = STATE_CHECKED;
            }
        } else if (this.mState != 0) {
            this.mButtonDrawable.setColorFilter(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2(), Mode.SRC_IN);
            this.mState = STATE_DEFAULT;
        }
    }

    private boolean stateIsChecked(int[] myDrawableState) {
        for (int index = STATE_DEFAULT; index < myDrawableState.length; index += STATE_CHECKED) {
            if (myDrawableState[index] == 0x10100a0) {
                return true;
            }
        }
        return false;
    }
}
