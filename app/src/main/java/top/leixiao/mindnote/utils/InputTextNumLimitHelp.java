package top.leixiao.mindnote.utils;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;

import top.leixiao.mindnote.widget.TextLengthFilter;

public class InputTextNumLimitHelp {
    private static final int INVALIDATE = -1;
    private static final int TYPE_BOTH = 2;
    private static final int TYPE_CHINESE_CHATACTER = 3;
    private static final int TYPE_ENGLISH_CHARACTER = 1;
    private int mBothCharacterMaxSize;
    private int mChineseCharacterMaxSize;
    private int mEnglishCharacterMaxSize;
    private EditText mInputView;
    private TextChangedListener mTextChangedListener;
    private TextWatcher mTextWatcher = new C03711();

    class C03711 implements TextWatcher {
        C03711() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {
                InputTextNumLimitHelp.this.setInputContentMaxSize(InputTextNumLimitHelp.this.getInputCharactersType(s), String.valueOf(s), count);
            }
        }

        public void afterTextChanged(Editable s) {
            if (InputTextNumLimitHelp.this.mTextChangedListener != null) {
                InputTextNumLimitHelp.this.mTextChangedListener.onTextChange();
            }
        }
    }

    public interface TextChangedListener {
        void onTextChange();
    }

    public InputTextNumLimitHelp(EditText inputView, int englishCharacterMaxSize, int chineseCharacterMaxSize, int bothCharacterMaxSize) {
        this.mEnglishCharacterMaxSize = englishCharacterMaxSize;
        this.mChineseCharacterMaxSize = chineseCharacterMaxSize;
        this.mBothCharacterMaxSize = bothCharacterMaxSize;
        this.mInputView = inputView;
        inputView.addTextChangedListener(this.mTextWatcher);
    }

    public void unRegisterWatcher() {
        this.mInputView.removeTextChangedListener(this.mTextWatcher);
        this.mInputView = null;
        this.mTextChangedListener = null;
    }

    public void setTextChangedListener(TextChangedListener listener) {
        this.mTextChangedListener = listener;
    }

    private int getInputCharactersType(CharSequence input) {
        CharSequence text = input;
        boolean isSameType = true;
        int i = 0;
        int len = text.length();
        while (i < len + INVALIDATE) {
            try {
                if (String.valueOf(text.charAt(i)).getBytes(StringUtils.ENCODING_UTF8).length != String.valueOf(text.charAt(i + TYPE_ENGLISH_CHARACTER)).getBytes(StringUtils.ENCODING_UTF8).length) {
                    isSameType = false;
                    break;
                }
                i += TYPE_ENGLISH_CHARACTER;
            } catch (UnsupportedEncodingException e) {
            }
        }
        if (isSameType) {
            try {
                return String.valueOf(text.charAt(0)).getBytes(StringUtils.ENCODING_UTF8).length;
            } catch (UnsupportedEncodingException e2) {
            }
        }
        return TYPE_BOTH;
    }

    private void setInputContentMaxSize(int inputCharacterType, String inputText, int count) {
        int maxInputSize = getInputContentMaxSize(inputCharacterType, inputText, count);
        if (INVALIDATE != maxInputSize) {
            if (maxInputSize < inputText.length()) {
                this.mInputView.setText(inputText.substring(0, maxInputSize + INVALIDATE));
            }
            EditText editText = this.mInputView;
            InputFilter[] inputFilterArr = new InputFilter[TYPE_ENGLISH_CHARACTER];
            inputFilterArr[0] = new TextLengthFilter(maxInputSize);
            editText.setFilters(inputFilterArr);
        }
    }

    private int getInputContentMaxSize(int inputCharacterType, String inputText, int count) {
        if (TYPE_ENGLISH_CHARACTER == inputCharacterType) {
            return getEnglishOrChineseMaxInputSize(inputText, count, this.mEnglishCharacterMaxSize);
        }
        if (TYPE_CHINESE_CHATACTER == inputCharacterType) {
            return getEnglishOrChineseMaxInputSize(inputText, count, this.mChineseCharacterMaxSize);
        }
        return getBothMaxInputSize(inputText, this.mBothCharacterMaxSize);
    }

    private int getEnglishOrChineseMaxInputSize(String inputText, int count, int maxSize) {
        if (count == TYPE_ENGLISH_CHARACTER) {
            return maxSize;
        }
        if (inputText.length() >= maxSize) {
            return maxSize;
        }
        return INVALIDATE;
    }

    private int getBothMaxInputSize(String inputText, int maxSize) {
        try {
            if (inputText.getBytes(StringUtils.ENCODING_UTF8).length >= maxSize) {
                return inputText.length();
            }
            return maxSize;
        } catch (UnsupportedEncodingException e) {
            return INVALIDATE;
        }
    }
}
