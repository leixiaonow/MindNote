package top.leixiao.mindnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import java.lang.reflect.Field;

public class DrawableBackgroundSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {
    private final int mColor;
    private final Bitmap pic;

    public DrawableBackgroundSpan(Context context, int res) {
        this.pic = BitmapFactory.decodeResource(context.getResources(), res);
        this.mColor = 0;
    }

    public DrawableBackgroundSpan(Context context, int res, int color) {
        this.pic = BitmapFactory.decodeResource(context.getResources(), res);
        this.mColor = color;
    }

    public DrawableBackgroundSpan(Parcel src) {
        this.mColor = src.readInt();
        this.pic = (Bitmap) Bitmap.CREATOR.createFromParcel(src);
    }

    public int getBackgroundColor() {
        return this.mColor;
    }

    public int getSpanTypeId() {
        return 24;
    }

    public int describeContents() {
        return 0;
    }

    public Bitmap getBackground() {
        return this.pic;
    }

    public void updateDrawState(TextPaint ds) {
        try {
            Field field = Class.forName("android.text.TextPaint").getDeclaredField("bgPic");
            field.setAccessible(true);
            field.set(ds, this.pic);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (NoSuchFieldException e4) {
            e4.printStackTrace();
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mColor);
        this.pic.writeToParcel(dest, flags);
    }
}
