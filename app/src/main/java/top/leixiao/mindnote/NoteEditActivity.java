package top.leixiao.mindnote;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v7.appcompat.BuildConfig;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import top.leixiao.mindnote.database.NoteData;
import top.leixiao.mindnote.database.NoteItem;
import top.leixiao.mindnote.database.NoteItemImage;
import top.leixiao.mindnote.database.NoteItemRecord;
import top.leixiao.mindnote.database.NoteItemText;
import top.leixiao.mindnote.database.NotePaper.Notes;
import top.leixiao.mindnote.database.TagData;
import top.leixiao.mindnote.database.TempFileProvider;
import top.leixiao.mindnote.label.LabelCustomActivity;
import top.leixiao.mindnote.utils.Constants;
import top.leixiao.mindnote.utils.EnvironmentUtils;
import top.leixiao.mindnote.utils.HanziToPinyin;
import top.leixiao.mindnote.utils.ImageUtil;
import top.leixiao.mindnote.utils.InputMethodManagerUtils;
import top.leixiao.mindnote.utils.NoteUtil;
import top.leixiao.mindnote.utils.ReflectUtils;
import top.leixiao.mindnote.utils.ScrollViewUtils;
import top.leixiao.mindnote.widget.CheckImageView;
import top.leixiao.mindnote.widget.DeleteImageView;
import top.leixiao.mindnote.widget.EditTextCloud;
import top.leixiao.mindnote.widget.FontPanelLinearLayout;
import top.leixiao.mindnote.widget.HorizontalBackgoundView;
import top.leixiao.mindnote.widget.ListDragLocalState;
import top.leixiao.mindnote.widget.NoteEditText;
import top.leixiao.mindnote.widget.PopupPaperWindow;
import top.leixiao.mindnote.widget.PopupPaperWindow.OnPopupStateChangeListener;
import top.leixiao.mindnote.widget.RecordLinearLayout;
import top.leixiao.mindnote.widget.RecordingLayout;
import top.leixiao.mindnote.widget.RichFrameLayout;
import top.leixiao.mindnote.widget.ScaleImageView;

//import top.leixiao.mindnote.database.NotePaper.NoteFiles;

public class NoteEditActivity extends RecordActivityBase implements OnClickListener{
    public static final int MAX_WORDS = 20000;
    public static final int MSG_SHARE_START = 0x0;
    private static final int CHANGE_CONTENT = 0x10;
    private static final int CHANGE_DESKTOP = 0x4;
    private static final int CHANGE_ENCRYPT = 0x8;
    private static final int CHANGE_FONT_COLOR = 0x1000;
    private static final int CHANGE_FONT_SIZE = 0x10000;
    private static final int CHANGE_PAPER = 0x100;
    private static final int CHANGE_TAG = 0x100000;
    private static final int CHANGE_TITLE = 0x1;
    private static final int CHANGE_TOP = 0x2;
    private static final int REQUEST_CODE_EXPORT_TO_PIC = 1;
    private static final int REQUEST_CODE_EXPORT_TO_TEXT = 2;
    private static final int REQUEST_CODE_LOGIN = 4;//是否登录？
    private static final int REQUEST_CODE_PICK = 0;
    private static final int REQUEST_CODE_SHARING = 3;
    private static final int REQUEST_CODE_VERIFY = 5;
    private static final String TAG = "NoteEditActivity";
    private static final int IMAGE_WIDTH = 800;
    private static final int REQUEST_CODE_PICK_CAPTURE = 6;
    private static String sLastExportPicDirPath = null;
    private static String sLastExportTextDirPath = null;
    private static String sLastInsertDirPath = null;
    final int MAX_LIST_COUNT = 100;
    final long SPACE_LIMIT = 0x200000;

    public int mChanged = 0;//标识改变了那些
    public NoteData mEditNote;//正在编辑页面显示的笔记
    public LinearLayout mEditParent;//所有笔记元素的父容器
    public String mFirstImg;//第一张图片的名称,是唯一的
    public String mFirstRecord;//第一个录音的名称，是唯一的
    public TextView mFirstTextView; //第一个TextView
    public NoteEditText mFocusNoteEditText;//当前获得焦点的NoteEditText文字元素
    public int mGreyColor;//灰色值
    public Object mIMEListener;//？？？
    public boolean mInitOK = false;//初始化是否完成？？？
    public boolean mIsCapture;//不知道？？
    public boolean mIsSoftInuptShow = false;//软键盘是否打开
    public boolean mSoftInputShown = false;//如键盘是否显示的标识
    public long mLaunchTime;//启动时间
    public ListPopupWindow mPopup;
    public int mPosition; //下一个插入元素的位置
    public ProgressDialog mProgressDialog;  //进度窗口
    public int mRecordHorizontalMargin;//录音水平和竖直间隔
    public int mRecordVerticalMargin;
    public RecordingLayout mRecordingLayoutView;//录音布局
    public ScrollView mScrollView; //滚动器，最外层控件
    public Intent mShareIntent; //分享时传递的Intent
    public TextView mTailView; //尾巴textVeiw，用来显示时间
    public TextView mSignature; //尾巴签名，用来显示签名
    public int mTextColor;//文本颜色

    //CheckImageView 的监听事件
    public OnClickListener mCheckClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v instanceof CheckImageView) {
                NoteEditText text = (NoteEditText) ((ViewGroup) v.getParent()).findViewById(R.id.text);
                CheckImageView check = (CheckImageView) v;
                switch (check.getImageType()) {
                    case NoteEditActivity.REQUEST_CODE_EXPORT_TO_PIC /*1*/:
                        check.setImageType(NoteEditActivity.REQUEST_CODE_EXPORT_TO_TEXT);
                        NoteEditActivity.this.setEditStrikeThrough(text, true);
                        break;
                    case NoteEditActivity.REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                        check.setImageType(NoteEditActivity.REQUEST_CODE_EXPORT_TO_PIC);
                        NoteEditActivity.this.setEditStrikeThrough(text, false);
                        break;
                }
                NoteEditActivity noteEditActivity = NoteEditActivity.this;
                noteEditActivity.mChanged |= NoteEditActivity.CHANGE_CONTENT;
            }
        }
    };


    public EditTextCloud mTitleView;//标题栏
    public int mType;//显示类型，导出图片或文字或编辑类型
    public RichFrameLayout mViewImageItem;//笔记的图片元素
    public int mWidth;//宽度？？界面宽度的宽度？
    public  int mCount = 0;

    //删除 点击 的监听 deleteImageView调用？？？
    public OnClickListener mDeleteClickListener = new OnClickListener() {
        public void onClick(View v) {
            View parentView = (View) v.getParent();
            if (parentView != null) {
                NoteEditText neText;
                View before;
                NoteEditText ntText;
                ViewGroup pparentView = (ViewGroup) parentView.getParent();//edit_parent
                int curLine;
                View after = null;
                int count = NoteEditActivity.this.mEditParent.getChildCount();
                //遍历mEditParent
                int index = getChildPosition(parentView);
                //得到其位置
                curLine = index;
                //如果不是最后一个元素
                if (index + 1 < count) {
                    //得到下一个元素
                    View next = NoteEditActivity.this.mEditParent.getChildAt(index + 1);
                    if (NoteUtil.JSON_TEXT.equals(next.getTag())) {
                        after = next;
                        Log.d(TAG, "onClick: 得到下一个元素");
                    }
                }
                //有下一个元素，并且是文本元素时
                if (after != null) {
                    Log.d(TAG, "onClick: after!=null");
                    pparentView.removeView(parentView);
                    neText = (NoteEditText) after.findViewById(R.id.text);
                    neText.requestFocus();
                    if (((CheckImageView) after.findViewById(R.id.check)).getImageType() != 0) {
                        NoteEditActivity.this.mergeCommonText(after);
                        Log.d(TAG, "onClick: imageType!=0");
                    } else {
                        Log.d(TAG, "onClick: imageType==0");
                        Selection.setSelection(neText.getText(), 0);
                        NoteEditActivity.this.showSoftInput(neText);
                    }
                } else {//下一个元素为null时，即没有下一个元素时，或下一个元素不是文本元素时
                    //得到前一个元素
                    Log.d(TAG, "onClick: after==null");
                    before = NoteEditActivity.this.mEditParent.getChildAt(curLine - 1);
                    //如果前一个元素不为空，且是文本元素
                    if (before != null && NoteUtil.JSON_TEXT.equals(before.getTag())) {
                        pparentView.removeView(parentView);
                        neText = (NoteEditText) before.findViewById(R.id.text);
                        neText.requestFocus();
                        Selection.setSelection(neText.getText(), neText.getText().length());
                        NoteEditActivity.this.showSoftInput(neText);
                    } else {//如果前一个元素为空，或不是文本元素
                        ntText = (NoteEditText) parentView.findViewById(R.id.text);
                        if (ntText.getTextSize() > 0.0f) {
                            ntText.setText(null);
                        }
                        if (((CheckImageView) parentView.findViewById(R.id.check)).getImageType() != 0) {
                            NoteEditActivity.this.onListMenuClick();
                        }
                    }
                }
                NoteEditActivity.this.setTextChanged();
                NoteEditActivity.this.setFirstHint();
            }
        }
    };


    private ArrayList<NoteItem> mDataList = new ArrayList<>();//存储NoteItem的列表，文字，图片，和声音
    private HashSet<String> mDeleteFilesList;//删除文件列表
    private int mFocusId = -2;//当前光标焦点？？？
    private Handler mHandler = new Handler();//多线程

    //界面更新Handler
    public Handler mUiHandler = new  Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NoteEditActivity.REQUEST_CODE_PICK /*0*/:
                    final int type = msg.arg1;
                    if (type == NoteEditActivity.REQUEST_CODE_EXPORT_TO_PIC) {
//                        MobEventUtil.onSendMobEvent(NoteEditActivity.this, "original_share", null);
                        NoteEditActivity.this.onShareMenuAction(type);
                        NoteEditActivity.this.startActivity(Intent.createChooser(NoteEditActivity.this.mShareIntent, NoteEditActivity.this.getString(R.string.share)));
                        NoteEditActivity.this.mShareIntent = null;
                        return;
                    } else if (NoteEditActivity.this.checkSdcardOK()) {
                        NoteEditActivity.this.popupProgressDialog(R.string.create_sharing);
                        new Thread(new Runnable() {
                            public void run() {
                                NoteEditActivity.this.onShareMenuAction(type);
                                NoteEditActivity.this.mUiHandler.sendMessageAtTime(NoteEditActivity.this.mHandler.obtainMessage(NoteEditActivity.REQUEST_CODE_EXPORT_TO_PIC), 0);
                            }
                        }).start();
                        return;
                    } else {
                        return;
                    }
                case NoteEditActivity.REQUEST_CODE_EXPORT_TO_PIC /*1*/:
                    NoteEditActivity.this.dismissProgressDialog();
                    try {
                        NoteEditActivity.this.startActivity(Intent.createChooser(NoteEditActivity.this.mShareIntent, NoteEditActivity.this.getString(R.string.share)));
                    } catch (ActivityNotFoundException e) {
                        Log.e(NoteEditActivity.TAG, "ActivityNotFoundException: " + e);
                    }
                    NoteEditActivity.this.mShareIntent = null;
                    return;
                case NoteEditActivity.REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                    NoteEditActivity.this.dismissProgressDialog();
                    Toast.makeText(NoteEditActivity.this, R.string.finish_export, Toast.LENGTH_SHORT).show();
                    return;
                default:
                    return;
            }
        }
    };

    private MenuItem mMenuDelete;//删除
    private MenuItem mMenuDesktop;//显示到桌面
    private MenuItem mMenuExport;//导出
    private MenuItem mMenuExportPic;//导出为图片
    private MenuItem mMenuList;//？？
    private MenuItem mMenuMore;//更多？
    private MenuItem mMenuPaper;//背景纸
    private MenuItem mMenuPhoto;//照片
    private MenuItem mMenuRecord;//录音
    private MenuItem mMenuShare;//分享
    private MenuItem mMenuTop;//置顶

    //键盘监听
    public OnKeyListener mEditKeyPreListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == 0 && keyCode == 66) {
                return NoteEditActivity.this.onKeyEnter();
            }
            if (event.getAction() == 0 && keyCode == 67) {
                return NoteEditActivity.this.onKeyDel();
            }
            return false;
        }
    };


    //什么监听？？根据实验，如果记事本既没有标题，又没有内容，或只有一个文字元素，且为空时
    //按返回键后，直接退出编辑界面，返回笔记列表界面，且不保存，或将已有笔记删除
    private EditTextCloud.OnKeyPreImeListener mOnKeyPreImeListener = new EditTextCloud.OnKeyPreImeListener() {
        public boolean onKeyPreIme(View view, int keyCode, KeyEvent event) {
            if (keyCode == 4 && event.getAction() == 1 && TextUtils.isEmpty(NoteEditActivity.this.mTitleView.getText())) {
                if (NoteEditActivity.this.mEditParent.getChildCount() > 1) {
                    return false;
                }
                View child = NoteEditActivity.this.mEditParent.getChildAt(0);
                if (NoteUtil.JSON_TEXT.equals(child.getTag()) && TextUtils.isEmpty(((NoteEditText) child.findViewById(R.id.text)).getText())) {
                    NoteEditActivity.this.onBackPressed();
                    return true;
                }
            }
            return false;
        }
    };

    private int mRequestCode = -1; //请求码
    private BroadcastReceiver mScreenOffAndHomeReceiver = null; //广播接收器
    private int mSelectStart = -1;//选择开始处
    private boolean mNewFlag=false;
    //时间改变广播接收器

    private BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.TIME_SET".equals(intent.getAction())) {
                String createTime = NoteEditActivity.this.getResources().getString(R.string.create_time) + HanziToPinyin.Token.SEPARATOR + NoteUtil.getDate(NoteEditActivity.this, NoteEditActivity.this.mEditNote.mCreateTime);
                String modifyTime = NoteEditActivity.this.getResources().getString(R.string.last_modified) + HanziToPinyin.Token.SEPARATOR + NoteUtil.getDate(NoteEditActivity.this, NoteEditActivity.this.mEditNote.mModifyTime);

                //根据当前的目的设置创建于，或更新于
                switch (NoteEditActivity.this.mType) {
                    case NoteUtil.EDIT_TYPE_UPDATE /*-5*/:
                        NoteEditActivity.this.mTailView.setText(modifyTime);
                        return;
                    case NoteUtil.EDIT_TYPE_CAMERA /*-4*/:
                    case NoteUtil.EDIT_TYPE_RECORD /*-3*/:
                    case NoteUtil.EDIT_TYPE_LIST /*-2*/:
                    case NoteUtil.EDIT_TYPE_NORMAL /*-1*/:
                        NoteEditActivity.this.mTailView.setText(createTime);
                        return;
                    default:
                        return;
                }
            }
        }
    };

    //电话状态监听器
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case 2 /*2*/:
                    if (NoteEditActivity.this.mRecordingLayoutView != null) {
                        NoteEditActivity.this.mRecordingLayoutView.pause();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    private TelephonyManager telephonyManager;//电话管理器
    private Toolbar edit_toolbar;
    private View mSelectBill;
    private View mSelectCamera;
    private View mSelectGallery;
    private View mSelectLabel;
    private View mSelectRecord;
    private View mSelectReminder;
    private LinearLayout mlastTimeView;

    private ArrayList<Integer> mSelectLabelIds = new ArrayList<>();
    private ArrayList<Integer> mLabel=new ArrayList<>();
    private ViewGroup mLabelContent;
    private LinearLayout mLabelView;


    //入口
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);//加载布局
        initData();
        initlView();
        initListener();
        initEditLayout();//加载显示的笔记内容
        initTitle();
        showLabel();
        this.telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        this.telephonyManager.listen(this.phoneStateListener, 32);
        registerReceiver(this.mTimeChangedReceiver, new IntentFilter("android.intent.action.TIME_SET"));

