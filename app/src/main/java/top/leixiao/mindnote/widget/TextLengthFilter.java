package top.leixiao.mindnote.widget;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

import top.leixiao.mindnote.NoteAppImpl;
import top.leixiao.mindnote.R;
import uk.co.senab.photoview.BuildConfig;

public class TextLengthFilter implements InputFilter {
    private int mMaxLength;

    public TextLengthFilter(int maxLength) {
        this.mMaxLength = maxLength;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int keep = this.mMaxLength - (dest.length() - (dend - dstart));
        if (keep <= 0) {
            Toast.makeText(NoteAppImpl.getContext(), NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit), Toast.LENGTH_SHORT).show();
            return BuildConfig.VERSION_NAME;
        } else if (keep >= end - start) {
            return null;
        } else {
            keep += start;
            if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                keep--;
                if (keep == start) {
                    Toast.makeText(NoteAppImpl.getContext(), NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit), Toast.LENGTH_SHORT).show();
                    return BuildConfig.VERSION_NAME;
                }
            }
            Toast.makeText(NoteAppImpl.getContext(), NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit), Toast.LENGTH_SHORT).show();
            return source.subSequence(start, keep);
        }
    }
}
