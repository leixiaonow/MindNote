package top.leixiao.mindnote;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.view.ViewDebug.ExportedProperty;

import java.lang.reflect.Method;

public class BlurDrawable extends Drawable {
    public static final int DEFAULT_BLUR_COLOR = Color.BLUE;
    public static final Mode DEFAULT_BLUR_COLOR_MODE = Mode.SRC_OVER;
    public static final float DEFAULT_BLUR_LEVEL = 0.9f;
    private static final Method sDrawBlurRectMethod = getDrawBlurRectMethod();
    private boolean mMutated;
    @ExportedProperty(deepExport = true, prefix = "state_")
    private BlurState mState;

    public BlurDrawable() {
        this(null);
    }

    public BlurDrawable(float level) {
        this(null);
        setBlurLevel(level);
    }

    private BlurDrawable(BlurState state) {
        this.mState = new BlurState(state);
        if (state == null) {
            setColorFilter(new PorterDuffColorFilter(DEFAULT_BLUR_COLOR, DEFAULT_BLUR_COLOR_MODE));
        }
    }

    private static Method getDrawBlurRectMethod() {
        try {
            return Canvas.class.getMethod("drawBlurRect", new Class[]{Rect.class, Float.TYPE, Paint.class});
        } catch (Exception e) {
            return null;
        }
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mState.mChangingConfigurations;
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mState = new BlurState(this.mState);
            this.mMutated = true;
        }
        return this;
    }

    public void draw(Canvas canvas) {
        if (sDrawBlurRectMethod != null) {
            try {
                sDrawBlurRectMethod.invoke(canvas, new Object[]{getBounds(), Float.valueOf(this.mState.mLevel), this.mState.mPaint});
                return;
            } catch (Exception e) {
                canvas.drawRect(getBounds(), this.mState.mPaint);
                return;
            }
        }
        canvas.drawRect(getBounds(), this.mState.mPaint);
    }

    public float getBlurLevel() {
        return this.mState.mLevel;
    }

    public void setBlurLevel(float level) {
        if (this.mState.mLevel != level) {
            invalidateSelf();
            this.mState.mLevel = level;
        }
    }

    public int getAlpha() {
        return this.mState.mPaint.getAlpha();
    }

    public void setAlpha(int alpha) {
        if (alpha != this.mState.mPaint.getAlpha()) {
            this.mState.mPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mState.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    public void setXfermode(Xfermode xfermode) {
        this.mState.mPaint.setXfermode(xfermode);
        invalidateSelf();
    }

    public int getOpacity() {
        return -3;
    }

    public ConstantState getConstantState() {
        this.mState.mChangingConfigurations = getChangingConfigurations();
        return this.mState;
    }

    static final class BlurState extends ConstantState {
        @ExportedProperty
        int mChangingConfigurations;
        float mLevel = BlurDrawable.DEFAULT_BLUR_LEVEL;
        Paint mPaint = new Paint();

        BlurState(BlurState state) {
            if (state != null) {
                this.mLevel = state.mLevel;
                this.mPaint = new Paint(state.mPaint);
                this.mChangingConfigurations = state.mChangingConfigurations;
            }
        }

        public Drawable newDrawable() {
            return new BlurDrawable();
        }

        public Drawable newDrawable(Resources res) {
            return new BlurDrawable();
        }

        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }
    }
}
