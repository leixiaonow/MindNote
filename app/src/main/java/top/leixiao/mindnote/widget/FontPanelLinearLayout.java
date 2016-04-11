package top.leixiao.mindnote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import top.leixiao.mindnote.NoteEditActivity;
import top.leixiao.mindnote.R;


public class FontPanelLinearLayout extends LinearLayout {
    private static final int FONT_SCALE = 1;
    private static final int MAX_FONT_SIZE = 24;
    private static final int MIN_FONT_SIZE = 12;
    private SeekBar fontBar;
    private ImageView fontDec;
    private ImageView fontInc;
    private int mFontSize = MIN_FONT_SIZE;

    public FontPanelLinearLayout(Context context) {
        super(context);
    }

    public FontPanelLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FontPanelLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    static /* synthetic */ int access$012(FontPanelLinearLayout x0, int x1) {
        int i = x0.mFontSize + x1;
        x0.mFontSize = i;
        return i;
    }

    static /* synthetic */ int access$020(FontPanelLinearLayout x0, int x1) {
        int i = x0.mFontSize - x1;
        x0.mFontSize = i;
        return i;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.fontBar = (SeekBar) findViewById(R.id.font_bar);
        this.fontBar.setMax(MIN_FONT_SIZE);
        this.fontBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FontPanelLinearLayout.this.mFontSize = (i * FontPanelLinearLayout.FONT_SCALE) + FontPanelLinearLayout.MIN_FONT_SIZE;
                ((NoteEditActivity) FontPanelLinearLayout.this.getContext()).onFontChanged(FontPanelLinearLayout.this.mFontSize);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.fontDec = (ImageView) findViewById(R.id.font_dec);
        this.fontDec.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (FontPanelLinearLayout.this.mFontSize > FontPanelLinearLayout.MIN_FONT_SIZE) {
                    FontPanelLinearLayout.access$020(FontPanelLinearLayout.this, FontPanelLinearLayout.FONT_SCALE);
                    FontPanelLinearLayout.this.fontBar.setProgress((FontPanelLinearLayout.this.mFontSize - 12) / FontPanelLinearLayout.FONT_SCALE);
                    ((NoteEditActivity) FontPanelLinearLayout.this.getContext()).onFontChanged(FontPanelLinearLayout.this.mFontSize);
                }
            }
        });
        this.fontInc = (ImageView) findViewById(R.id.font_inc);
        this.fontInc.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (FontPanelLinearLayout.this.mFontSize < FontPanelLinearLayout.MAX_FONT_SIZE) {
                    FontPanelLinearLayout.access$012(FontPanelLinearLayout.this, FontPanelLinearLayout.FONT_SCALE);
                    FontPanelLinearLayout.this.fontBar.setProgress((FontPanelLinearLayout.this.mFontSize - 12) / FontPanelLinearLayout.FONT_SCALE);
                    ((NoteEditActivity) FontPanelLinearLayout.this.getContext()).onFontChanged(FontPanelLinearLayout.this.mFontSize);
                }
            }
        });
    }

    public void setFontSize(int fontSize) {
        if (fontSize < MIN_FONT_SIZE) {
            fontSize = MIN_FONT_SIZE;
        }
        if (fontSize > MAX_FONT_SIZE) {
            fontSize = MIN_FONT_SIZE;
        }
        this.mFontSize = fontSize;
        this.fontBar.setProgress((this.mFontSize - 12) / FONT_SCALE);
    }
}