//        初始化数据和界面后，剩下的主要是对事件的相应
//        1、OpentionMenu
//        2、功能按钮

    }

    private void initlView() {

        this.mLabelView = (LinearLayout) findViewById(R.id.new_note_label);
        //功能按键
        this.mLabelContent = (ViewGroup) this.mLabelView.findViewById(R.id.new_note_label_content);
        this.mSelectBill = findViewById(R.id.action_bill);
        this.mSelectLabel = findViewById(R.id.action_label);
        this.mSelectRecord = findViewById(R.id.action_recorde);
        this.mSelectReminder = findViewById(R.id.action_reminder);
        this.mSelectCamera = findViewById(R.id.action_camera);
        this.mSelectGallery = findViewById(R.id.action_gallery);
        tintImageViewDrawable(R.id.action_label, R.mipmap.action_label_icon, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_bill, R.mipmap.action_bill_icon, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_reminder, R.mipmap.action_alert_icon, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_recorde, R.mipmap.attachment_sound_recorder, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_gallery, R.mipmap.attachment_select_image, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_camera, R.mipmap.attachment_take_photos, R.color.action_bar_image_color);

        //toolbar
        edit_toolbar = (Toolbar) findViewById(R.id.edit_toolbar);//加载toolbar
        setSupportActionBar(edit_toolbar);

        //滑动框中View
        this.mTitleView = (EditTextCloud) findViewById(R.id.title);
        this.mScrollView = (ScrollView) findViewById(R.id.scroll_view);//得到最外层mScrollView
        ScrollViewUtils.setDelayTopOverScrollEnabled(this.mScrollView, true);
        LinearLayout contentParent = (LinearLayout) this.mScrollView.findViewById(R.id.parent);
        this.mEditParent = (LinearLayout) contentParent.findViewById(R.id.edit_parent);
        this.mlastTimeView = (LinearLayout) contentParent.findViewById(R.id.last_parent);
        this.mTailView = (TextView) mlastTimeView.findViewById(R.id.last_modify);//得到最后修改时间TextView
        this.mSignature=(TextView) mlastTimeView.findViewById(R.id.signature);

        //生成创建和修改时间字符串
        String createTime = getResources().getString(R.string.create_time) + HanziToPinyin.Token.SEPARATOR + NoteUtil.getDate(this, this.mEditNote.mCreateTime);
        String modifyTime = getResources().getString(R.string.last_modified) + HanziToPinyin.Token.SEPARATOR + NoteUtil.getDate(this, this.mEditNote.mModifyTime);
        switch (this.mType) {
            case NoteUtil.EDIT_TYPE_UPDATE /*-5*/:
                this.mTailView.setText(modifyTime);
                getWindow().setSoftInputMode(18);
                //下面不懂
                this.mScrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                break;
            case NoteUtil.EDIT_TYPE_CAMERA /*-4*/:
            case NoteUtil.EDIT_TYPE_RECORD /*-3*/:
                this.mTailView.setText(createTime);
                getWindow().setSoftInputMode(18);
                int childCount = this.mEditParent.getChildCount();
                if (childCount > 0) {
                    View last = this.mEditParent.getChildAt(childCount - 1);
                    if (NoteUtil.JSON_TEXT.equals(last.getTag())) {
                        last.findViewById(R.id.text).requestFocus();
                    }
                }
                this.mSoftInputShown = true;
                break;
            case NoteUtil.EDIT_TYPE_LIST /*-2*/:
            case NoteUtil.EDIT_TYPE_NORMAL /*-1*/:
                this.mTailView.setText(createTime);
                getWindow().setSoftInputMode(21);
                this.mSoftInputShown = true;
                onFocusToEdit();
                break;
        }

    }

    private void initListener() {
        this.mSelectBill.setOnClickListener(this);
        this.mSelectLabel.setOnClickListener(this);
        this.mSelectRecord.setOnClickListener(this);
        this.mSelectReminder.setOnClickListener(this);
        this.mSelectCamera.setOnClickListener(this);
        this.mSelectGallery.setOnClickListener(this);

        this.mScrollView.findViewById(R.id.empty).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Point pt = new Point();
                ReflectUtils.getLastTouchPoint(v, pt);
                Rect r = new Rect();
                NoteEditActivity.this.mEditParent.getGlobalVisibleRect(r);
                if (pt.y >= r.bottom) {
                    NoteEditActivity.this.onFocusToEdit();
                }
            }
        });

        mlastTimeView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NoteEditActivity.this.onFocusToEdit();
            }
        });

        this.mTitleView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                NoteEditActivity.this.setTitleChanged();
            }
        });

        this.mTitleView.setOnKeyPreImeListener(getKeyPreImeListener());
        this.mTitleView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (NoteEditActivity.this.mInitOK) {

                }
            }
        });


    }

    void setTopBlurEffect() {
        int color = Color.GREEN | (ViewCompat.MEASURED_SIZE_MASK & NoteUtil.getBackgroundColor(this.mEditNote != null ? this.mEditNote.mPaper : REQUEST_CODE_PICK));
        BlurDrawable bd = new BlurDrawable();
        bd.setColorFilter(color, BlurDrawable.DEFAULT_BLUR_COLOR_MODE);
        ColorDrawable cd = new ColorDrawable(Color.GREEN);
        Drawable[] drawableArr = new Drawable[REQUEST_CODE_EXPORT_TO_TEXT];
        drawableArr[REQUEST_CODE_PICK] = bd;
        drawableArr[REQUEST_CODE_EXPORT_TO_PIC] = cd;
        LayerDrawable ld = new LayerDrawable(drawableArr);
        ld.setLayerInset(REQUEST_CODE_EXPORT_TO_PIC, 52, getResources().getDimensionPixelSize(R.dimen.system_bar_top_height) - 3, 52, REQUEST_CODE_PICK);
//        actionBar.setBackgroundDrawable(ld);
    }

    void setBottomBlurEffect(boolean useWhite) {
        ActionBar actionBar = getActionBar();
        if (useWhite || this.mEditNote == null || this.mEditNote.mPaper == 0) {
            BlurDrawable bd = new BlurDrawable();
            bd.setColorFilter(BlurDrawable.DEFAULT_BLUR_COLOR, BlurDrawable.DEFAULT_BLUR_COLOR_MODE);
            ColorDrawable cd = new ColorDrawable(855638016);
            Drawable[] drawableArr = new Drawable[REQUEST_CODE_EXPORT_TO_TEXT];
            drawableArr[REQUEST_CODE_PICK] = bd;
            drawableArr[REQUEST_CODE_EXPORT_TO_PIC] = cd;
            LayerDrawable ld = new LayerDrawable(drawableArr);
            ld.setLayerInset(REQUEST_CODE_EXPORT_TO_PIC, REQUEST_CODE_PICK, REQUEST_CODE_PICK, REQUEST_CODE_PICK, getResources().getDimensionPixelSize(R.dimen.mz_action_button_min_height) - 1);
//            actionBar.setSplitBackgroundDrawable(ld);
            return;
        }
        int color = 0xe6000000 | (ViewCompat.MEASURED_SIZE_MASK & NoteUtil.getBackgroundColor(this.mEditNote.mPaper));
        BlurDrawable bd = new BlurDrawable();
        bd.setColorFilter(color, BlurDrawable.DEFAULT_BLUR_COLOR_MODE);
        ColorDrawable cd = new ColorDrawable(0xa000000);
        ColorDrawable cd2 = new ColorDrawable(0x33000000);
        Drawable[] drawableArr2 = new Drawable[REQUEST_CODE_SHARING];
        drawableArr2[REQUEST_CODE_PICK] = bd;
        drawableArr2[REQUEST_CODE_EXPORT_TO_PIC] = cd;
        drawableArr2[REQUEST_CODE_EXPORT_TO_TEXT] = cd2;
        LayerDrawable ld = new LayerDrawable(drawableArr2);
        ld.setLayerInset(REQUEST_CODE_EXPORT_TO_TEXT, REQUEST_CODE_PICK, REQUEST_CODE_PICK, REQUEST_CODE_PICK, getResources().getDimensionPixelSize(R.dimen.mz_action_button_min_height) - 1);
//        actionBar.setSplitBackgroundDrawable(ld);
    }

    void setActionBarOverLayColor() {
        setTopBlurEffect();
        setBottomBlurEffect(false);
    }

    //不懂？？？
    boolean matchType(Uri uri, String mimetype) {
        if (uri == null || mimetype == null) {
            return false;
        }
        String scheme = uri.getScheme();
        String type;
        if ("file".equals(scheme)) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath());
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                if (type != null && type.startsWith(mimetype)) {
                    return true;
                }
            }
        } else if ("content".equals(scheme)) {
            type = getContentResolver().getType(uri);
            if (type != null && type.startsWith(mimetype)) {
                return true;
            }
        }
        return false;
    }

    //检查sdcard空间，并创建文件夹
    boolean checkSdcardOK() {
        if (ImageUtil.checkSdcardAvailableSpace(2097152)) {
            File pDataDir = new File(NoteUtil.FILES_ANDROID_DATA);
            if (pDataDir != null && pDataDir.exists()) {
                return true;
            }
            showErrorTip(-4);
            Log.d(TAG, "Android data dir not exist.");
            return false;
        }
        showErrorTip(-1);
        return false;
    }

    //初始化一些View，好几把长
    public void initData() {
        Intent intent = getIntent();
        this.mPosition = intent.getIntExtra("pos", -1);//得到传入的位置
        long id = intent.getLongExtra("id", -1);//得到传入的id，笔记的id
        Uri noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, id);//得到笔记的唯一路径，ContentProvider需要
        this.mType = intent.getIntExtra(Constants.JSON_KEY_TYPE, -1);//得到传入的mtype
        this.mFocusId = intent.getIntExtra("focus", -2);//得到传入的mFocusId，光标位置？？
        this.mSelectStart = intent.getIntExtra("select", -1);//得到传入的mSelectStart
        this.mNewFlag = intent.getBooleanExtra("creating", false);//得到传入的mNewFlag，新建笔记标签
        long tag = intent.getLongExtra("tag", -1);//得到标识tag
        //如果传入的id<=-1，新建笔记
        if (id <= -1) {
            if (this.mEditNote == null) {
                //mmEditNote为空，新建笔记，并为其生成和设置唯一标识符mUUId
                this.mEditNote = new NoteData();//新建笔记
                this.mEditNote.mUUId = generateUUId();//设置uuid
            }
            this.mPosition = -1; //设置位置mPosition
            this.mEditNote.mId = -1;//设置mEditNote.mId
            this.mEditNote.mTag = tag;//设置tag
            this.mEditNote.mTopTime = 0; //设置置顶时间为0
            this.mEditNote.mCreateTime = System.currentTimeMillis();//设置创建时间
            this.mNewFlag = true;//新建笔记设为true
        } else {
            //不是新建笔记的情况
            //从数据库读出笔记
            Cursor cursor = getContentResolver().query(noteUri, NoteData.NOTES_PROJECTION, null, null, Notes.DEFAULT_SORT_ORDER);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                Toast.makeText(this, R.string.note_not_exist, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            cursor.moveToFirst();//cursor光标移到开头
            this.mEditNote = NoteData.getItem(cursor);//解析cursor内容
            cursor.close();
        }

        if (this.mEditNote == null) {
            this.mEditNote = new NoteData();
            Log.d(toString(), "mEditNote is null ");
        }

        this.mFirstImg = this.mEditNote.mFirstImg;//设置mFirstImg
        this.mFirstRecord = this.mEditNote.mFirstRecord;//设置mFirstRecord

//        参数
        this.mTextColor = getResources().getColor(R.color.common_font_color);//设置文字颜色
        this.mGreyColor = getResources().getColor(R.color.common_grey_color);//设置灰色
        //水平边距
        this.mRecordHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.edit_recording_horizontal_margin);
        //数值边距
        this.mRecordVerticalMargin = getResources().getDimensionPixelOffset(R.dimen.edit_recording_bottom_margin);
        DisplayMetrics dm = new DisplayMetrics(); //得到显示宽度？？？
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.mWidth = dm.widthPixels - 200;//设置图片，或录音控件宽度
        this.mInitOK = true;

    }

    private void dismissPopupIfShown() {
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
        this.mPopup = null;
    }

    //不就是设置提示吧
    void setFirstHint() {
        TextView first = null;
        int childCount = this.mEditParent.getChildCount();
        //找到第一个NoteEditText
        for (int index = 0; index < childCount; index += 1) {
            View last = this.mEditParent.getChildAt(index);
            if (NoteUtil.JSON_TEXT.equals(last.getTag())) {
                first = (NoteEditText) last.findViewById(R.id.text);
                break;
            }
        }
        //如果第一个NoteEditText不是mFirstTextView
        if (first != this.mFirstTextView) {
            //并且不为空，说明在最前面加了新的NoteEditText
            if (this.mFirstTextView != null) {
                this.mFirstTextView.setHint(null);
            }
            this.mFirstTextView = first;
            if (this.mFirstTextView != null) {
                this.mFirstTextView.setHint(R.string.edit_hint);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    //查询taglist
/*    void queryTagList() {
        this.mTagList.clear();
        this.mTagList.add(new TagInfo(-1, getString(R.string.all_tag)));
        Cursor c = getContentResolver().query(NoteCategory.CONTENT_URI, TagData.TAGS_LIST, NoteCategory.DELETE + "<> 1", null, NoteCategory.DEFAULT_SORT_ORDER);
        if (c != null) {
            int cID = c.getColumnIndex(NoteFiles.DEFAULT_SORT_ORDER);
            int cName = c.getColumnIndex(NoteUtil.JSON_FILE_NAME);
            while (c.moveToNext()) {
                this.mTagList.add(new TagInfo(c.getLong(cID), c.getString(cName)));
            }
            c.close();
        }
        if (TagData.FUN_ENCRYPT) {
            this.mTagList.add(new TagInfo(-2, getString(R.string.group_encrypt)));
        }
        this.mTagList.add(new TagInfo(-3, getString(R.string.new_tag)));
    }*/

    @Override
    public void finish() {
        super.finish();
    }

    //根据mFocusId来设置光标位置，和设置setSelection
    void onFocusToEdit() {
        NoteEditText edit;
        if (this.mFocusId < -1) {
            int childCount = this.mEditParent.getChildCount();
            if (childCount > 0) {
                View last = this.mEditParent.getChildAt(childCount - 1);
                if (NoteUtil.JSON_TEXT.equals(last.getTag())) {
                    edit = (NoteEditText) last.findViewById(R.id.text);
                    edit.requestFocus();
                    Selection.setSelection(edit.getText(), edit.getText().length());
                    showSoftInput(edit);
                }
            }
        } else if (this.mFocusId == -1) {
            this.mTitleView.requestFocus();
            this.mTitleView.setSelection(this.mSelectStart);
            showSoftInput(this.mTitleView);
        } else {
            View focuView = this.mEditParent.getChildAt(this.mFocusId);
            if (NoteUtil.JSON_TEXT.equals((String) focuView.getTag())) {
                edit = (NoteEditText) focuView.findViewById(R.id.text);
                edit.requestFocus();
                if (this.mSelectStart != -1) {
                    edit.setSelection(this.mSelectStart);
                }
                showSoftInput(edit);
            } else if (focuView instanceof RichFrameLayout) {
                ((RichFrameLayout) focuView).onFocus();
            }
        }
    }

    //设置titleView和titleToolBar
    public void initTitle() {

        //为mTitleView设置标题
        this.mTitleView.setText(this.mEditNote.mTitle);
        this.mTitleView.clearFocus();

    }

    //着当前FocusEditView
    public View findFocusEditView() {
        if (this.mFocusNoteEditText != null && this.mFocusNoteEditText.hasFocus()) {
            return this.mFocusNoteEditText;
        }
        this.mFocusNoteEditText = null;
        View view = this.mScrollView.findFocus();
        if (view == null || !(view instanceof NoteEditText)) {
            return null;
        }
        this.mFocusNoteEditText = (NoteEditText) view;
        return this.mFocusNoteEditText;
    }

    //找当前FocusView
    public View findFocusView() {
        if (this.mFocusNoteEditText != null && this.mFocusNoteEditText.hasFocus()) {
            return this.mFocusNoteEditText;
        }
        this.mFocusNoteEditText = null;
        View view = this.mScrollView.findFocus();
        if (view == null) {
            return null;
        }
        if (!(view instanceof NoteEditText)) {
            //不是NoteEditText实例
            return view;
        }
        this.mFocusNoteEditText = (NoteEditText) view;
        return this.mFocusNoteEditText;
    }

    public boolean isSelectionState() {
        if (this.mFocusNoteEditText == null) {
            return false;
        }
        SpannableStringBuilder sb = (SpannableStringBuilder) this.mFocusNoteEditText.getText();
        if (Selection.getSelectionStart(sb) != Selection.getSelectionEnd(sb)) {
            return true;
        }
        return false;
    }

    //添加文本--重点分析
    void addTextItem(NoteItemText nt) {
        //为父布局mEditParent添加一个子布局
        getLayoutInflater().inflate(R.layout.edit_textlist_item, this.mEditParent);
        //得到刚才加载的子布局
        View item = this.mEditParent.getChildAt(this.mEditParent.getChildCount() - 1);
        //得到子布局中的控件

        CheckImageView check = (CheckImageView) item.findViewById(R.id.check);
        NoteEditText edit = (NoteEditText) item.findViewById(R.id.text);
        //设置文字
        edit.setText(nt.mText);
        //设置字体大小
        edit.setTextSize((float) (this.mEditNote.mTextSize > 0 ? this.mEditNote.mTextSize : NoteData.DEFAULT_FONT_SIZE));

        //为edit设置文字改变监听


        DeleteImageView deleteView = (DeleteImageView) item.findViewById(R.id.delete);

        switch (nt.mState) {
            case REQUEST_CODE_PICK /*0*/:
                check.setImageType(nt.mState);
                deleteView.setVisibility(View.GONE);
                return;
            //导出为图片的情况
            case REQUEST_CODE_EXPORT_TO_PIC /*1*/:
                check.setImageType(nt.mState);
                //设置文字是否StrikeThrough
                setEditStrikeThrough(edit, false);
                deleteView.setVisibility(View.GONE);
                return;
            //导出为文本的情况
            case REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                check.setImageType(nt.mState);
                setEditStrikeThrough(edit, true);
                deleteView.setVisibility(View.GONE);
                return;
            default:
                return;
        }
    }

    //在界面上加一个图片---重点分析
    void addImageItem(NoteItemImage nt) {
        //为父布局mEditParent添加一个子布局
        getLayoutInflater().inflate(R.layout.edit_image, this.mEditParent);
        //获得刚才添加的子布局
        RichFrameLayout imageParent = (RichFrameLayout) this.mEditParent.getChildAt(this.mEditParent.getChildCount() - 1);
        //为子布局设置尺寸
        imageParent.setSize(nt.mWidth, nt.mHeight);
        //为子布局涉资uuid和资源
        imageParent.setUUIDandName(this.mEditNote.mUUId, nt.mFileName);
    }

    //在界面上加一个录音---重点分析
    void addRecordItem(NoteItemRecord nt) {
        //为父布局mEditParent添加一个子布局
        getLayoutInflater().inflate(R.layout.edit_record_item, this.mEditParent);
        //获得刚才添加的子布局
        RichFrameLayout parent = (RichFrameLayout) this.mEditParent.getChildAt(this.mEditParent.getChildCount() - 1);
        //为子布局涉资uuid和资源
        parent.setUUIDandName(this.mEditNote.mUUId, nt.mFileName);
        //录音
        ((RecordLinearLayout) parent.findViewById(R.id.recordLayout)).setRecordPlayManager((RecordLinearLayout.RecordPlayManager) this);
    }

    //初始化EditLayout，长 且有些问题
    void initEditLayout() {
        boolean addNew = true;
        int index = 0;
        JSONObject o;
        NoteItem nt;
        NoteItemText nt2;
        this.mDataList.clear();//清除数据
        this.mFocusNoteEditText = null;//置为空
        int size = 0;//数量为0
        JSONArray ja = null;//json数组置为空
        //mNoteData是String类型，如果有数据就加到界面中
        if (this.mEditNote.mNoteData != null) {
            //从mNoteData回复出json数组
            JSONArray ja2 = null;
            try {
                ja2 = new JSONArray(this.mEditNote.mNoteData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //设置size为数组的元素个数
            size = ja2.length();
            ja = ja2;
            addNew = true;
            //将所有从json数组中解析出来的对象，加到编辑界面中，重要
            while (index < size) {
                o = (JSONObject) ja.opt(index);
                nt = NoteData.getNoteItem(o);
                addNew = false;
                this.mDataList.add(nt); //加入数据列表
                switch (nt.mState) {//根据mStata判断
                    case 0 /*0*/:
                    case 1 /*1*/:
                    case 2 /*2*/:
                        addTextItem((NoteItemText) nt);
                        break;
                    case 3 /*3*/:
                        addImageItem((NoteItemImage) nt);
                        break;
                    case 4 /*4*/:
                        addRecordItem((NoteItemRecord) nt);
                        break;
                    default:
                        break;
                }
            }
        }
        //如果是新加的，则添加一个NoteItemText
        if (addNew) {
            nt2 = new NoteItemText();
            nt2.mState = 0;
            if (this.mType == -2) {
                nt2.mState = 1;
            }
            addTextItem(nt2);
        }
    }

    public OnClickListener getCheckClickListener() {
        return this.mCheckClickListener;
    }

    public OnClickListener getDeleteClickListener() {
        return this.mDeleteClickListener;
    }

    //？？？
    public void setEditStrikeThrough(TextView edit, boolean isStrike) {
        if (isStrike) {
            edit.setTextColor(this.mGreyColor);
            edit.getPaint().setStrikeThruText(true);
            return;
        }
        edit.setTextColor(this.mTextColor);
        edit.getPaint().setStrikeThruText(false);
    }

    //生成UUID
    String generateUUId() {
        return UUID.randomUUID().toString();
    }

    public void setTextChanged() {
        if (this.mInitOK && (this.mChanged & CHANGE_CONTENT) != CHANGE_CONTENT) {
            this.mChanged |= CHANGE_CONTENT;
            Log.d(TAG, "text  changed");
        }
    }

    public void setTitleChanged() {
        if (this.mInitOK && (this.mChanged & REQUEST_CODE_EXPORT_TO_PIC) != REQUEST_CODE_EXPORT_TO_PIC) {
            this.mChanged |= REQUEST_CODE_EXPORT_TO_PIC;
        }
    }

    public OnKeyListener getKeyPreListener() {
        return this.mEditKeyPreListener;
    }

    public EditTextCloud.OnKeyPreImeListener getKeyPreImeListener() {
        return this.mOnKeyPreImeListener;
    }

    void removeStrikeThough(CharSequence cs) {
        if (cs != null) {
            try {
                SpannableStringBuilder sb = (SpannableStringBuilder) cs;
                if (sb != null) {
                    StrikethroughSpan[] stSpans = (StrikethroughSpan[]) sb.getSpans(REQUEST_CODE_PICK, sb.length(), StrikethroughSpan.class);
                    if (stSpans != null) {
                        StrikethroughSpan[] arr$ = stSpans;
                        int len$ = arr$.length;
                        for (int i$ = REQUEST_CODE_PICK; i$ < len$; i$ += REQUEST_CODE_EXPORT_TO_PIC) {
                            sb.removeSpan(arr$[i$]);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //按键盘的回车键？？？
    boolean onKeyEnter() {
        if (findFocusEditView() == null) {
            return false;
        }
        View parent = (ViewGroup) this.mFocusNoteEditText.getParent();
        CheckImageView check0 = (CheckImageView) parent.findViewById(R.id.check);
        Editable edit = this.mFocusNoteEditText.getText();
        int start = this.mFocusNoteEditText.getSelectionStart();
        int type = check0.getImageType();
        if (type == 0) {
            return false;
        }
        if (!listCountCheck()) {
            return true;
        }
        CharSequence cutText = edit.subSequence(start, edit.length());
        edit.delete(start, edit.length());
        View item = getLayoutInflater().inflate(R.layout.edit_textlist_item, null);
        CheckImageView check = (CheckImageView) item.findViewById(R.id.check);
        NoteEditText neText = (NoteEditText) item.findViewById(R.id.text);
        neText.setText(cutText);
        neText.setTextSize((float) (this.mEditNote.mTextSize > 0 ? this.mEditNote.mTextSize : NoteData.DEFAULT_FONT_SIZE));
        DeleteImageView deleteView = (DeleteImageView) item.findViewById(R.id.delete);
        switch (type) {
            case REQUEST_CODE_PICK /*0*/:
                check.setImageType(type);
                deleteView.setVisibility(CHANGE_ENCRYPT);
                setEditStrikeThrough(neText, false);
                break;
            case REQUEST_CODE_EXPORT_TO_PIC /*1*/:
            case REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                check.setImageType(REQUEST_CODE_EXPORT_TO_PIC);
                setEditStrikeThrough(neText, false);
                deleteView.setVisibility(CHANGE_ENCRYPT);
                break;
        }
        int count = this.mEditParent.getChildCount();
        int index = REQUEST_CODE_PICK;
        while (index < count && this.mEditParent.getChildAt(index) != parent) {
            index += REQUEST_CODE_EXPORT_TO_PIC;
        }
        if (index < count) {
            index += REQUEST_CODE_EXPORT_TO_PIC;
        }
        this.mEditParent.addView(item, index);
        neText.requestFocus();
        Selection.setSelection(neText.getText(), REQUEST_CODE_PICK);
        showSoftInput(neText);

        setFirstHint();
        this.mChanged |= CHANGE_CONTENT;
        return true;
    }

    void changeListToText(View parent) {
        CheckImageView check = (CheckImageView) parent.findViewById(R.id.check);
        DeleteImageView deleteView = (DeleteImageView) parent.findViewById(R.id.delete);
        NoteEditText neText = (NoteEditText) parent.findViewById(R.id.text);
        Editable edit = neText.getText();
        int type = check.getImageType();
        check.setImageType(REQUEST_CODE_PICK);
        deleteView.setVisibility(View.GONE);
        setEditStrikeThrough(neText, false);
        mergeCommonText(parent);
    }

    //按键盘的删除按钮？？？
    boolean onKeyDel() {
        if (findFocusEditView() == null) {
            return false;
        }
        int start = this.mFocusNoteEditText.getSelectionStart();
        int end = this.mFocusNoteEditText.getSelectionEnd();
        if (start > 0 || start != end) {
            return false;
        }
        View parent = (ViewGroup) this.mFocusNoteEditText.getParent();
        int type = ((CheckImageView) parent.findViewById(R.id.check)).getImageType();
        if (type != 0) {
            onListMenuClick();
            return true;
        }
        Editable edit = this.mFocusNoteEditText.getText();
        int count = this.mEditParent.getChildCount();
        int index = 9;
        while (index < count && this.mEditParent.getChildAt(index) != parent) {
            index += 1;
        }
        if (index >= count) {
            return false;
        }
//        RestoreTextRemove rTextRemove;
        if (index >= 1) {
            View upper = this.mEditParent.getChildAt(index - 1);
            if ("image".equals(upper.getTag())) {
                if (edit != null && edit.length() == 0 && index < count - 1) {
                    this.mEditParent.removeView(parent);

                    setFirstHint();
                    this.mChanged |= CHANGE_CONTENT;
                }
                ((RichFrameLayout) upper).onFocus();
                return true;
            } else if ("record".equals(upper.getTag())) {
                if (edit != null && edit.length() == 0 && index < count - 1) {
                    this.mEditParent.removeView(parent);

                    setFirstHint();
                    this.mChanged |= CHANGE_CONTENT;
                }
                ((RichFrameLayout) upper).onFocus();
                return true;
            } else if (!"recording".equals(upper.getTag()) || this.mRecordingLayoutView == null) {
                NoteEditText neText = (NoteEditText) upper.findViewById(R.id.text);
                int len = neText.length();
                if (((CheckImageView) upper.findViewById(R.id.check)).getImageType() != 0) {
                    clearTextSpan(edit);
                }
                this.mEditParent.removeViewAt(index);

                neText.append(edit);
                neText.requestFocus();
                Selection.setSelection(neText.getText(), len);
                showSoftInput(neText);
                setFirstHint();
                this.mChanged |= CHANGE_CONTENT;
                return true;
            } else {
                this.mRecordingLayoutView.cancelRecording();

                this.mRecordingLayoutView = null;

                setFirstHint();
                this.mChanged |= CHANGE_CONTENT;
                return true;
            }
        } else if (count == REQUEST_CODE_EXPORT_TO_PIC) {
            return true;
        } else {
            this.mEditParent.removeView(parent);

            setFirstHint();
            this.mChanged |= CHANGE_CONTENT;
            return true;
        }
    }

    void clearTextSpan(CharSequence cs) {
        if (cs != null) {
            try {
                SpannableStringBuilder sb = (SpannableStringBuilder) cs;
                if (sb != null) {
                    CharacterStyle[] stSpans = (CharacterStyle[]) sb.getSpans(REQUEST_CODE_PICK, sb.length(), CharacterStyle.class);
                    if (stSpans != null) {
                        CharacterStyle[] arr$ = stSpans;
                        int len$ = arr$.length;
                        for (int i$ = REQUEST_CODE_PICK; i$ < len$; i$ += REQUEST_CODE_EXPORT_TO_PIC) {
                            CharacterStyle st = arr$[i$];
                            if ((st instanceof ForegroundColorSpan) || (st instanceof DrawableBackgroundSpan) || (st instanceof AbsoluteSizeSpan)) {
                                sb.removeSpan(st);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //显示软键盘？？？
    void showSoftInput(View edit) {
        if (edit != null && (edit instanceof EditText)) {
            InputMethodManager imm = InputMethodManagerUtils.peekInstance();
            imm.viewClicked(edit);
            imm.showSoftInput(edit, REQUEST_CODE_PICK);
        }
    }

    public void removeFocusView(RichFrameLayout richView) {
        int newfocus;
        boolean next = false;
        int count = this.mEditParent.getChildCount();
        int index = REQUEST_CODE_PICK;
        while (index < count && this.mEditParent.getChildAt(index) != richView) {
            index += REQUEST_CODE_EXPORT_TO_PIC;
        }
        this.mEditParent.removeViewAt(index);

        setTextChanged();
        deleteFileInDataBase(richView.getUUID(), richView.getFileName());

        scanNoteDir();
        if (index > 0) {
            newfocus = index - 1;
        } else {
            newfocus = index;
            next = true;
        }
        try {
            View parent = this.mEditParent.getChildAt(newfocus);
            if (parent != null) {
                String tag = (String) parent.getTag();
                NoteEditText newText;
                View nextView;
                if (NoteUtil.JSON_TEXT.equals(tag)) {
                    CheckImageView check = (CheckImageView) parent.findViewById(R.id.check);
                    newText = (NoteEditText) parent.findViewById(R.id.text);
                    int len = newText.getText().length();
                    if (check.getImageType() == 0) {

                        if (newfocus + REQUEST_CODE_EXPORT_TO_PIC < this.mEditParent.getChildCount()) {
                            nextView = this.mEditParent.getChildAt(newfocus + REQUEST_CODE_EXPORT_TO_PIC);
                            if (NoteUtil.JSON_TEXT.equals(nextView.getTag())) {
                                NoteEditText nextTextView = (NoteEditText) nextView.findViewById(R.id.text);
                                if (((CheckImageView) nextView.findViewById(R.id.check)).getImageType() == 0) {
                                    newText.append("\n");
                                    newText.append(nextTextView.getText());
                                    this.mEditParent.removeViewAt(newfocus + REQUEST_CODE_EXPORT_TO_PIC);
                                }
                            }
                        }

                    }
                    newText.requestFocus();
                    Editable text = newText.getText();
                    if (next) {
                        len = REQUEST_CODE_PICK;
                    }
                    Selection.setSelection(text, len);
                    showSoftInput(newText);
                } else if ("image".equals(tag) || "record".equals(tag)) {
                    ((RichFrameLayout) parent).onFocus();
                } else if ("recording".equals(tag)) {
                    nextView = this.mEditParent.getChildAt(newfocus + REQUEST_CODE_EXPORT_TO_PIC);
                    if (NoteUtil.JSON_TEXT.equals(nextView.getTag())) {
                        newText = (NoteEditText) nextView.findViewById(R.id.text);
                        newText.requestFocus();
                        Selection.setSelection(newText.getText(), REQUEST_CODE_PICK);
                        showSoftInput(newText);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //合并公共的text，
    void mergeCommonText(View parent) {
        if (parent != null) {
            int oldCount = this.mCount;
            this.mCount = 0;
            int childCount = this.mEditParent.getChildCount();
            int index = 0;
            while (index < childCount && this.mEditParent.getChildAt(index) != parent) {
                index += 1;
            }
            if (index != childCount) {
                NoteEditText newText;
                boolean append = false;
                int selectionPos = 0;
                View before = null;
                View after = null;
                if (index > 0) {
                    before = this.mEditParent.getChildAt(index - 1);
                }
                NoteEditText current = (NoteEditText) parent.findViewById(R.id.text);
                if (index + 1 < childCount) {
                    after = this.mEditParent.getChildAt(index + 1);
                }
//                RestoreTextMerge restoreTextMerge = null;
                NoteEditText edit = current;
                if (before != null && NoteUtil.JSON_TEXT.equals(before.getTag())) {
                    newText = (NoteEditText) before.findViewById(R.id.text);
                    if (((CheckImageView) before.findViewById(R.id.check)).getImageType() == 0) {

                        newText.append("\n");
                        selectionPos = newText.length();
                        newText.append(current.getText());
                        current = newText;
                        this.mEditParent.removeViewAt(index);
                        append = true;
                    }
                }
                if (after != null && NoteUtil.JSON_TEXT.equals(after.getTag())) {
                    newText = (NoteEditText) after.findViewById(R.id.text);
                    if (((CheckImageView) after.findViewById(R.id.check)).getImageType() == 0) {

                        current.append("\n");
                        current.append(newText.getText());
                        this.mEditParent.removeView(after);
                        append = true;
                    }
                }
                this.mCount = oldCount;

                if (append && current != null) {
                    current.requestFocus();
                    Selection.setSelection(current.getText(), selectionPos);
                }
                showSoftInput(current);
            }
        }
    }

    //当点击清单时执行的方法
    void onListMenuClick() {
        View view = findFocusView();
        CheckImageView check;
        NoteEditText neText;
        DeleteImageView newdeleteView;
        NoteEditText newText;
        if (this.mFocusNoteEditText == null) {
            int position = 0;
            if (this.mTitleView.hasFocus()) {
                View first = this.mEditParent.getChildAt(REQUEST_CODE_PICK);
                if (first != null && NoteUtil.JSON_TEXT.equals(first.getTag())) {
                    check = (CheckImageView) first.findViewById(R.id.check);
                    neText = (NoteEditText) first.findViewById(R.id.text);
                    if (check.getImageType() == 0 && neText.getText().length() == 0) {
                        newdeleteView = (DeleteImageView) first.findViewById(R.id.delete);
                        check.setImageType(REQUEST_CODE_EXPORT_TO_PIC);
                        newdeleteView.setVisibility(View.GONE);
                        neText.requestFocus();
                        Selection.setSelection(neText.getText(), REQUEST_CODE_PICK);
                        showSoftInput(neText);
                        setFirstHint();
                        this.mChanged |= CHANGE_CONTENT;
                        return;
                    }
                }
            } else if (view != null && (view instanceof RichFrameLayout)) {
                position = getChildPosition(view) + REQUEST_CODE_EXPORT_TO_PIC;
            }
            if (listCountCheck()) {
                View item = getLayoutInflater().inflate(R.layout.edit_textlist_item, null);
                CheckImageView newCheck = (CheckImageView) item.findViewById(R.id.check);
                newText = (NoteEditText) item.findViewById(R.id.text);
                newdeleteView = (DeleteImageView) item.findViewById(R.id.delete);
                newCheck.setImageType(REQUEST_CODE_EXPORT_TO_PIC);
                newdeleteView.setVisibility(CHANGE_ENCRYPT);
                this.mEditParent.addView(item, position);
                newText.requestFocus();
                Selection.setSelection(newText.getText(), REQUEST_CODE_PICK);
                showSoftInput(newText);

                setFirstHint();
                this.mChanged |= CHANGE_CONTENT;
                return;
            }
            return;
        }
        int start = this.mFocusNoteEditText.getSelectionStart();
        View parent = (ViewGroup) this.mFocusNoteEditText.getParent();
        check = (CheckImageView) parent.findViewById(R.id.check);
        DeleteImageView deleteView = (DeleteImageView) parent.findViewById(R.id.delete);
        neText = (NoteEditText) parent.findViewById(R.id.text);
        Editable edit = neText.getText();
        int type = check.getImageType();
        if (start == 0) {
//            RestoreStageChange restoreStageChange;
            if (type != 0) {
                int oldState = check.getImageType();
                check.setImageType(REQUEST_CODE_PICK);

                deleteView.setVisibility(CHANGE_ENCRYPT);
                setEditStrikeThrough(neText, false);
                mergeCommonText(parent);
            } else if (listCountCheck()) {
                check.setImageType(REQUEST_CODE_EXPORT_TO_PIC);

                deleteView.setVisibility(REQUEST_CODE_PICK);
                showSoftInput(neText);
            } else {
                return;
            }
        } else if (listCountCheck()) {
            CharSequence cs = edit.subSequence(start, edit.length());
            edit.delete(start, edit.length());
            deleteLastLineFeedChar(edit);
            CharSequence p1 = cs;
            clearTextSpan(p1);
            View item = getLayoutInflater().inflate(R.layout.edit_textlist_item, null);
            CheckImageView newCheck = (CheckImageView) item.findViewById(R.id.check);
            newText = (NoteEditText) item.findViewById(R.id.text);
            newText.setText(p1);
            newText.setTextSize((float) (this.mEditNote.mTextSize > 0 ? this.mEditNote.mTextSize : NoteData.DEFAULT_FONT_SIZE));
            newdeleteView = (DeleteImageView) item.findViewById(R.id.delete);
            newCheck.setImageType(REQUEST_CODE_EXPORT_TO_PIC);
            newdeleteView.setVisibility(CHANGE_ENCRYPT);
            int count = this.mEditParent.getChildCount();
            int index = REQUEST_CODE_PICK;
            while (index < count && this.mEditParent.getChildAt(index) != parent) {
                index += REQUEST_CODE_EXPORT_TO_PIC;
            }
            if (index < count) {
                index += REQUEST_CODE_EXPORT_TO_PIC;
            }
            this.mEditParent.addView(item, index);
            newText.requestFocus();
            Selection.setSelection(newText.getText(), REQUEST_CODE_PICK);
            showSoftInput(newText);

        } else {
            return;
        }
        setFirstHint();
        this.mChanged |= CHANGE_CONTENT;
    }

    //保存，新线程调用saveImpl();
    void save() {
            new Thread(new Runnable() {
                public void run() {
//                    NoteEditActivity.this.saveImpl();
                    NoteEditActivity.this.mChanged = 0;
                }
            }).start();
    }

    //按返回键，结束录音
    public void onBackPressed() {
        if (this.mRecordingLayoutView != null) {
            this.mRecordingLayoutView.stopRecording(false);
            this.mRecordingLayoutView = null;
        }
        super.onBackPressed();
    }




    //这个方法有问题很多问题，保存？？
/*    void saveImpl() {
        int index;
        View view;
        String tag;
        RichFrameLayout rl;
        NoteItemRecord nt;
        RecordingLayout rl2;
        NoteItemText nt2;
        NoteEditText edit;
        ScaleImageView image;
        NoteItemImage nii;
        int size;
        NoteItem ni;
        Uri noteUri;
        NoteApplication app;
        ArrayList<Integer> deleteList;
        NotePaperActivity npa;
        Long now;
        ArrayList<Integer> changedList;
        long accountID;
        boolean delete = true;
        boolean islist = false;
        if (this.mEditNote == null) {
            Log.e(TAG, "the mEditNote is null!");
            return;
        }
        ContentValues cv;

        //改变title
        if ((this.mChanged & CHANGE_TITLE) == CHANGE_TITLE) {
            Editable title = this.mTitleView.getText();
            if (title == null || title.length() <= 0) {
                this.mEditNote.mTitle = null;
            } else {
                delete = false;
                this.mEditNote.mTitle = title.toString();
            }
        }

        // delete = false;不知道什么意思
        if (this.mEditNote.mTitle != null && this.mEditNote.mTitle.length() > 0) {
            delete = false;
        }
        ArrayList<String> fileList = new ArrayList();
        //改变内容
        if ((this.mChanged & CHANGE_CONTENT) == CHANGE_CONTENT) {
            BufferedWriter bw;
            JSONObject jo;
            long time;
            String head;
            NoteItemImage nt3;
            this.mDataList.clear();
            int childCount = this.mEditParent.getChildCount();
            this.mFirstImg = null;
            this.mFirstRecord = null;
            FileWriter fw = null;
            ///开始try
            try {
                Writer fileWriter = new FileWriter(getFileStreamPath("list"), true);
                ////try中try
                    bw = new BufferedWriter(fileWriter);
                    fw = fileWriter;
                    ////try中try的catch
                    if (fw != null) {
                            fw.close();
                    }
                    fw = null;
                    bw = null;
                    /////
                    for (index = 0; index < childCount; index += 1) {
                        view = this.mEditParent.getChildAt(index);
                        tag = (String) view.getTag();

                            if (!"record".equals(tag)) {
                                rl = (RichFrameLayout) view;
                                nt = new NoteItemRecord();
                                nt.mState = REQUEST_CODE_LOGIN;
                                nt.mFileName = rl.getFileName();
                                this.mDataList.add(nt);
                                if (this.mFirstRecord == null) {
                                    jo = new JSONObject();
                                    jo.put(NoteUtil.JSON_STATE, nt.mState);
                                    jo.put(NoteUtil.JSON_FILE_NAME, nt.mFileName);
                                    this.mFirstRecord = jo.toString();
                                }
                                fileList.add(nt.mFileName);
                            } else if (!"recording".equals(tag)) {
                                rl2 = (RecordingLayout) view;
                                nt = new NoteItemRecord();
                                nt.mState = REQUEST_CODE_LOGIN;
                                nt.mFileName = rl2.getRecordFileName();
                                this.mDataList.add(nt);
                                if (this.mFirstRecord == null) {
                                    jo = new JSONObject();
                                    jo.put(NoteUtil.JSON_STATE, nt.mState);
                                    jo.put(NoteUtil.JSON_FILE_NAME, nt.mFileName);
                                    this.mFirstRecord = jo.toString();
                                }
                                fileList.add(nt.mFileName);
                            } else if (!NoteUtil.JSON_TEXT.equals(tag)) {
                                nt2 = new NoteItemText();
                                edit = (NoteEditText) view.findViewById(R.id.text);
                                nt2.mText = edit.getText().toString();
                                nt2.mState = ((CheckImageView) view.findViewById(R.id.check)).getImageType();
                                nt2.mSpan = null;
                                if (nt2.mState != 0) {
                                    if (!islist) {
                                        islist = true;
                                    }
                                    time = System.currentTimeMillis();
                                    if (bw != null) {
                                        head = BuildConfig.VERSION_NAME;
                                        if (index == 0) {
                                            head = "\n";
                                        }
                                        bw.write(head + this.mEditNote.mId + LunarCalendar.DATE_SEPARATOR + index + LunarCalendar.DATE_SEPARATOR + String.valueOf(time) + ":" + nt2.mText + "\nview : " + view + "\nedit: " + edit + "\n");
                                    }
                                }
                                this.mDataList.add(nt2);
                            } else if (!"image".equals(tag)) {
                                image = (ScaleImageView) view.findViewById(R.id.image);
                                nt3 = new NoteItemImage();
                                nt3.mState = REQUEST_CODE_SHARING;
                                nt3.mHeight = image.mHeight;
                                nt3.mWidth = image.mWidth;
                                nt3.mFileName = image.mFileName;
                                this.mDataList.add(nt3);
                                if (this.mFirstImg == null) {
                                    jo = new JSONObject();
                                    nii = nt3;
                                    jo.put(NoteUtil.JSON_STATE, nii.mState);
                                    jo.put(NoteUtil.JSON_IMAGE_HEIGHT, nii.mHeight);
                                    jo.put(NoteUtil.JSON_IMAGE_WIDTH, nii.mWidth);
                                    jo.put(NoteUtil.JSON_FILE_NAME, nii.mFileName);
                                    this.mFirstImg = jo.toString();
                                }
                                fileList.add(nt3.mFileName);
                            }
                    }
                    /////
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                        }
                    }
                    /////
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (Exception e322) {
                            e322.printStackTrace();
                        }
                    }
                    /////
                    if (delete) {
                        size = this.mDataList.size();
                        index = 0;
                        while (index < size) {
                            ni = (NoteItem) this.mDataList.get(index);
                            if (ni.mState != REQUEST_CODE_SHARING) {
                                delete = false;
                                break;
                            } else if (ni.mState == REQUEST_CODE_LOGIN) {
                                delete = false;
                                break;
                            } else {
                                nt2 = (NoteItemText) ni;
                                if (nt2.mText != null) {
                                }
                                index += 1;
                            }
                        }
                    }
                    /////
                    if (delete) {
                        if (this.mEditNote.mId == -1) {
                        }
                        if (REQUEST_CODE_EXPORT_TO_PIC == this.mPauseState) {
                            if (this.mEditNote.mId == -1) {
                                noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, this.mEditNote.mId);
                                app = (NoteApplication) getApplication();
                                if (this.mPosition != -1) {
                                    deleteList = new ArrayList();
                                    deleteList.add(Integer.valueOf(this.mPosition));
                                    app.setChangedData(REQUEST_CODE_SHARING, deleteList);
                                }
                                npa = app.getNotePaperActivity();
                                if (npa != null) {
                                    npa.setDeleteItemId(this.mEditNote.mId);
                                }
                                getContentResolver().delete(noteUri, null, null);
                                return;
                            }
                            return;
                        }
                    }
                    /////
                    cv = new ContentValues();
                    if (this.mEditNote.mId == -1) {
                        cv.put(Notes.TITLE, this.mEditNote.mTitle);
                        cv.put(Notes.NOTE, convert2JsonNoteText());
                        now = Long.valueOf(System.currentTimeMillis());
                        cv.put(Notes.CREATE_TIME, Long.valueOf(this.mEditNote.mCreateTime));
                        cv.put(Notes.MODIFIED_DATE, now);
                        cv.put(Notes.PAPER, Integer.valueOf(this.mEditNote.mPaper));
                        cv.put(Notes.FONT_SIZE, Integer.valueOf(this.mEditNote.mTextSize));
                        cv.put(Notes.UUID, this.mEditNote.mUUId);
                        cv.put(Notes.FIRST_IMAGE, this.mFirstImg);
                        cv.put(Notes.FIRST_RECORD, this.mFirstRecord);
                        cv.put(Notes.FILE_LIST, NoteUtil.getFileListString(fileList));
                        cv.put(Notes.TAG, Long.valueOf(this.mEditNote.mTag));
                        if ((this.mChanged & REQUEST_CODE_EXPORT_TO_TEXT) != 0) {
                            cv.put(Notes.TOP, Long.valueOf(this.mEditNote.mTopTime));
                        }
                        if ((this.mChanged & REQUEST_CODE_LOGIN) != 0) {
                            cv.put(Notes.DESKTOP, Integer.valueOf(this.mEditNote.mDesktop));
                        }
                        cv.put(Notes.ENCRYPT, Integer.valueOf(this.mEditNote.mEncrypt ? REQUEST_CODE_EXPORT_TO_PIC : REQUEST_CODE_PICK));
                        app = (NoteApplication) getApplication();
                        changedList = new ArrayList();
                        changedList.add(Integer.valueOf(REQUEST_CODE_PICK));
                        app.setChangedData(REQUEST_CODE_EXPORT_TO_PIC, changedList);
                        this.mEditNote.mId = ContentUris.parseId(getContentResolver().insert(Notes.CONTENT_URI, cv));
                        this.mEditNote.mFirstImg = this.mFirstImg;
                        this.mEditNote.mFirstRecord = this.mFirstRecord;
                        MobEventUtil.onSendMobEvent(this, "new_note", null);
                        if (!TextUtils.isEmpty(this.mTitleView.getText())) {
                            MobEventUtil.onSendMobEvent(this, "click_title_bar", null);
                        }
                        if (this.mFloatFlag) {
                            this.mNewFlag = true;
                            enterFloatingMode();
                        }
                        if (this.mEditNote.mTag > 0) {
                            if ((this.mChanged & CHANGE_TAG) == 0) {
                                MobEventUtil.onSendMobEvent(this, "click_new_withgroup", (String) null);
                            } else {
                                MobEventUtil.onSendMobEvent(this, "click_modifygroup", (String) null);
                            }
                        }
                        if ((this.mChanged & CHANGE_FONT_SIZE) == 0) {
                            MobEventUtil.onSendMobEvent(this, "change_font_size", (String) null);
                            return;
                        }
                        return;
                    }
                    /////
                    if (this.mChanged != 0) {
                        noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, this.mEditNote.mId);
                        if ((this.mChanged & REQUEST_CODE_EXPORT_TO_PIC) != 0) {
                            cv.put(Notes.TITLE, this.mEditNote.mTitle);
                        }
                        if ((this.mChanged & CHANGE_CONTENT) != 0) {
                            cv.put(Notes.NOTE, convert2JsonNoteText());
                            app = (NoteApplication) getApplication();
                            if (this.mEditNote.mFirstImg == null) {
                            }
                            cv.put(Notes.FIRST_IMAGE, this.mFirstImg);
                            app.onNoteImgChanged(this.mEditNote.mUUId);
                            if (this.mEditNote.mFirstRecord == null) {
                            }
                            cv.put(Notes.FIRST_RECORD, this.mFirstRecord);
                            cv.put(Notes.FILE_LIST, NoteUtil.getFileListString(fileList));
                        }
                        now = Long.valueOf(System.currentTimeMillis());
                        if ((this.mChanged & -1048591) != 0) {
                            cv.put(Notes.MODIFIED_DATE, now);
                        }
                        if ((this.mChanged & CHANGE_PAPER) != 0) {
                            cv.put(Notes.PAPER, Integer.valueOf(this.mEditNote.mPaper));
                        }
                        if ((this.mChanged & CHANGE_FONT_COLOR) != 0) {
                        }
                        if ((this.mChanged & CHANGE_FONT_SIZE) != 0) {
                            cv.put(Notes.FONT_SIZE, Integer.valueOf(this.mEditNote.mTextSize));
                            MobEventUtil.onSendMobEvent(this, "change_font_size", (String) null);
                        }
                        if ((this.mChanged & CHANGE_TAG) != 0) {
                            cv.put(Notes.TAG, Long.valueOf(this.mEditNote.mTag));
                            if (this.mEditNote.mTag > 0) {
                                MobEventUtil.onSendMobEvent(this, "click_modifygroup", (String) null);
                            }
                        }
                        if ((this.mChanged & REQUEST_CODE_EXPORT_TO_TEXT) != 0) {
                            cv.put(Notes.TOP, Long.valueOf(this.mEditNote.mTopTime));
                        }
                        if ((this.mChanged & REQUEST_CODE_LOGIN) != 0) {
                            cv.put(Notes.DESKTOP, Integer.valueOf(this.mEditNote.mDesktop));
                        }
                        cv.put(Notes.ENCRYPT, Integer.valueOf(this.mEditNote.mEncrypt ? REQUEST_CODE_EXPORT_TO_PIC : REQUEST_CODE_PICK));
                        if (this.mEditNote.mEncrypt) {
                            accountID = ((NoteApplication) getApplication()).getMeizuAccount();
                            if (accountID > 0) {
                                cv.put(Notes.ACCOUNT_ID, Long.valueOf(accountID));
                            } else {
                                Log.e(TAG, "Encrypt without flyme account login, this shouldn't happen!");
                            }
                        } else {
                            cv.put(Notes.ACCOUNT_ID, Integer.valueOf(REQUEST_CODE_PICK));
                        }
                        app = (NoteApplication) getApplication();
                        changedList = new ArrayList();
                        changedList.add(Integer.valueOf(this.mPosition));
                        app.setChangedData(REQUEST_CODE_EXPORT_TO_TEXT, changedList);
                        getContentResolver().update(noteUri, cv, null, null);
                        this.mEditNote.mFirstImg = this.mFirstImg;
                        this.mEditNote.mFirstRecord = this.mFirstRecord;
                    }
                    /////
                    if (!this.mFloatFlag) {
                        enterFloatingMode();
                    }

                ////try中try的catch结束
            ///try的catch开始
            } catch (Exception e4) {
                ////
                if (fw != null) {
                    fw.close();
                }
                fw = null;
                bw = null;
                ////
                for (index = REQUEST_CODE_PICK; index < childCount; index += REQUEST_CODE_EXPORT_TO_PIC) {
                    view = this.mEditParent.getChildAt(index);
                    tag = (String) view.getTag();
                    if (!"record".equals(tag)) {
                        rl = (RichFrameLayout) view;
                        nt = new NoteItemRecord();
                        nt.mState = REQUEST_CODE_LOGIN;
                        nt.mFileName = rl.getFileName();
                        this.mDataList.add(nt);
                        if (this.mFirstRecord == null) {
                            jo = new JSONObject();
                            jo.put(NoteUtil.JSON_STATE, nt.mState);
                            jo.put(NoteUtil.JSON_FILE_NAME, nt.mFileName);
                            this.mFirstRecord = jo.toString();
                        }
                        fileList.add(nt.mFileName);
                    } else if (!"recording".equals(tag)) {
                        rl2 = (RecordingLayout) view;
                        nt = new NoteItemRecord();
                        nt.mState = REQUEST_CODE_LOGIN;
                        nt.mFileName = rl2.getRecordFileName();
                        this.mDataList.add(nt);
                        if (this.mFirstRecord == null) {
                            jo = new JSONObject();
                            jo.put(NoteUtil.JSON_STATE, nt.mState);
                            jo.put(NoteUtil.JSON_FILE_NAME, nt.mFileName);
                            this.mFirstRecord = jo.toString();
                        }
                        fileList.add(nt.mFileName);
                    } else if (!NoteUtil.JSON_TEXT.equals(tag)) {
                        nt2 = new NoteItemText();
                        edit = (NoteEditText) view.findViewById(R.id.text);
                        nt2.mText = edit.getText().toString();
                        nt2.mState = ((CheckImageView) view.findViewById(R.id.check)).getImageType();
                        nt2.mSpan = null;
                        if (nt2.mState != 0) {
                            if (islist) {
                                islist = true;
                            }
                            time = System.currentTimeMillis();
                            if (bw != null) {
                                head = BuildConfig.VERSION_NAME;
                                if (index == 0) {
                                    head = "\n";
                                }
                                bw.write(head + this.mEditNote.mId + LunarCalendar.DATE_SEPARATOR + index + LunarCalendar.DATE_SEPARATOR + String.valueOf(time) + ":" + nt2.mText + "\nview : " + view + "\nedit: " + edit + "\n");
                            }
                        }
                        this.mDataList.add(nt2);
                    } else if (!"image".equals(tag)) {
                        image = (ScaleImageView) view.findViewById(R.id.image);
                        nt3 = new NoteItemImage();
                        nt3.mState = REQUEST_CODE_SHARING;
                        nt3.mHeight = image.mHeight;
                        nt3.mWidth = image.mWidth;
                        nt3.mFileName = image.mFileName;
                        this.mDataList.add(nt3);
                        if (this.mFirstImg == null) {
                            jo = new JSONObject();
                            nii = nt3;
                            jo.put(NoteUtil.JSON_STATE, nii.mState);
                            jo.put(NoteUtil.JSON_IMAGE_HEIGHT, nii.mHeight);
                            jo.put(NoteUtil.JSON_IMAGE_WIDTH, nii.mWidth);
                            jo.put(NoteUtil.JSON_FILE_NAME, nii.mFileName);
                            this.mFirstImg = jo.toString();
                        }
                        fileList.add(nt3.mFileName);
                    }
                }
                ////
                if (bw != null) {
                    bw.close();
                }
                ////
                if (fw != null) {
                    fw.close();
                }
                ////
                if (delete) {
                    size = this.mDataList.size();
                    index = REQUEST_CODE_PICK;
                    while (index < size) {
                        ni = (NoteItem) this.mDataList.get(index);
                        if (ni.mState != REQUEST_CODE_SHARING) {
                            if (ni.mState == REQUEST_CODE_LOGIN) {
                                delete = false;
                                break;
                            }
                            nt2 = (NoteItemText) ni;
                            if (nt2.mText != null) {
                            }
                            index += REQUEST_CODE_EXPORT_TO_PIC;
                        } else {
                            delete = false;
                            break;
                        }
                    }
                }
                ////
                if (delete) {
                    if (this.mEditNote.mId == -1) {
                    }
                    if (REQUEST_CODE_EXPORT_TO_PIC == this.mPauseState) {
                        if (this.mEditNote.mId == -1) {
                            noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, this.mEditNote.mId);
                            app = (NoteApplication) getApplication();
                            if (this.mPosition != -1) {
                                deleteList = new ArrayList();
                                deleteList.add(Integer.valueOf(this.mPosition));
                                app.setChangedData(REQUEST_CODE_SHARING, deleteList);
                            }
                            npa = app.getNotePaperActivity();
                            if (npa != null) {
                                npa.setDeleteItemId(this.mEditNote.mId);
                            }
                            getContentResolver().delete(noteUri, null, null);
                            return;
                        }
                        return;
                    }
                }
                ////
                cv = new ContentValues();
                if (this.mEditNote.mId == -1) {
                    if (this.mChanged != 0) {
                        noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, this.mEditNote.mId);
                        if ((this.mChanged & REQUEST_CODE_EXPORT_TO_PIC) != 0) {
                            cv.put(Notes.TITLE, this.mEditNote.mTitle);
                        }
                        if ((this.mChanged & CHANGE_CONTENT) != 0) {
                            cv.put(Notes.NOTE, convert2JsonNoteText());
                            app = (NoteApplication) getApplication();
                            if (this.mEditNote.mFirstImg == null) {
                            }
                            cv.put(Notes.FIRST_IMAGE, this.mFirstImg);
                            app.onNoteImgChanged(this.mEditNote.mUUId);
                            if (this.mEditNote.mFirstRecord == null) {
                            }
                            cv.put(Notes.FIRST_RECORD, this.mFirstRecord);
                            cv.put(Notes.FILE_LIST, NoteUtil.getFileListString(fileList));
                        }
                        now = Long.valueOf(System.currentTimeMillis());
                        if ((this.mChanged & -1048591) != 0) {
                            cv.put(Notes.MODIFIED_DATE, now);
                        }
                        if ((this.mChanged & CHANGE_PAPER) != 0) {
                            cv.put(Notes.PAPER, Integer.valueOf(this.mEditNote.mPaper));
                        }
                        if ((this.mChanged & CHANGE_FONT_COLOR) != 0) {
                        }
                        if ((this.mChanged & CHANGE_FONT_SIZE) != 0) {
                            cv.put(Notes.FONT_SIZE, Integer.valueOf(this.mEditNote.mTextSize));
                            MobEventUtil.onSendMobEvent(this, "change_font_size", (String) null);
                        }
                        if ((this.mChanged & CHANGE_TAG) != 0) {
                            cv.put(Notes.TAG, Long.valueOf(this.mEditNote.mTag));
                            if (this.mEditNote.mTag > 0) {
                                MobEventUtil.onSendMobEvent(this, "click_modifygroup", (String) null);
                            }
                        }
                        if ((this.mChanged & REQUEST_CODE_EXPORT_TO_TEXT) != 0) {
                            cv.put(Notes.TOP, Long.valueOf(this.mEditNote.mTopTime));
                        }
                        if ((this.mChanged & REQUEST_CODE_LOGIN) != 0) {
                            cv.put(Notes.DESKTOP, Integer.valueOf(this.mEditNote.mDesktop));
                        }
                        if (this.mEditNote.mEncrypt) {
                        }
                        cv.put(Notes.ENCRYPT, Integer.valueOf(this.mEditNote.mEncrypt ? REQUEST_CODE_EXPORT_TO_PIC : REQUEST_CODE_PICK));
                        if (this.mEditNote.mEncrypt) {
                            cv.put(Notes.ACCOUNT_ID, Integer.valueOf(REQUEST_CODE_PICK));
                        } else {
                            accountID = ((NoteApplication) getApplication()).getMeizuAccount();
                            if (accountID > 0) {
                                Log.e(TAG, "Encrypt without flyme account login, this shouldn't happen!");
                            } else {
                                cv.put(Notes.ACCOUNT_ID, Long.valueOf(accountID));
                            }
                        }
                        app = (NoteApplication) getApplication();
                        changedList = new ArrayList();
                        changedList.add(Integer.valueOf(this.mPosition));
                        app.setChangedData(REQUEST_CODE_EXPORT_TO_TEXT, changedList);
                        getContentResolver().update(noteUri, cv, null, null);
                        this.mEditNote.mFirstImg = this.mFirstImg;
                        this.mEditNote.mFirstRecord = this.mFirstRecord;
                    }
                    if (!this.mFloatFlag) {
                        enterFloatingMode();
                    }
                }
                ////
                cv.put(Notes.TITLE, this.mEditNote.mTitle);
                cv.put(Notes.NOTE, convert2JsonNoteText());
                now = Long.valueOf(System.currentTimeMillis());
                cv.put(Notes.CREATE_TIME, Long.valueOf(this.mEditNote.mCreateTime));
                cv.put(Notes.MODIFIED_DATE, now);
                cv.put(Notes.PAPER, Integer.valueOf(this.mEditNote.mPaper));
                cv.put(Notes.FONT_SIZE, Integer.valueOf(this.mEditNote.mTextSize));
                cv.put(Notes.UUID, this.mEditNote.mUUId);
                cv.put(Notes.FIRST_IMAGE, this.mFirstImg);
                cv.put(Notes.FIRST_RECORD, this.mFirstRecord);
                cv.put(Notes.FILE_LIST, NoteUtil.getFileListString(fileList));
                cv.put(Notes.TAG, Long.valueOf(this.mEditNote.mTag));
                ////
                if ((this.mChanged & REQUEST_CODE_EXPORT_TO_TEXT) != 0) {
                    cv.put(Notes.TOP, Long.valueOf(this.mEditNote.mTopTime));
                }
                ////
                if ((this.mChanged & REQUEST_CODE_LOGIN) != 0) {
                    cv.put(Notes.DESKTOP, Integer.valueOf(this.mEditNote.mDesktop));
                }
                ////
                if (this.mEditNote.mEncrypt) {
                }
                cv.put(Notes.ENCRYPT, Integer.valueOf(this.mEditNote.mEncrypt ? REQUEST_CODE_EXPORT_TO_PIC : REQUEST_CODE_PICK));
                app = (NoteApplication) getApplication();
                changedList = new ArrayList();
                changedList.add(Integer.valueOf(REQUEST_CODE_PICK));
                app.setChangedData(REQUEST_CODE_EXPORT_TO_PIC, changedList);
                this.mEditNote.mId = ContentUris.parseId(getContentResolver().insert(Notes.CONTENT_URI, cv));
                this.mEditNote.mFirstImg = this.mFirstImg;
                this.mEditNote.mFirstRecord = this.mFirstRecord;
                MobEventUtil.onSendMobEvent(this, "new_note", null);
                ////
                if (TextUtils.isEmpty(this.mTitleView.getText())) {
                    MobEventUtil.onSendMobEvent(this, "click_title_bar", null);
                }
                ////
                if (this.mFloatFlag) {
                    this.mNewFlag = true;
                    enterFloatingMode();
                }
                ////
                if (this.mEditNote.mTag > 0) {
                    if ((this.mChanged & CHANGE_TAG) == 0) {
                        MobEventUtil.onSendMobEvent(this, "click_modifygroup", (String) null);
                    } else {
                        MobEventUtil.onSendMobEvent(this, "click_new_withgroup", (String) null);
                    }
                }
                ////
                if ((this.mChanged & CHANGE_FONT_SIZE) == 0) {
                    MobEventUtil.onSendMobEvent(this, "change_font_size", (String) null);
                    return;
                }
                ////
                return;
            }
            ///try的catch结束,try结束

            ///
            for (index = 0; index < childCount; index += 1) {
                view = this.mEditParent.getChildAt(index);
                tag = (String) view.getTag();
                if (!"record".equals(tag)) {
                    rl = (RichFrameLayout) view;
                    nt = new NoteItemRecord();
                    nt.mState = REQUEST_CODE_LOGIN;
                    nt.mFileName = rl.getFileName();
                    this.mDataList.add(nt);
                    if (this.mFirstRecord == null) {
                        jo = new JSONObject();
                        jo.put(NoteUtil.JSON_STATE, nt.mState);
                        jo.put(NoteUtil.JSON_FILE_NAME, nt.mFileName);
                        this.mFirstRecord = jo.toString();
                    }
                    fileList.add(nt.mFileName);
                } else if (!"recording".equals(tag)) {
                    rl2 = (RecordingLayout) view;
                    nt = new NoteItemRecord();
                    nt.mState = REQUEST_CODE_LOGIN;
                    nt.mFileName = rl2.getRecordFileName();
                    if (nt.mFileName != null && (NoteUtil.getFile(this.mEditNote.mUUId, nt.mFileName).length() / 8) / 1000 >= 1) {
                        this.mDataList.add(nt);
                        if (this.mFirstRecord == null) {
                            jo = new JSONObject();
                            jo.put(NoteUtil.JSON_STATE, nt.mState);
                            jo.put(NoteUtil.JSON_FILE_NAME, nt.mFileName);
                            this.mFirstRecord = jo.toString();
                        }
                        fileList.add(nt.mFileName);
                    }
                } else if (!NoteUtil.JSON_TEXT.equals(tag)) {
                    nt2 = new NoteItemText();
                    edit = (NoteEditText) view.findViewById(R.id.text);
                    nt2.mText = edit.getText().toString();
                    nt2.mState = ((CheckImageView) view.findViewById(R.id.check)).getImageType();
                    nt2.mSpan = null;
                    if (nt2.mState != 0) {
                        if (islist) {
                            islist = true;
                        }
                        time = System.currentTimeMillis();
                        if (bw != null) {
                            head = BuildConfig.VERSION_NAME;
                            if (index == 0) {
                                head = "\n";
                            }
                            bw.write(head + this.mEditNote.mId + LunarCalendar.DATE_SEPARATOR + index + LunarCalendar.DATE_SEPARATOR + String.valueOf(time) + ":" + nt2.mText + "\nview : " + view + "\nedit: " + edit + "\n");
                        }
                    }
                    this.mDataList.add(nt2);
                } else if (!"image".equals(tag)) {
                    image = (ScaleImageView) view.findViewById(R.id.image);
                    nt3 = new NoteItemImage();
                    nt3.mState = REQUEST_CODE_SHARING;
                    nt3.mHeight = image.mHeight;
                    nt3.mWidth = image.mWidth;
                    nt3.mFileName = image.mFileName;
                    this.mDataList.add(nt3);
                    if (this.mFirstImg == null) {
                        jo = new JSONObject();
                        nii = nt3;
                        jo.put(NoteUtil.JSON_STATE, nii.mState);
                        jo.put(NoteUtil.JSON_IMAGE_HEIGHT, nii.mHeight);
                        jo.put(NoteUtil.JSON_IMAGE_WIDTH, nii.mWidth);
                        jo.put(NoteUtil.JSON_FILE_NAME, nii.mFileName);
                        this.mFirstImg = jo.toString();
                    }
                    fileList.add(nt3.mFileName);
                }
            }
            ///
            if (bw != null) {
                bw.close();
            }
            ///
            if (fw != null) {
                fw.close();
            }
        }
        //改变内容结束，哈哈h6asdf1-3asdfasd-6f1-32水电费-312 31 321-32asd1f-3a2sd1f-3a-3sd21f-a23sd1f6w5e4r656511-3ds1f-32a1sd-f323-a2sd1f-3a2d1sf-32asd1f-31



        //
        if (delete) {
            size = this.mDataList.size();
            index = REQUEST_CODE_PICK;
            while (index < size) {
                ni = (NoteItem) this.mDataList.get(index);
                if (ni.mState != REQUEST_CODE_SHARING) {
                    if (ni.mState == REQUEST_CODE_LOGIN) {
                        nt2 = (NoteItemText) ni;
                        if (nt2.mText != null && nt2.mText.length() > 0) {
                            delete = false;
                            break;
                        }
                        index += REQUEST_CODE_EXPORT_TO_PIC;
                    } else {
                        delete = false;
                        break;
                    }
                }
                delete = false;
                break;
            }
        }

        //
        if (delete) {
            if (this.mEditNote.mId == -1 || this.mFloatFlag) {
                if (REQUEST_CODE_EXPORT_TO_PIC == this.mPauseState) {
                    if (this.mEditNote.mId == -1) {
                        noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, this.mEditNote.mId);
                       //一大堆事，删除时要做
                        *//* app = (NoteApplication) getApplication();
                        if (this.mPosition != -1) {
                            deleteList = new ArrayList();
                            deleteList.add(Integer.valueOf(this.mPosition));
                            app.setChangedData(REQUEST_CODE_SHARING, deleteList);
                        }
                        npa = app.getNotePaperActivity();
                        if (npa != null) {
                            npa.setDeleteItemId(this.mEditNote.mId);
                        }*//*
                        getContentResolver().delete(noteUri, null, null);
                        return;
                    }
                    return;
                }
            }
            return;
        }
        cv = new ContentValues();
        //
        if (this.mEditNote.mId == -1) {
            cv.put(Notes.TITLE, this.mEditNote.mTitle);
            cv.put(Notes.NOTE, convert2JsonNoteText());
            now = Long.valueOf(System.currentTimeMillis());
            cv.put(Notes.CREATE_TIME, Long.valueOf(this.mEditNote.mCreateTime));
            cv.put(Notes.MODIFIED_DATE, now);
            cv.put(Notes.PAPER, Integer.valueOf(this.mEditNote.mPaper));
            cv.put(Notes.FONT_SIZE, Integer.valueOf(this.mEditNote.mTextSize));
            cv.put(Notes.UUID, this.mEditNote.mUUId);
            cv.put(Notes.FIRST_IMAGE, this.mFirstImg);
            cv.put(Notes.FIRST_RECORD, this.mFirstRecord);
            cv.put(Notes.FILE_LIST, NoteUtil.getFileListString(fileList));
            cv.put(Notes.TAG, Long.valueOf(this.mEditNote.mTag));
            if ((this.mChanged & REQUEST_CODE_EXPORT_TO_TEXT) != 0) {
                cv.put(Notes.TOP, Long.valueOf(this.mEditNote.mTopTime));
            }
            if ((this.mChanged & REQUEST_CODE_LOGIN) != 0) {
                cv.put(Notes.DESKTOP, Integer.valueOf(this.mEditNote.mDesktop));
            }
            if (TagData.FUN_ENCRYPT && (this.mChanged & CHANGE_ENCRYPT) != 0) {
                if (this.mEditNote.mEncrypt) {
                }
                cv.put(Notes.ENCRYPT, Integer.valueOf(this.mEditNote.mEncrypt ? REQUEST_CODE_EXPORT_TO_PIC : REQUEST_CODE_PICK));
            }
//            app = (NoteApplication) getApplication();
            changedList = new ArrayList();
            changedList.add(Integer.valueOf(REQUEST_CODE_PICK));
//            app.setChangedData(REQUEST_CODE_EXPORT_TO_PIC, changedList);
            this.mEditNote.mId = ContentUris.parseId(getContentResolver().insert(Notes.CONTENT_URI, cv));
            this.mEditNote.mFirstImg = this.mFirstImg;
            this.mEditNote.mFirstRecord = this.mFirstRecord;
//            MobEventUtil.onSendMobEvent(this, "new_note", null);
            if (TextUtils.isEmpty(this.mTitleView.getText())) {
//                MobEventUtil.onSendMobEvent(this, "click_title_bar", null);
            }
            if (this.mFloatFlag) {
                this.mNewFlag = true;
                enterFloatingMode();
            }
            if (this.mEditNote.mTag > 0) {
                if ((this.mChanged & CHANGE_TAG) == 0) {
//                    MobEventUtil.onSendMobEvent(this, "click_new_withgroup", (String) null);
                } else {
//                    MobEventUtil.onSendMobEvent(this, "click_modifygroup", (String) null);
                }
            }
            if ((this.mChanged & CHANGE_FONT_SIZE) == 0) {
//                MobEventUtil.onSendMobEvent(this, "change_font_size", (String) null);
                return;
            }
            return;
        }

        //
        if (this.mChanged != 0) {
            noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, this.mEditNote.mId);
            if ((this.mChanged & REQUEST_CODE_EXPORT_TO_PIC) != 0) {
                cv.put(Notes.TITLE, this.mEditNote.mTitle);
            }
            if ((this.mChanged & CHANGE_CONTENT) != 0) {
                cv.put(Notes.NOTE, convert2JsonNoteText());
                //涉及NoteApplication
//                app = (NoteApplication) getApplication();
                if (this.mEditNote.mFirstImg == null && !this.mEditNote.mFirstImg.equals(this.mFirstImg)) {
                    cv.put(Notes.FIRST_IMAGE, this.mFirstImg);
//                    app.onNoteImgChanged(this.mEditNote.mUUId);
                } else if (this.mEditNote.mFirstImg == null && this.mFirstImg != null) {
                    cv.put(Notes.FIRST_IMAGE, this.mFirstImg);
//                    app.onNoteImgChanged(this.mEditNote.mUUId);
                }
                if (this.mEditNote.mFirstRecord == null && !this.mEditNote.mFirstRecord.equals(this.mFirstRecord)) {
                    cv.put(Notes.FIRST_RECORD, this.mFirstRecord);
                } else if (this.mEditNote.mFirstRecord == null && this.mFirstRecord != null) {
                    cv.put(Notes.FIRST_RECORD, this.mFirstRecord);
                }
                cv.put(Notes.FILE_LIST, NoteUtil.getFileListString(fileList));
            }
            now = Long.valueOf(System.currentTimeMillis());
            if ((this.mChanged & -1048591) != 0) {
                cv.put(Notes.MODIFIED_DATE, now);
            }
            if ((this.mChanged & CHANGE_PAPER) != 0) {
                cv.put(Notes.PAPER, Integer.valueOf(this.mEditNote.mPaper));
            }
            if ((this.mChanged & CHANGE_FONT_COLOR) != 0) {
            }
            if ((this.mChanged & CHANGE_FONT_SIZE) != 0) {
                cv.put(Notes.FONT_SIZE, Integer.valueOf(this.mEditNote.mTextSize));
//                MobEventUtil.onSendMobEvent(this, "change_font_size", (String) null);
            }
            if ((this.mChanged & CHANGE_TAG) != 0) {
                cv.put(Notes.TAG, Long.valueOf(this.mEditNote.mTag));
                if (this.mEditNote.mTag > 0) {
//                    MobEventUtil.onSendMobEvent(this, "click_modifygroup", (String) null);
                }
            }
            if ((this.mChanged & REQUEST_CODE_EXPORT_TO_TEXT) != 0) {
                cv.put(Notes.TOP, Long.valueOf(this.mEditNote.mTopTime));
            }
            if ((this.mChanged & REQUEST_CODE_LOGIN) != 0) {
                cv.put(Notes.DESKTOP, Integer.valueOf(this.mEditNote.mDesktop));
            }
            if (TagData.FUN_ENCRYPT && (this.mChanged & CHANGE_ENCRYPT) != 0) {
                if (this.mEditNote.mEncrypt) {
                }
                cv.put(Notes.ENCRYPT, this.mEditNote.mEncrypt ? 1 : 0);
                //put魅族accountID，只有登录了魅族账户才能加密
*//*                if (this.mEditNote.mEncrypt) {
                    accountID = ((NoteApplication) getApplication()).getMeizuAccount();
                    if (accountID > 0) {
                        cv.put(Notes.ACCOUNT_ID, Long.valueOf(accountID));
                    } else {
                        Log.e(TAG, "Encrypt without flyme account login, this shouldn't happen!");
                    }
                } else {
                    cv.put(Notes.ACCOUNT_ID, Integer.valueOf(REQUEST_CODE_PICK));
                }*//*
            }
            //涉及NoteApplication
//            app = (NoteApplication) getApplication();
            changedList = new ArrayList();
            changedList.add(Integer.valueOf(this.mPosition));
//            app.setChangedData(REQUEST_CODE_EXPORT_TO_TEXT, changedList);
            getContentResolver().update(noteUri, cv, null, null);
            this.mEditNote.mFirstImg = this.mFirstImg;
            this.mEditNote.mFirstRecord = this.mFirstRecord;
        }
        //
        if (!this.mFloatFlag) {
            enterFloatingMode();
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mRecordingLayoutView != null) {
            this.mRecordingLayoutView.stopRecording(false);
            this.mRecordingLayoutView = null;
        }
        //涉及NoteApplication
//        ((NoteApplication) getApplication()).unregisterFloatChangedListener(null);

        if (TagData.FUN_ENCRYPT && this.mScreenOffAndHomeReceiver != null) {
            unregisterReceiver(this.mScreenOffAndHomeReceiver);
        }
        if (this.mIMEListener != null) {
            InputMethodManagerUtils.removeInputShownChangeListener(InputMethodManagerUtils.peekInstance(), this.mIMEListener);
            this.mIMEListener = null;
        }
        this.telephonyManager.listen(this.phoneStateListener, REQUEST_CODE_PICK);
        try {
            unregisterReceiver(this.mTimeChangedReceiver);
            this.mTimeChangedReceiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置mViewImageItem
    public void viewImage(RichFrameLayout view) {
        this.mViewImageItem = view;
    }

    @Override
    protected void onPause() {
        super.onPause();
        save();
        saveOnPageInfo();
    }

    //当前页面的名称
    String getCurrentPageName() {
        if (getIntent().getLongExtra("id", -1) == -1) {
            return "create";
        }
        if (this.mSoftInputShown) {
            return "editing";
        }
        return "preview";
    }

    //保存mLaunchTime = exitTime
    void saveOnPageInfo() {
        long exitTime = System.currentTimeMillis();
//        MobEventUtil.onMobPage(this, getCurrentPageName(), this.mLaunchTime, exitTime);
        this.mLaunchTime = exitTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mViewImageItem != null) {
            for (int index = this.mEditParent.getChildCount(); index >= 0; index--) {
                View view = this.mEditParent.getChildAt(index);
                if (view != null && "image".equals(view.getTag()) && (view instanceof RichFrameLayout) && !NoteUtil.getFile(((RichFrameLayout) view).getUUID(), ((RichFrameLayout) view).getFileName()).exists()) {
                    ((RichFrameLayout) view).deleteRichLayout();
                }
            }
            this.mViewImageItem = null;
        }
        this.mLaunchTime = System.currentTimeMillis();
    }

    //将mDataList转换为json字符串，便于存储在数据库中
    private String convert2JsonNoteText() {
        if (this.mDataList == null) {
            return null;
        }
        int size = this.mDataList.size();
        if (size == 0) {
            return null;
        }
        String jsonString = BuildConfig.VERSION_NAME;
        JSONArray ja = new JSONArray();
        int index = REQUEST_CODE_PICK;
        while (index < size) {
            try {
                NoteItem ni = (NoteItem) this.mDataList.get(index);
                JSONObject jo = new JSONObject();
                switch (ni.mState) {
                    case REQUEST_CODE_PICK /*0*/:
                    case REQUEST_CODE_EXPORT_TO_PIC /*1*/:
                    case REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                        NoteItemText nt = (NoteItemText) ni;
                        jo.put(NoteUtil.JSON_STATE, nt.mState);
                        jo.put(NoteUtil.JSON_TEXT, nt.mText);
                        jo.put(NoteUtil.NOTE_SPAN_TYPE, nt.mSpan);
                        ja.put(jo);
                        break;
                    case REQUEST_CODE_SHARING /*3*/:
                        NoteItemImage nii = (NoteItemImage) ni;
                        jo.put(NoteUtil.JSON_STATE, nii.mState);
                        jo.put(NoteUtil.JSON_IMAGE_HEIGHT, nii.mHeight);
                        jo.put(NoteUtil.JSON_IMAGE_WIDTH, nii.mWidth);
                        jo.put(NoteUtil.JSON_FILE_NAME, nii.mFileName);
                        ja.put(jo);
                        break;
                    case REQUEST_CODE_LOGIN /*4*/:
                        NoteItemRecord ntr = (NoteItemRecord) ni;
                        jo.put(NoteUtil.JSON_STATE, ntr.mState);
                        jo.put(NoteUtil.JSON_FILE_NAME, ntr.mFileName);
                        ja.put(jo);
                        break;
                    default:
                        break;
                }
                index += REQUEST_CODE_EXPORT_TO_PIC;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ja.toString();
    }

    //当点击添加图片按钮时，打开图片获取页面，选择图片后返回，在onActivityResult中处理
    void onInsertImage() {
        //隐藏键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), REQUEST_CODE_PICK);
        }

        findFocusView();
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(AccessibilityEventCompat.TYPE_GESTURE_DETECTION_END);
        intent.putExtra("output", TempFileProvider.SCRAP_CONTENT_URI);
        if (!TextUtils.isEmpty(sLastInsertDirPath)) {
            intent.putExtra("init_directory", sLastInsertDirPath);
        }
        Intent resultIntent = Intent.createChooser(intent, getResources().getText(R.string.insert_pic_title));
        startActivityForResult(resultIntent, REQUEST_CODE_PICK_CAPTURE);
    }


    public void TakePhotos() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), REQUEST_CODE_PICK);
        }
        findFocusView();
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("output", TempFileProvider.SCRAP_CONTENT_URI);
        if (!TextUtils.isEmpty(sLastInsertDirPath)) {
            intent.putExtra("init_directory", sLastInsertDirPath);
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_CAPTURE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    //创建OptionsMenu时
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        this.mMenuDelete = menu.findItem(R.id.menu_delete);
        this.mMenuPaper = menu.findItem(R.id.menu_change_paper);//纸张
        this.mMenuShare = menu.findItem(R.id.menu_share);//分享
        this.mMenuTop = menu.findItem(R.id.menu_top);//置顶
        if (!(this.mEditNote == null || this.mEditNote.mTopTime == 0)) {
            this.mMenuTop.setChecked(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    //是否锁定dir？？？
    boolean isLockDir(String dir) {
        if (dir == null || (!dir.equals(NoteUtil.SAFE_BOX_FILE_PATH) && !dir.startsWith(NoteUtil.SAFE_BOX_FILE_PATH + NoteUtil.RECORD_DIV))) {
            return false;
        }
        return true;
    }

    //根据result显示错误信息
    void showErrorTip(int result) {
        int strId = 0;
        switch (result) {
            case NoteUtil.EDIT_TYPE_UPDATE /*-5*/:
                strId = R.string.other_failure;
                break;
            case NoteUtil.EDIT_TYPE_CAMERA /*-4*/:
                strId = R.string.create_file_fail;
                break;
            case NoteUtil.EDIT_TYPE_RECORD /*-3*/:
                strId = R.string.save_failure;
                break;
            case NoteUtil.EDIT_TYPE_LIST /*-2*/:
                strId = R.string.file_not_available;
                break;
            case -1 /*-1*/:
                strId = R.string.space_not_enough;
                break;
        }
        if (strId != 0) {
            Toast.makeText(this, strId, Toast.LENGTH_SHORT).show();
        }
    }

    //把图片保存到文件夹中
    String addImage(Context context, Uri uri, String uuid) {
        //调用ImageUtil.getImageFile
        File file = ImageUtil.getImageFile(context, uri, uuid);
        if (file == null && !checkSdcardOK()) {
            return null;
        }
        //选择的图片所在的位置
        String path = uri.getPath();
        Log.d(TAG, "addImage: path" + path);
        if (path != null) {
            //将图片从原来的位置保存到新建的位置

            int result = 0;
            try {
                result = ImageUtil.saveIntoFile(context, uri, file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "addImage: result:" + result);
            //保存成功，返回新文件名
            if (result == 0) {
                Log.d(TAG, "addImage: file.getName();" + file.getName());
                return file.getName();
            }
            else return null;
        }
//        Log.d(TAG, "addImage: path != null 或 !path.equals(TempFileProvider.getScrapPath(context)");
//        File cameraFile = new File(path);
//        if (cameraFile.renameTo(file)) {
//            Log.d(TAG, "addImage: file.getName();" + file.getName());
//            return file.getName();
//        }
//        Log.d(TAG, "renameTo fail: from " + cameraFile.getPath() + " to :" + file.getPath());
        return null;
    }

    //当打开的Activity结束时
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            Log.d(TAG, "onActivityResult: requestCode==0");
            View view = findFocusView();
            //如果焦点View为空，或titleView获得焦点，设置titleView为这里的View
            if (view == null && this.mTitleView.hasFocus()) {
                view = this.mTitleView;
            }
            if (view != null) {
                if ((getWindow().getAttributes().softInputMode & REQUEST_CODE_LOGIN) == 0) {
                    getWindow().setSoftInputMode(20);
                }
                view.requestFocus();
                showSoftInput(view);
            }
        }

        if (resultCode == -1) {
            Log.d(TAG, "onActivityResult: resultCode==-1");
            Uri uri = null;
            switch (requestCode) {
                //加载图片的情况
                case REQUEST_CODE_PICK /*0*/:
                    if (data != null) {
                        uri = data.getData();
                        Log.d(TAG, "onActivityResult: uri:"+uri);
                    }
                    if (uri != null) {
                        //保存图片到引用文件夹
                        String picName = addImage(this, uri, this.mEditNote.mUUId);
                        if (picName != null) {
                            insertImage(picName); //插入图片元素到笔记上
                            break;
                        }
                        return;
                    }
                    return;
                case REQUEST_CODE_EXPORT_TO_PIC /*1*/:
                    Log.d(TAG, "onActivityResult: requestCode=1");
                    if (data != null) {
                        sLastExportPicDirPath = data.getData().getPath();
                        popupProgressDialog(R.string.exporting);
                        this.mUiHandler.postAtTime(new Runnable() {
                            public void run() {
                                new Thread(new Runnable() {
                                    public void run() {
                                        NoteEditActivity.this.exportToPic(NoteEditActivity.sLastExportPicDirPath);
                                        if (NoteEditActivity.this.isLockDir(NoteEditActivity.sLastExportPicDirPath)) {
                                            NoteEditActivity.sLastExportPicDirPath = null;
                                        }
                                        NoteEditActivity.this.mUiHandler.sendEmptyMessage(NoteEditActivity.REQUEST_CODE_EXPORT_TO_TEXT);
                                    }
                                }).start();
                            }
                        }, 20);
                        break;
                    }
                    break;
                case REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                    Log.d(TAG, "onActivityResult: requestCode=2");
                    if (data != null) {
                        sLastExportTextDirPath = data.getData().getPath();
                        popupProgressDialog(R.string.exporting);
                        new Thread(new Runnable() {
                            public void run() {
                                NoteEditActivity.this.exportToText(NoteEditActivity.sLastExportTextDirPath);
                                if (NoteEditActivity.this.isLockDir(NoteEditActivity.sLastExportTextDirPath)) {
                                    NoteEditActivity.sLastExportTextDirPath = null;
                                }
                                NoteEditActivity.this.mUiHandler.sendEmptyMessage(NoteEditActivity.REQUEST_CODE_EXPORT_TO_TEXT);
                            }
                        }).start();
                        break;
                    }
                    break;
                case REQUEST_CODE_LOGIN /*4*/:
                case REQUEST_CODE_VERIFY /*5*/:
                    this.mChanged |= CHANGE_ENCRYPT;
                    this.mChanged |= REQUEST_CODE_LOGIN;
                    break;
                case REQUEST_CODE_PICK_CAPTURE/*6*/:
                    Log.d(TAG, "onActivityResult: uri:"+TempFileProvider.SCRAP_CONTENT_URI);
                    String picName = addImage(this, TempFileProvider.SCRAP_CONTENT_URI, this.mEditNote.mUUId);
                    if (picName != null) {
                        insertImage(picName); //插入图片元素到笔记上
                        break;
                    }
                    break;
                case 99:
                    Log.d(TAG, "onActivityResult: "+99);
                    Log.d(TAG, "onActivityResult: "+((NoteAppImpl)getApplication()).mSelectLabelIds.size());
                    refreshLabel(((NoteAppImpl)getApplication()).mSelectLabelIds);
                    showLabel();
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_CODE_LOGIN || requestCode == REQUEST_CODE_VERIFY) {
            this.mRequestCode = -1;
        }
    }


    private void refreshLabel(ArrayList<Integer> ids) {
        ArrayList<Integer> currLabels = this.mLabel;
        currLabels.clear();
        currLabels.addAll(ids);
    }

    private void showLabel() {
        if (mLabel.size()==0){
            this.mLabelContent.setVisibility(View.INVISIBLE);
        }
        this.mLabelContent.setVisibility(View.VISIBLE);
        this.mLabelContent.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        ArrayList<Integer> labels = this.mLabel;
        ArrayList<Integer> invalidLabels = new ArrayList<>();
        Iterator i$ = labels.iterator();
        while (i$.hasNext()) {
            Integer id = (Integer) i$.next();
            String label = getLabelContentById(id);
            if (TextUtils.isEmpty(label)) {
                invalidLabels.add(id);
            } else {
                TextView view = (TextView) inflater.inflate(R.layout.edit_page_label_item, null);
                view.setText(label);
                this.mLabelContent.addView(view);
            }
        }
        if (invalidLabels.size() > 0) {
            labels.removeAll(invalidLabels);
        }
        if (this.mLabelContent.getChildCount() > 0) {
            this.mLabelView.setVisibility(View.VISIBLE);/*0*/
        } else {
            this.mLabelView.setVisibility(View.GONE);/*8*/
        }
    }


    public String getLabelContentById(int id) {
        Iterator i$ = ((NoteAppImpl)this.getApplication()).mlabels.iterator();
        while (i$.hasNext()) {
            LabelCustomActivity.LabelHolder holder = (LabelCustomActivity.LabelHolder) i$.next();
            if (holder.mId == id) {
                return holder.mContent;
            }
        }
        return null;
    }


    //在数据库中插入File
    public void insertFileInDataBase(final String uuid, final String name, final int type) {
//        new Thread(new Runnable() {
//            public void run() {
//                ContentValues value = new ContentValues();
//                value.put(NoteUtil.JSON_FILE_NAME, name);
//                value.put(NoteFiles.NOTE_UUID, uuid);
//                File file = NoteUtil.getFile(uuid, name);
//                //调用的函数被删除了
////                value.put(NoteFiles.MD5, NoteUtil.md5sum(file.getPath()));
//                value.put(NoteUtil.JSON_MTIME, Long.valueOf(file.lastModified()));
//                value.put(Constants.JSON_KEY_TYPE, Integer.valueOf(type));
//                //Unknown URL content://com.meizu.flyme.notepaper.NotePaper/notefiles
//                NoteEditActivity.this.getContentResolver().insert(NoteFiles.CONTENT_URI, value);
//            }
//        }).start();
    }

    //从数据库中删除file
    public void deleteFileInDataBase(final String uuid, final String name) {
        new Thread(new Runnable() {
            public void run() {

//                NoteEditActivity.this.getContentResolver().delete(NoteFiles.CONTENT_URI, "name = \"" + name + "\"" + " and " + NoteFiles.NOTE_UUID + " = \"" + uuid + "\"", null);
            }
        }).start();
    }

    public void insertInRichItem(View view, CharSequence s) {
        boolean appendNew = true;
        NoteEditText noteEditText = null;
        int childCount = this.mEditParent.getChildCount();
        int position = -1;
        for (int index = 0; index < childCount; index += 1) {
            if (this.mEditParent.getChildAt(index) == view) {
                position = index;
                break;
            }
        }
        if (position == -1) {
            Log.d(TAG, "position is not found");
            return;
        }
        if (position + 1 < childCount) {
            View next = this.mEditParent.getChildAt(position + 1);
            if (NoteUtil.JSON_TEXT.equals(next.getTag())) {
                appendNew = false;
                noteEditText = (NoteEditText) next.findViewById(R.id.text);
            }
        }
        if (appendNew) {
            View item = getLayoutInflater().inflate(R.layout.edit_textlist_item, null);
            this.mEditParent.addView(item, position + 1);
            noteEditText = (NoteEditText) item.findViewById(R.id.text);
        }
        SpannableStringBuilder sb = (SpannableStringBuilder) noteEditText.getText();
        int end = REQUEST_CODE_PICK;
        if (!(s == null || s.equals("\n"))) {
            sb.insert(REQUEST_CODE_PICK, s.toString());
            end = s.length();
        }
        noteEditText.requestFocus();
        Selection.setSelection(noteEditText.getText(), end);
        showSoftInput(noteEditText);

        setFirstHint();
        this.mChanged |= CHANGE_CONTENT;
    }

    //在某个positon插入图片
    public void insertPictureAtPos(String name, int position, boolean appendNew) {
        insertFileInDataBase(this.mEditNote.mUUId, name, REQUEST_CODE_PICK);
        LayoutInflater inflater = getLayoutInflater();
        RichFrameLayout imageParent = (RichFrameLayout) inflater.inflate(R.layout.edit_image, null);
        imageParent.setUUIDandName(this.mEditNote.mUUId, name);
        this.mEditParent.addView(imageParent, position);

        if (appendNew) {
            View item = inflater.inflate(R.layout.edit_textlist_item, null);
            NoteEditText newText = (NoteEditText) item.findViewById(R.id.text);
            newText.setTextSize((float) (this.mEditNote.mTextSize > 0 ? this.mEditNote.mTextSize : NoteData.DEFAULT_FONT_SIZE));
            this.mEditParent.addView(item, position + REQUEST_CODE_EXPORT_TO_PIC);
            newText.requestFocus();
            Selection.setSelection(newText.getText(), REQUEST_CODE_PICK);
            showSoftInput(newText);

        } else {
            View current = this.mEditParent.getChildAt(position + REQUEST_CODE_EXPORT_TO_PIC);
            if (NoteUtil.JSON_TEXT.equals(current.getTag())) {
                NoteEditText edit = (NoteEditText) current.findViewById(R.id.text);
                edit.requestFocus();
                Selection.setSelection(edit.getText(), REQUEST_CODE_PICK);
                showSoftInput(edit);
            }
        }
        setFirstHint();
    }

    //删除最后一个字符？？吗
    void deleteLastLineFeedChar(Editable edit) {
        if (edit != null) {
            int length = edit.length();
            if (length > 0 && edit.charAt(length - 1) == '\n') {
                edit.delete(length - 1, length);
            }
        }
    }

    //遍历mEditParent查询child的position
    int getChildPosition(View child) {
        int count = this.mEditParent.getChildCount();
        for (int i = 0; i < count; i += 1) {
            if (this.mEditParent.getChildAt(i) == child) {
                return i;
            }
        }
        return -1;
    }

    //插入图片，在focus的地方，好像很难
    void insertImage(String picName) {
        View view = findFocusView();
        int type;
        int position;
        if (this.mFocusNoteEditText != null) {
            type = ((CheckImageView) ((ViewGroup) this.mFocusNoteEditText.getParent()).findViewById(R.id.check)).getImageType();
            position = getChildPosition((View) this.mFocusNoteEditText.getParent());
            SpannableStringBuilder sb = (SpannableStringBuilder) this.mFocusNoteEditText.getText();
            int end = Selection.getSelectionEnd(sb);
            int length = sb.length();
            if (end == 0) {
                insertPictureAtPos(picName, position, false);
            } else if (end == length) {
                deleteLastLineFeedChar(sb);
                insertPictureAtPos(picName, position + 1, true);
            } else {
                int newstart = end;
                if (sb.charAt(end) == '\n') {
                    newstart += REQUEST_CODE_EXPORT_TO_PIC;
                }
                CharSequence cutText = sb.subSequence(newstart, length);
                sb.delete(end, length);
                deleteLastLineFeedChar(sb);
                insertPictureAtPos(picName, position + 1, true);
                View item = this.mEditParent.getChildAt(position + 2);
                CheckImageView check = (CheckImageView) item.findViewById(R.id.check);
                TextView neText = (TextView) item.findViewById(R.id.text);
                DeleteImageView deleteView = (DeleteImageView) item.findViewById(R.id.delete);
                switch (type) {
                    case REQUEST_CODE_PICK /*0*/:
                        check.setImageType(type);
                        deleteView.setVisibility(View.GONE);
                        break;
                    case REQUEST_CODE_EXPORT_TO_PIC /*1*/:
                        check.setImageType(type);
                        setEditStrikeThrough((TextView) neText, false);
                        deleteView.setVisibility(View.GONE);
                        break;
                    case REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                        check.setImageType(type);
                        setEditStrikeThrough((TextView) neText, true);
                        deleteView.setVisibility(View.GONE);
                        break;
                }
                neText.setText(cutText);
                Selection.setSelection((Spannable) neText.getText(), 0);
                neText.requestFocus();
                showSoftInput(neText);
                //mRestoreSwitch打开

            }
        } else if (this.mTitleView.hasFocus()) {
            insertPictureAtPos(picName, 0, false);
        } else if (view == null || !(view instanceof RichFrameLayout)) {
            int childCount = this.mEditParent.getChildCount();
            if (childCount > 0) {
                position = childCount - 1;
                View last = this.mEditParent.getChildAt(position);
                if (NoteUtil.JSON_TEXT.equals(last.getTag())) {
                    NoteEditText edit = (NoteEditText) last.findViewById(R.id.text);
                    type = ((CheckImageView) last.findViewById(R.id.check)).getImageType();
                    Editable text = edit.getText();
                    if (text.length() == 0 && type == 0) {
                        insertPictureAtPos(picName, position, false);
                    } else {
                        deleteLastLineFeedChar(text);
                        insertPictureAtPos(picName, position + 1, true);
                    }
                } else {
                    insertPictureAtPos(picName, position + 1, true);
                }
            }
        } else {
            insertPictureAtPos(picName, getChildPosition(view) + 1, false);
        }
        setFirstHint();
        this.mChanged |= CHANGE_CONTENT;
        scanNoteDir();
    }

    //扫描NoteDir
    void scanNoteDir() {
        File parent = new File(NoteUtil.FILES_DIR, this.mEditNote.mUUId);
        if (parent.exists()) {
            sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(parent)));
        }
    }

    //改变字体大小时，遍历更改所有标签为String的文字控件的字体大小
    public void onFontChanged(int fontSize) {
        this.mEditNote.mTextSize = fontSize;
        this.mChanged |= CHANGE_FONT_SIZE;
        for (int i = 0; i < this.mEditParent.getChildCount(); i += 1) {
            View view = this.mEditParent.getChildAt(i);
            if (NoteUtil.JSON_TEXT.equals((String) view.getTag())) {
                ((NoteEditText) view.findViewById(R.id.text)).setTextSize((float) fontSize);
            }
        }
    }

    //背景改变？？
    public void onBackgroundChanged(int index) {
        if (this.mEditNote.mPaper != index) {
            this.mEditNote.mPaper = index;
            View view = this.mScrollView.findViewById(R.id.frame_parent);
            getWindow().setBackgroundDrawable(new ColorDrawable(NoteUtil.getBackgroundColor(this.mEditNote.mPaper)));
            this.mChanged |= CHANGE_PAPER;
            setTopBlurEffect();
        }
    }

    //点击背景按钮？？
    void onBackgroundMenuClick() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), REQUEST_CODE_PICK);
        }
        PopupPaperWindow popup = new PopupPaperWindow(this);
        popup.setTouchable(true);
        popup.setFocusable(true);
        popup.setClippingEnabled(true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(REQUEST_CODE_PICK));
        popup.setWindowLayoutMode(-1, -2);
        popup.setInputMethodMode(REQUEST_CODE_EXPORT_TO_TEXT);
        View parent = getLayoutInflater().inflate(R.layout.background_panel, null);
        popup.setContentView(parent);
        ((HorizontalBackgoundView) parent.findViewById(R.id.scroll_parent)).setFocusBackground(this.mEditNote.mPaper);
        ((FontPanelLinearLayout) parent.findViewById(R.id.font_panel)).setFontSize(this.mEditNote.mTextSize);
        popup.setPopupStateChangeListener(new OnPopupStateChangeListener() {
            public void onPopup() {
                NoteEditActivity.this.setBottomBlurEffect(true);
            }

            public void onPopDown() {
                NoteEditActivity.this.setBottomBlurEffect(false);
            }
        });
        popup.setAnimateView(parent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int footHeight = getResources().getDimensionPixelSize(R.dimen.mz_action_button_min_height);
        popup.showAtLocation(parent, REQUEST_CODE_PICK, REQUEST_CODE_PICK, (((dm.heightPixels - footHeight) - getResources().getDimensionPixelSize(R.dimen.font_color_panel_height)) - parent.getPaddingTop()) - 1);
    }

    //实现删除动作的函数，当点击菜单中的删除时，在对话框中确认后执行
    //需要NoteApplication，这里先不关注这个，注释了
    void onDeleteAction() {
/*        this.mPauseState = REQUEST_CODE_SHARING;
        if (this.mEditNote.mId != -1) {
            NoteApplication app = (NoteApplication) getApplication();
            if (this.mPosition != -1) {
                ArrayList<Integer> changedList = new ArrayList();
                changedList.add(Integer.valueOf(this.mPosition));
                app.setChangedData(REQUEST_CODE_SHARING, changedList);
            }
            NotePaperActivity npa = app.getNotePaperActivity();
            if (npa != null) {
                npa.setDeleteItemId(this.mEditNote.mId);
            }
            getContentResolver().delete(ContentUris.withAppendedId(Notes.CONTENT_URI, this.mEditNote.mId), null, null);
        } else {
            getContentResolver().delete(NoteFiles.CONTENT_URI, "note_uuid = \"" + this.mEditNote.mUUId + "\"", null);
            File file = new File(NoteUtil.FILES_DIR, this.mEditNote.mUUId);
            if (file.exists()) {
                NoteUtil.deleteFile(file);
            }
        }
        finish();*/
    }

    //判断是否为空，包括检查mTitleView和mEditParent
    boolean checkEmpty() {
        Editable title = this.mTitleView.getText();
        if (title != null && title.length() > 0) {
            return false;
        }
        int childCount = this.mEditParent.getChildCount();
        for (int index = REQUEST_CODE_PICK; index < childCount; index += REQUEST_CODE_EXPORT_TO_PIC) {
            View view = this.mEditParent.getChildAt(index);
            String tag = (String) view.getTag();
            if ("record".equals(tag) || "image".equals(tag)) {
                return false;
            }
            if (NoteUtil.JSON_TEXT.equals(tag)) {
                NoteEditText edit = (NoteEditText) view.findViewById(R.id.text);
                if (edit.getText() != null && edit.getText().length() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    //使用向上弹出菜单删除
    private void onDeleteBrowseMenuClicked() {
        if (checkEmpty()) {
            onDeleteAction();
            return;
        }
        PopupMenu menu = new PopupMenu(this, findViewById(R.id.menu_delete_browse));
        menu.getMenuInflater().inflate(R.menu.menu_delete_popupmenu, menu.getMenu());
        menu.getMenu().findItem(R.id.popup_delete_item).setTitle(getString(R.string.delete_tip));
        menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.popup_delete_item) {
                    NoteEditActivity.this.mUiHandler.post(new Runnable() {
                        public void run() {
                            //执行删除动作
                            NoteEditActivity.this.onDeleteAction();
                        }
                    });
                }
                return false;
            }
        });
        menu.show();
    }

    //使用删除对话框
    private void onDeleteMenuClicked() {
        if (checkEmpty()) {
            onDeleteAction();
            return;
        }
        DialogInterface.OnClickListener confirmDeleteListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //调用删除函数
                NoteEditActivity.this.onDeleteAction();
            }
        };
        DialogInterface.OnClickListener cancleDeleteListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        Builder dlg = new Builder(this);
        dlg.setIcon(R.drawable.mz_ic_popup_delete);
        dlg.setTitle(getString(R.string.delete_one_tip));
        dlg.setPositiveButton(getString(R.string.ok), confirmDeleteListener);
        dlg.setNegativeButton(getString(R.string.cancel), null);
        dlg.show().setCanceledOnTouchOutside(true);
    }

    //
    void createSharePicture(String filename) {
        LayoutInflater inflater = getLayoutInflater();
        //加载picture_share布局
        View view = inflater.inflate(R.layout.picture_share, null);
        //得到picture_share布局的title
        TextView title = (TextView) view.findViewById(R.id.title);
        //将mTitleView的内容转换为title_txt
        String title_txt = this.mTitleView.getText().toString();
        if (TextUtils.isEmpty(title_txt)) {
            //如果title_txt是空的
            title.setVisibility(View.GONE);
            view.findViewById(R.id.divider1).setVisibility(View.GONE);
        } else {
            //如果title_txt不是空的
            title.setText(title_txt);
        }
        //上面处理了名称，下面处理内容
        //得到picture_share布局的parent，下面还要加载share_item_text布局share_item_image布局
        ViewGroup parent = (LinearLayout) view.findViewById(R.id.parent);
        //孩子总数
        int childCount = this.mEditParent.getChildCount();
        //遍历mEditParent
        for (int index = 0; index < childCount; index += 1) {
            //得到孩子View
            View child = this.mEditParent.getChildAt(index);
            //得到孩子View的tag
            String tag = (String) child.getTag();
            if (!("record".equals(tag) || "recording".equals(tag))) {
                //不是录音的情况
                if (NoteUtil.JSON_TEXT.equals(tag)) {
                    //不是录音的情况下，是文字的情况
                    //得到NoteEditText edit
                    NoteEditText edit = (NoteEditText) child.findViewById(R.id.text);
                    //得到CheckImageView check
                    CheckImageView check = (CheckImageView) child.findViewById(R.id.check);

                    if (index != childCount - 1 || !TextUtils.isEmpty(edit.getText())) {
                        //不是最后一个孩子，或者得到NoteEditText edit内容不为空

                        //加载share_item_text布局，到parent上
                        inflater.inflate(R.layout.share_item_text, parent);
                        //得到刚加载的布局
                        View text_child = parent.getChildAt(parent.getChildCount() - 1);
                        //得到布局中的TextView
                        TextView tc = (TextView) text_child.findViewById(R.id.text);
                        //为TextView tc 设置内容
                        tc.setText(edit.getText());
                        //设置share_item_text布局的CheckImageView
                        ((CheckImageView) text_child.findViewById(R.id.check)).setShareImageType(check.getImageType());
                        if (check.getImageType() == 2) {
                            //全文字，忽略CheckImageView
                            setEditStrikeThrough(tc, true);
                        }
                    }
                } else if ("image".equals(tag)) {
                    //不是录音的情况下，是图片的情况
                    ScaleImageView image = (ScaleImageView) child.findViewById(R.id.image);
                    inflater.inflate(R.layout.share_item_image, parent);
                    ((ScaleImageView) parent.getChildAt(parent.getChildCount() - 1)).setUUIDandName(this.mEditNote.mUUId, image.mFileName);
                }
            }
        }
        //下面是保存为图片，不懂
        view.measure(MeasureSpec.makeMeasureSpec(IMAGE_WIDTH, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(REQUEST_CODE_PICK, REQUEST_CODE_PICK));
        int w = view.getMeasuredWidth();
        int h = view.getMeasuredHeight();
        view.layout(REQUEST_CODE_PICK, REQUEST_CODE_PICK, w, h);
        Bitmap src = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        src.eraseColor(NoteUtil.getBackgroundColor(this.mEditNote.mPaper));
        Canvas canvas = new Canvas(src);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(REQUEST_CODE_PICK, REQUEST_CODE_SHARING));
        view.draw(canvas);
        ImageUtil.saveBitmap2file(src, filename);
        src.recycle();
    }

    //点击分享时，根据type判断分享类型
    void onShareMenuAction(int type) {
        this.mShareIntent = new Intent();
        this.mShareIntent.addFlags(524289);
        if (type == 0) {
            File parent = EnvironmentUtils.buildExternalStorageAppCacheDirs(Config.PACKAGE_NAME)[REQUEST_CODE_PICK];
            if (!(parent.exists() || parent.mkdirs())) {
                Log.d(TAG, "mkdirs fail: " + parent.getPath());
                if (!new File(parent.getPath()).exists()) {
                    return;
                }
            }
            File file = new File(parent, "note_sharing.png");
            createSharePicture(file.getPath());
            this.mShareIntent.setAction("android.intent.action.SEND");
            this.mShareIntent.setType("image/*");
            this.mShareIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
            return;
        }
        int index;
        this.mShareIntent.putExtra("format_txt", true);
        ArrayList<String> picList = new ArrayList();
        ArrayList<String> recordList = new ArrayList();
        int childCount = this.mEditParent.getChildCount();
        for (index = REQUEST_CODE_PICK; index < childCount; index += REQUEST_CODE_EXPORT_TO_PIC) {
            NoteItem nt = new NoteItem();
            View view = this.mEditParent.getChildAt(index);
            String tag = (String) view.getTag();
            if ("record".equals(tag)) {
                recordList.add(((RichFrameLayout) view).getFileName());
            } else if ("image".equals(tag)) {
                picList.add(((RichFrameLayout) view).getFileName());
            }
        }
        int picSize = picList.size();
        int recordSize = recordList.size();
        int size = picSize + recordSize;
        String txt = exportToString();
        if (txt != null && txt.length() > 0) {
            this.mShareIntent.putExtra("android.intent.extra.TEXT", txt);
        }
        if (size == REQUEST_CODE_EXPORT_TO_PIC) {
            Uri uri;
            this.mShareIntent.setAction("android.intent.action.SEND");
            if (picSize == REQUEST_CODE_EXPORT_TO_PIC) {
                this.mShareIntent.setType("image/*");
                uri = Uri.fromFile(NoteUtil.getFile(this.mEditNote.mUUId, (String) picList.get(REQUEST_CODE_PICK)));
            } else {
                this.mShareIntent.setType("audio/*");
                uri = Uri.fromFile(NoteUtil.getFile(this.mEditNote.mUUId, (String) recordList.get(REQUEST_CODE_PICK)));
            }
            this.mShareIntent.putExtra("android.intent.extra.STREAM", uri);
        } else if (size > REQUEST_CODE_EXPORT_TO_PIC) {
            this.mShareIntent.setAction("android.intent.action.SEND_MULTIPLE");
            if (picSize == 0) {
                this.mShareIntent.setType("audio/*");
            } else if (recordSize == 0) {
                this.mShareIntent.setType("image/*");
            } else {
                this.mShareIntent.setType("*/*");
            }
            ArrayList<Parcelable> parcelableList = new ArrayList();
            for (index = REQUEST_CODE_PICK; index < picSize; index += REQUEST_CODE_EXPORT_TO_PIC) {
                parcelableList.add(Uri.fromFile(NoteUtil.getFile(this.mEditNote.mUUId, (String) picList.get(index))));
            }
            for (index = REQUEST_CODE_PICK; index < recordSize; index += REQUEST_CODE_EXPORT_TO_PIC) {
                parcelableList.add(Uri.fromFile(NoteUtil.getFile(this.mEditNote.mUUId, (String) recordList.get(index))));
            }
            this.mShareIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", parcelableList);
        } else {
            this.mShareIntent.setAction("android.intent.action.SEND");
            this.mShareIntent.setType("text/plain");
        }
    }

    //弹出ProgressDialog
    void popupProgressDialog(int stringId) {
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(getString(stringId));
        this.mProgressDialog.show();
    }

    //取消ProgressDialog
    void dismissProgressDialog() {
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    //???
    public boolean getCaptureState() {
        return this.mIsCapture;
    }

    //导出为pic文件
    void exportToPicFile(File file, boolean attachMZFlag, int scaled) {
        int height;
        int attachHeight = 100;
        boolean drawTitle = true;
        if (this.mTitleView.getText() == null || this.mTitleView.getText().length() == 0) {
            drawTitle = false;
        }
        View view = this.mScrollView.findViewById(R.id.frame_parent);
        int width = view.getWidth();
        if (drawTitle) {
            height = this.mEditParent.getBottom();
        } else {
            view = this.mEditParent;
            height = this.mEditParent.getHeight();
        }
        if (width != 0 && height != 0) {
            if (height > 5600) {
                height = 5600;
                attachHeight = 100 + 40;
            }
            int editHeight = this.mEditParent.getHeight();
            View lastView = this.mEditParent.getChildAt(this.mEditParent.getChildCount() - 1);
            if (NoteUtil.JSON_TEXT.equals((String) lastView.getTag())) {
                NoteEditText edit = (NoteEditText) lastView.findViewById(R.id.text);
                int type = ((CheckImageView) lastView.findViewById(R.id.check)).getImageType();
                if (edit.getText().length() == 0 && type == 0) {
                    height -= lastView.getHeight();
                }
            }
            if (lastView.getBottom() < editHeight - this.mEditParent.getPaddingBottom()) {
                height -= (editHeight - lastView.getBottom()) - 32;
            }
            if (attachMZFlag) {
                height += attachHeight;
            }
            float scale = scaled == 0 ? 1.0F : 0.5f;
            Bitmap src = Bitmap.createBitmap((int) (((float) width) * scale), (int) (((float) height) * scale), Bitmap.Config.ARGB_8888);
            src.eraseColor(NoteUtil.getBackgroundColor(this.mEditNote.mPaper));
            Canvas canvas = new Canvas(src);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(REQUEST_CODE_PICK, REQUEST_CODE_SHARING));
            if (scale != 1.0F) {
                canvas.scale(scale, scale);
            }
            if (attachMZFlag) {
                canvas.save();
                canvas.clipRect(new Rect(REQUEST_CODE_PICK, REQUEST_CODE_PICK, width, height - attachHeight));
            }
            this.mIsCapture = true;
            view.draw(canvas);
            if (attachMZFlag) {
                canvas.restore();
                ImageView divider = (ImageView) this.mScrollView.findViewById(R.id.divider);
                BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.divider);
                canvas.translate((float) ((width - divider.getWidth()) / REQUEST_CODE_EXPORT_TO_TEXT), (float) ((attachHeight > 100 ? attachHeight - 100 : REQUEST_CODE_PICK) + (height - attachHeight)));
                bd.setBounds(new Rect(REQUEST_CODE_PICK, REQUEST_CODE_PICK, divider.getWidth(), divider.getHeight()));
                bd.draw(canvas);
                Paint paint = new Paint(REQUEST_CODE_EXPORT_TO_PIC);
                paint.setTextSize(40.0f);
                paint.setColor(ViewCompat.MEASURED_STATE_MASK);
                canvas.drawText(getResources().getString(R.string.share_tail), 0.0f, 60.0f, paint);
            }
            ImageUtil.saveBitmap2file(src, file.getPath());
            src.recycle();
            this.mIsCapture = false;
        }
    }

    //导出为pic
    void exportToPic(String parent) {
        File file = new File(parent, NoteUtil.getOutputName("jpg"));
        exportToPicFile(file, false, REQUEST_CODE_PICK);
        File parentfile = file.getParentFile();
        if (parentfile.exists()) {
            sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(parentfile)));
        }
    }

    //导出为String
    String exportToString() {
        String export = BuildConfig.VERSION_NAME;
        int childCount = this.mEditParent.getChildCount();
        if (this.mTitleView.getText() != null && this.mTitleView.getText().length() > 0) {
            export = export + this.mTitleView.getText() + "\n";
        }
        for (int index = REQUEST_CODE_PICK; index < childCount; index += REQUEST_CODE_EXPORT_TO_PIC) {
            NoteItem nt = new NoteItem();
            View view = this.mEditParent.getChildAt(index);
            if (NoteUtil.JSON_TEXT.equals((String) view.getTag())) {
                NoteEditText edit = (NoteEditText) view.findViewById(R.id.text);
                String item = edit.getText() == null ? BuildConfig.VERSION_NAME : edit.getText().toString();
                if (!(item == null || item.length() == 0)) {
                    export = export + item + "\n";
                }
            }
        }
        if (export.length() > 0) {
            return export.substring(REQUEST_CODE_PICK, export.length() - 1);
        }
        return export;
    }

    //导出为文本文件？？ 有严重问题，注释了
    void exportToText(String parent) {

    }

    //内容是否为空
    boolean isEmptyContent() {
        int childCount = this.mEditParent.getChildCount();
        for (int index = 0; index < childCount; index += 1) {
            View view = this.mEditParent.getChildAt(index);
            String tag = (String) view.getTag();
            if ("record".equals(tag)) {
                return false;
            }
            if ("recording".equals(tag)) {
                return false;
            }
            if (NoteUtil.JSON_TEXT.equals(tag)) {
                NoteEditText edit = (NoteEditText) view.findViewById(R.id.text);
                if (edit.getText() != null && edit.getText().length() > 0) {
                    return false;
                }
            } else if ("image".equals(tag)) {
                return false;
            }
        }
        return true;
    }

    void exportTo(int requestCode) {
        String dir;
        Intent intent = new Intent();
        intent.setAction("com.meizu.action.SAVE_FILE");
        if (requestCode == REQUEST_CODE_EXPORT_TO_PIC) {
            dir = sLastExportPicDirPath;
        } else {
            dir = sLastExportTextDirPath;
        }
        if (!TextUtils.isEmpty(dir)) {
            intent.putExtra("init_directory", dir);
        }
        startActivityForResult(intent, requestCode);
    }

    //在某个position插入录音，并指定是否附加一个文字元素
    void insertRecordingAtPos(int position, boolean appendNew) {
        LayoutInflater inflater = getLayoutInflater();
        //加载RecordingLayout
        RecordingLayout parent = (RecordingLayout) inflater.inflate(R.layout.edit_recording, null);
        //赋值给mRecordingLayoutView
        this.mRecordingLayoutView = parent;
        //设置uuid，为笔记的uuid
        parent.setUUID(this.mEditNote.mUUId);
        //开始录音
        parent.startRecord();
        //设置显示
        this.mRecordingLayoutView.setVisibility(View.GONE);
        //加入到mEditParent容器中
        this.mEditParent.addView(parent, position);
        //如果有撤销功能

        if (appendNew) {
            View item = inflater.inflate(R.layout.edit_textlist_item, null);
            NoteEditText newText = (NoteEditText) item.findViewById(R.id.text);
            newText.setTextSize((float) (this.mEditNote.mTextSize > 0 ? this.mEditNote.mTextSize : NoteData.DEFAULT_FONT_SIZE));
            this.mEditParent.addView(item, position + REQUEST_CODE_EXPORT_TO_PIC);
            newText.requestFocus();
            Selection.setSelection(newText.getText(), REQUEST_CODE_PICK);
            showSoftInput(newText);
            //如果启用了撤销功能

        }
        setFirstHint();
    }

    //在menu中选择录音调用
    void onRecord() {
        View view = findFocusView();
        int type;
        int position;
        //若当前光标在NoteEditText中
        if (this.mFocusNoteEditText != null) {
            //当前NoteEditText前面选择按钮的选中类型,选中或没选中
            type = ((CheckImageView) ((ViewGroup) this.mFocusNoteEditText.getParent()).findViewById(R.id.check)).getImageType();
            //文字元素在父类中的位置
            position = getChildPosition((View) this.mFocusNoteEditText.getParent());
            //用当前文字元素的文字创建SpannableStringBuilder
            SpannableStringBuilder sb = (SpannableStringBuilder) this.mFocusNoteEditText.getText();

            //从sb中得到end，好像是到光标的长度
            int end = Selection.getSelectionEnd(sb);
            //从sb中获得长度
            int length = sb.length();
            //没有内容，或光标在开头
            if (end == 0) {
                //插在最前面
                insertRecordingAtPos(position, false);
            } else if (end == length) {
                //光标在文字最后
                //删除最后一行最后一个字符？？？
                deleteLastLineFeedChar(sb);
                //插入到下一个位置
                insertRecordingAtPos(position + 1, true);
            } else {
                //新的开始
                int newstart = end;
                if (sb.charAt(end) == '\n') {
                    newstart += 1;
                }
                //取得后半部分的文本
                CharSequence cutText = sb.subSequence(newstart, length);
                //删除sb里的后半部分
                sb.delete(end, length);
                //删除换行符号后的补位feed
                deleteLastLineFeedChar(sb);
                //插入到文本元素的下面，并在后面添加一个新文字元素
                insertRecordingAtPos(position + 1, true);
                //得到新文字元素
                View item = this.mEditParent.getChildAt(position + 2);
                //得到新文字元素中的控件
                CheckImageView check = (CheckImageView) item.findViewById(R.id.check);
                NoteEditText neText = (NoteEditText) item.findViewById(R.id.text);
                //设置文字元素的内容，是刚刚截取的后半部分
                neText.setText(cutText);
                DeleteImageView deleteView = (DeleteImageView) item.findViewById(R.id.delete);
                //设置文本元素中控件，是否显示，根据type来判断，每种type对应一套显示方式
                switch (type) {
                    case REQUEST_CODE_PICK /*0*/:
                        check.setImageType(type);
                        deleteView.setVisibility(View.GONE);
                        break;
                    case REQUEST_CODE_EXPORT_TO_PIC /*1*/:
                        check.setImageType(type);
                        setEditStrikeThrough(neText, false);
                        deleteView.setVisibility(View.GONE);
                        break;
                    case REQUEST_CODE_EXPORT_TO_TEXT /*2*/:
                        check.setImageType(type);
                        setEditStrikeThrough(neText, true);
                        deleteView.setVisibility(View.GONE);
                        break;
                }
                //获取光标
                neText.requestFocus();
                //设置光标到0位置
                Selection.setSelection(neText.getText(), 0);
                //显示键盘
                showSoftInput(neText);
            }
        }
        //光标在titleView时，直接插入到最前面，即位置0
        else if (this.mTitleView.hasFocus()) {
            insertRecordingAtPos(REQUEST_CODE_PICK, false);
        }
        //如果光标不在任何一个笔记元素上
        else if (view == null || !(view instanceof RichFrameLayout)) {
            int childCount = this.mEditParent.getChildCount();
            if (childCount > 0) {
                position = childCount - 1;
                //得到最后一个笔记元素
                View last = this.mEditParent.getChildAt(position);
                //如果最后一个笔记元素是文字元素
                if (NoteUtil.JSON_TEXT.equals(last.getTag())) {
                    NoteEditText edit = (NoteEditText) last.findViewById(R.id.text);
                    //得到check的类型
                    type = ((CheckImageView) last.findViewById(R.id.check)).getImageType();
                    //得到内容
                    Editable text = edit.getText();
                    //如果不是清单，也没有内容，就直接插在这个位置，将原来的笔记元素挤到后面
                    if (text.length() == 0 && type == 0) {
                        insertRecordingAtPos(position, false);
                    } else {
                        //插入下一个位置，并接一个空的文字元素
                        deleteLastLineFeedChar(text);
                        insertRecordingAtPos(position + 1, true);
                    }
                }
                //如果不是文字元素，插在最后
                else {
                    insertRecordingAtPos(position + 1, true);
                }
            }
        }
        //否则插到下一个位置
        else {
            insertRecordingAtPos(getChildPosition(view) + 1, false);
        }
        setFirstHint();
        this.mChanged |= CHANGE_CONTENT;
    }

    //当选择菜单中的选项时，有：录音，清单，照片，置顶等
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_top:
                if (item.isChecked()) {
                    this.mEditNote.mTopTime = 0;
                    item.setChecked(false);
                    Toast.makeText(this, R.string.top_cancel, Toast.LENGTH_SHORT).show();
                } else {
                    this.mEditNote.mTopTime = System.currentTimeMillis();
                    item.setChecked(true);
                    Toast.makeText(this, R.string.top_success, Toast.LENGTH_SHORT).show();
                }
                this.mChanged |= REQUEST_CODE_EXPORT_TO_TEXT;
                break;
            case R.id.menu_share:
                if (this.mShareIntent == null) {
                    if (checkSdcardOK()) {
                        popupProgressDialog(R.string.create_sharing);
                        new Thread(new Runnable() {
                            public void run() {
                                NoteEditActivity.this.onShareMenuAction(NoteEditActivity.REQUEST_CODE_PICK);
                                NoteEditActivity.this.mUiHandler.sendMessageAtTime(NoteEditActivity.this.mHandler.obtainMessage(NoteEditActivity.REQUEST_CODE_EXPORT_TO_PIC), 0);
                            }
                        }).start();
                        break;
                    }
                }
                return true;
            case R.id.menu_change_paper:
                onBackgroundMenuClick();
                break;
            case R.id.menu_delete:
                onDeleteMenuClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public Handler getUIHandler() {
        return this.mUiHandler;
    }

    //不懂？？
    public void onRecordResult(String fileName, int position) {
        Log.d(TAG, "onRecordResult");
        if (this.mRecordingLayoutView != null) {
            this.mRecordingLayoutView = null;

            if (fileName != null) {
                File file = NoteUtil.getFile(this.mEditNote.mUUId, fileName);
                if (!file.exists()) {
                    Log.d(TAG, "file not exist: " + fileName);
                    Toast.makeText(this, R.string.record_fail, Toast.LENGTH_SHORT).show();

                } else if (file.length() == 0) {
                    file.delete();
                    Toast.makeText(this, R.string.record_fail, Toast.LENGTH_SHORT).show();

                } else {
                    onInsertRecord(fileName, position);
                }
            }
        }
    }

    //在某个position插入录音
    public void insertRecordAtPos(String name, int position, boolean appendNew) {
        LayoutInflater inflater = getLayoutInflater();
        RichFrameLayout parent = (RichFrameLayout) inflater.inflate(R.layout.edit_record_item, null);
        parent.setUUIDandName(this.mEditNote.mUUId, name);
        this.mEditParent.addView(parent, position);
        ((RecordLinearLayout) parent.findViewById(R.id.recordLayout)).setRecordPlayManager((RecordLinearLayout.RecordPlayManager) this);
        if (appendNew) {
            View item = inflater.inflate(R.layout.edit_textlist_item, null);
            NoteEditText newText = (NoteEditText) item.findViewById(R.id.text);
            newText.setTextSize((float) (this.mEditNote.mTextSize > 0 ? this.mEditNote.mTextSize : NoteData.DEFAULT_FONT_SIZE));
            this.mEditParent.addView(item, position + REQUEST_CODE_EXPORT_TO_PIC);
            newText.requestFocus();
            Selection.setSelection(newText.getText(), REQUEST_CODE_PICK);
            //mRestoreSwitch打开的时候
        }
        setFirstHint();
    }

    //当点击录音时，调用insertRecordAtPos插入录音，需要名称和位置
    void onInsertRecord(String fileName, int position) {
        int count = this.mEditParent.getChildCount();
        boolean addText = false;
        if (position == count && count > 0) {
            if (!NoteUtil.JSON_TEXT.equals(this.mEditParent.getChildAt(count - 1).getTag())) {
                addText = true;
            }
        }
        //addText标识是否在录音后插入一个Text控件继续输入，若是在记事末尾则需要，在中间就不需要
        insertRecordAtPos(fileName, position, addText);
        //mRestoreSwitch打开的时候

        setFirstHint();
        this.mChanged |= CHANGE_CONTENT;
    }

    //打开软键盘
    public void onSoftInputShow() {
        if (!this.mSoftInputShown) {
            saveOnPageInfo();
            this.mSoftInputShown = true;
            if ((getWindow().getAttributes().softInputMode & REQUEST_CODE_LOGIN) == 0) {
                getWindow().setSoftInputMode(20);
            }
            invalidateOptionsMenu();
        }
    }

    public void setSoftInputShow(boolean flag) {
        if (this.mIsSoftInuptShow != flag) {
            this.mIsSoftInuptShow = flag;
            ActionBar actionBar = getActionBar();
            if (flag) {
                setBottomBlurEffect(true);
            } else {
                setBottomBlurEffect(false);
            }
        }
    }

    //需要滚动到Content吗？？？
    public boolean needScrollToContent() {
        if (this.mType != -5 || this.mTitleView.getText() == null || this.mTitleView.getText().length() <= 0) {
            return true;
        }
        return false;
    }

    //拉？？？没搞懂
    public void onDragList(View v) {
        if (v != null) {
            ViewGroup parent = (ViewGroup) v.getParent();
            DeleteImageView deleteView = null;
            if (parent != null) {
                deleteView = (DeleteImageView) parent.findViewById(R.id.delete);
            }
            if (deleteView != null) {
                deleteView.setVisibility(View.GONE);
            }
            View edit = parent.findViewById(R.id.text);
            if (edit != null) {
                boolean z;
                if (edit.isFocused() && this.mIsSoftInuptShow) {
                    z = true;
                } else {
                    z = false;
                }
                parent.startDrag(null, new ListShadowBuilder(parent), new ListDragLocalState(parent, Boolean.valueOf(z)), REQUEST_CODE_PICK);
            }
        }
    }

    //是否是编辑模式下
    public boolean isEditMode() {
        return this.mSoftInputShown;
    }

    //要删除的文件列表
    public HashSet<String> getDeleteFilesList() {
        if (this.mDeleteFilesList == null) {
            this.mDeleteFilesList = new HashSet();
        }
        return this.mDeleteFilesList;
    }

    //从删除mDeleteFilesList数组中的文件，里面存的是路径
    public void deleteFiles() {
        if (this.mDeleteFilesList != null) {
            Iterator i$ = this.mDeleteFilesList.iterator();
            while (i$.hasNext()) {
                String fileName = (String) i$.next();
                Log.i(TAG, "delete files : " + fileName);
                File file = NoteUtil.getFile(this.mEditNote.mUUId, fileName);
                if (file.exists()) {
                    File parentDir = file.getParentFile();
                    file.delete();
                    String[] childList = parentDir.list();
                    if (childList == null || childList.length == 0) {
                        parentDir.delete();
                    }
                }
            }
            scanNoteDir();
        }
    }


    //在指定position插入edit_textlist_item，根据state判断有三种模式


    //在指定position插入edit_image的RichFrameLayout控件，并设置指定图片

    //？？？
    public RecordingLayout getRecording() {
        return this.mRecordingLayoutView;
    }




    //listCount 好像是文本控件的数量在100以内返回true
    boolean listCountCheck() {
        int listCount = 0;
        int size = this.mEditParent.getChildCount();
        for (int i = 0; i < size; i += 1) {
            View view = this.mEditParent.getChildAt(i);
            if (NoteUtil.JSON_TEXT.equals((String) view.getTag()) && view.findViewById(R.id.check).isShown()) {
                listCount += 1;//1
            }
        }
        if (listCount < 100) {
            return true;
        }
        Toast.makeText(this, R.string.list_limit, REQUEST_CODE_PICK).show();
        return false;
    }

    //
    public int getCount() {
        return this.mCount;
    }

    //
    public void setCount(int count) {
        if (count < 0) {
            count = REQUEST_CODE_PICK;
        }
        if (count > MAX_WORDS) {
            count = MAX_WORDS;
        }
        this.mCount = count;
    }

    @Override
    public void onClick(View v) {
        int count;
        int index;
        switch (v.getId()) {
            case R.id.action_bill:
                if (this.mRecordingLayoutView == null || !this.mRecordingLayoutView.isRecording()) {
//                    MobEventUtil.onSendMobEvent(this, "click_list_button", null);
                    onListMenuClick();
                    break;
                }
                return;
            case R.id.action_camera:
                if ((this.mRecordingLayoutView == null || !this.mRecordingLayoutView.isRecording()) && checkSdcardOK()) {
                    count = this.mEditParent.getChildCount();
                    int picCount = 0;
                    for (index = 0; index < count; index += 1) {
                        if ("image".equals(this.mEditParent.getChildAt(index).getTag())) {
                            picCount += 1;
                            if (picCount >= 10) {
                                Toast.makeText(this, R.string.image_limit_tip, REQUEST_CODE_PICK).show();
                                return ;
                            }
                        }
                    }
                    TakePhotos();
                    break;
                }
                return;
            case R.id.action_gallery:

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), REQUEST_CODE_PICK);
                }
                findFocusView();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(AccessibilityEventCompat.TYPE_GESTURE_DETECTION_END);
                intent.putExtra("output", TempFileProvider.SCRAP_CONTENT_URI);
                if (!TextUtils.isEmpty(sLastInsertDirPath)) {
                    intent.putExtra("init_directory", sLastInsertDirPath);
                }
                Intent resultIntent = Intent.createChooser(intent, getResources().getText(R.string.insert_pic_title));
                startActivityForResult(resultIntent, REQUEST_CODE_PICK);
                break;

            case R.id.action_label:
                Intent i= new Intent();
                i.setClass(NoteEditActivity.this, LabelCustomActivity.class);
                startActivityForResult(i,99);
                return;
            case R.id.action_recorde:
                if (this.mRecordingLayoutView == null && checkSdcardOK()) {
//                    MobEventUtil.onSendMobEvent(this, "click_voice_button", null);
                    count = this.mEditParent.getChildCount();
                    int recordCount = 0;
                    //遍历所有的Noteitem，计算record的个数，当个数大于等于10的时候返回
                    for (index = 0; index < count; index += 1) {
                        if ("record".equals(this.mEditParent.getChildAt(index).getTag())) {
                            recordCount += 1;
                            if (recordCount >= 10) {
                                Toast.makeText(this, R.string.record_limit_tip, REQUEST_CODE_PICK).show();
                                return;
                            }
                        }
                    }
                    //录音个数小于10时，就录音
                    onRecord();
                }
                return;
            case R.id.action_reminder:
//                selectReminder();
                return;
            default:
                return;
        }
    }

    class ListShadowBuilder extends DragShadowBuilder {
        private int mBottomMargin;
        private int mLineSpace;
        private int mMargin;
        private int mTopMargin;
        private int mWidth;

        public ListShadowBuilder(View view) {
            super(view);
            Resources resources = view.getContext().getResources();
            this.mMargin = resources.getDimensionPixelSize(R.dimen.edit_item_dragshadow_space);
            this.mLineSpace = resources.getDimensionPixelSize(R.dimen.edit_text_line_space);
            this.mTopMargin = resources.getDimensionPixelSize(R.dimen.edit_note_edit_top_margin);
            this.mBottomMargin = resources.getDimensionPixelSize(R.dimen.edit_text_bottom_margin);
        }

        public void onDrawShadow(Canvas canvas) {
            View view = getView();
            Drawable background = view.getContext().getResources().getDrawable(R.drawable.list_choosen_background);
            background.setBounds(NoteEditActivity.REQUEST_CODE_PICK, NoteEditActivity.REQUEST_CODE_PICK, canvas.getWidth(), canvas.getHeight());
            background.draw(canvas);
            canvas.save();
            canvas.translate(0.0f, (float) ((this.mMargin - view.getPaddingTop()) - this.mTopMargin));
            super.onDrawShadow(canvas);
            canvas.restore();
        }

        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            View view = getView();
            if (view != null) {
                shadowSize.set(view.getWidth(), (((((view.getHeight() - view.getPaddingTop()) - view.getPaddingBottom()) - this.mLineSpace) - this.mTopMargin) - this.mBottomMargin) + (this.mMargin * NoteEditActivity.REQUEST_CODE_EXPORT_TO_TEXT));
                shadowTouchPoint.set(NoteEditActivity.REQUEST_CODE_PICK, 40);
            }
        }
    }

    //Tag信息类
    class TagInfo {
        long id;
        String name;

        public TagInfo(long tid, String tname) {
            this.id = tid;
            this.name = tname;
        }

        public String toString() {
            return this.name;
        }
    }


    private void tintImageViewDrawable(int imageViewId, int iconId, int colorsId) {
        Drawable tintIcon = DrawableCompat.wrap(ContextCompat.getDrawable(this, iconId));
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(this, colorsId));
        ((ImageView) findViewById(imageViewId)).setImageDrawable(tintIcon);
    }
}