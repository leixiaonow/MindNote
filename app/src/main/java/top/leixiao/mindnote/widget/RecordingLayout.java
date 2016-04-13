package top.leixiao.mindnote.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Handler;
import android.support.v7.appcompat.BuildConfig;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import top.leixiao.mindnote.NoteEditActivity;
import top.leixiao.mindnote.R;
import top.leixiao.mindnote.utils.NoteUtil;
import top.leixiao.mindnote.utils.RecordUtil;
import top.leixiao.mindnote.utils.ReflectUtils;

public class RecordingLayout extends RelativeLayout {
    private static final int MAXAMPLITUDE = 32768;
    private static final int MAXLEVEL = 10000;
    private static final int REFRESH_FREQUENCE = 100;
    private static final float SCALE = 5.0f;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_STOP = 0;
    final String TAG = "RecordingLayout";
    final Handler mRecordHandler = new Handler();
    ImageView mAnimView;
    TextView mDigitTimer;
    MediaRecorder mMediaRecorder = null;
    ImageView mPauseBtn;
    String mRecordName;
    int mRecordState = 0;
    File mRecordingFile;
    ImageView mStopBtn;
    String mUUID;
    Runnable mUpdateRecordDisplay = new Runnable() {
        public void run() {
            RecordingLayout.this.setVisibility(VISIBLE);
        }
    };
    Runnable mUpdateRecordTimer = new Runnable() {
        public void run() {
            if (RecordingLayout.this.mRecordState == RecordingLayout.STATE_RECORDING && RecordingLayout.this.mMediaRecorder != null) {
                RecordingLayout.this.updateTimerView();
                float scaledAmp = ((float) RecordingLayout.this.mMediaRecorder.getMaxAmplitude()) * RecordingLayout.SCALE;
                int level = scaledAmp >= 32768.0f ? RecordingLayout.MAXLEVEL : (int) ((10000.0f * scaledAmp) / 32768.0f);
                Drawable drawable = RecordingLayout.this.mAnimView.getDrawable();
                if (level <= 0) {
                    level = RecordingLayout.STATE_RECORDING;
                }
                drawable.setLevel(level);
            }
        }
    };
    Runnable mStopRecord = new Runnable() {
        public void run() {
            RecordingLayout.this.mRecordHandler.removeCallbacks(RecordingLayout.this.mUpdateRecordTimer);
            RecordingLayout.this.stopRecording(true);
        }
    };
    OnErrorListener mErrorListener = new OnErrorListener() {
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.d(toString(), "record error: what " + what + " extra: " + extra);
            RecordingLayout.this.mRecordHandler.removeCallbacks(RecordingLayout.this.mUpdateRecordTimer);
            RecordingLayout.this.stopRecording(true);
        }
    };

    public RecordingLayout(Context context) {
        super(context);
    }

    public RecordingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void init() {
        this.mDigitTimer = (TextView) findViewById(R.id.time);
        this.mStopBtn = (ImageView) findViewById(R.id.stop);
        this.mStopBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RecordingLayout.this.mRecordHandler.removeCallbacks(RecordingLayout.this.mUpdateRecordTimer);
                try {
                    long time = (RecordingLayout.this.mRecordingFile.length() / 8) / 1000;
                    if (time < 1) {
                        Log.d(toString(), "Duration < 1s: " + time);
                        Toast.makeText(RecordingLayout.this.getContext(), R.string.record_too_short, Toast.LENGTH_SHORT).show();
                        RecordingLayout.this.deleteRecordFile();
                        RecordingLayout.this.mRecordName = null;
                    }
                    RecordingLayout.this.stopRecording(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Object[] objArr = new Object[STATE_PAUSE];
        objArr[0] = Integer.valueOf(0);
        objArr[STATE_RECORDING] = Integer.valueOf(0);
        this.mDigitTimer.setText(String.format("%02d:%02d", objArr));
        this.mAnimView = (ImageView) findViewById(R.id.anim);
        this.mAnimView.getDrawable().setLevel(STATE_RECORDING);
        this.mPauseBtn = (ImageView) findViewById(R.id.pause);
        this.mPauseBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (RecordingLayout.this.mRecordState == RecordingLayout.STATE_RECORDING) {
                    if (ReflectUtils.pause(RecordingLayout.this.mMediaRecorder) != -1) {
                        RecordingLayout.this.mRecordState = RecordingLayout.STATE_PAUSE;
                        RecordingLayout.this.mPauseBtn.setImageResource(R.drawable.mz_ic_play);
                    }
                } else if (RecordingLayout.this.mRecordState == RecordingLayout.STATE_PAUSE && ReflectUtils.resume(RecordingLayout.this.mMediaRecorder) != -1) {
                    RecordingLayout.this.mRecordState = RecordingLayout.STATE_RECORDING;
                    RecordingLayout.this.mRecordHandler.postDelayed(RecordingLayout.this.mUpdateRecordTimer, 10);
                    RecordingLayout.this.mPauseBtn.setImageResource(R.drawable.mz_ic_pause);
                }
            }
        });
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public void setUUID(String uuid) {
        this.mUUID = uuid;
    }

    private File getNewRecordFile() {
        String prefix = "record_" + NoteUtil.getTimeString();
        int index = 0;
        //遍历文件夹，找到一个编号是多少，并生成File返回
        //100表示文件数目最大值，也可是其他值
        while (index < 100) {
            String indexStr;
            if (index == 0) {
                indexStr = BuildConfig.VERSION_NAME;
            } else {
                indexStr = "_" + String.valueOf(index);
            }
            File file = NoteUtil.getFile(this.mUUID, prefix + indexStr + ".mp3");
            if (file.exists()) {
                index += 1;
            } else {
                if (!file.getParentFile().exists()) {
                    File pDataDir = new File(NoteUtil.FILES_ANDROID_DATA);
                    if (pDataDir == null || !pDataDir.exists()) {
                        Log.d("RecordingLayout", "Android data dir not exist.");
                        return null;
                    } else if (!file.getParentFile().mkdirs()) {
                        Log.d("RecordingLayout", "mkdirs fail: " + file.getPath());
                        return null;
                    }
                }
                return file;
            }
        }
        return null;
    }

    public String getRecordFileName() {
        return this.mRecordName;
    }

    public void startRecord() {
        new Thread(new Runnable() {
            public void run() {
                Log.d(toString(), "startRecord");
                RecordingLayout.this.mRecordName = null;
                if (RecordingLayout.this.mMediaRecorder == null) {
                    RecordingLayout.this.mMediaRecorder = new MediaRecorder();
                    RecordingLayout.this.mMediaRecorder.setAudioSource(RecordingLayout.STATE_RECORDING);
//                    RecordingLayout.this.mMediaRecorder.setOutputFormat(ReflectUtils.getStaticIntValue("android.media.MediaRecorder$OutputFormat", "MPEG_3", 9));
//                    RecordingLayout.this.mMediaRecorder.setAudioEncoder(ReflectUtils.getStaticIntValue("android.media.MediaRecorder$AudioEncoder", "MPEG_3", 6));
                    RecordingLayout.this.mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    RecordingLayout.this.mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    File file = RecordingLayout.this.getNewRecordFile();
                    if (file == null) {
                        Log.d(toString(), "no file return");
                        RecordingLayout.this.mRecordHandler.postDelayed(RecordingLayout.this.mStopRecord, 0);
                        return;
                    }
                    try {
                        if (file.createNewFile()) {
                            RecordingLayout.this.mRecordingFile = file;
                            RecordingLayout.this.mRecordName = file.getName();
                            RecordingLayout.this.mMediaRecorder.setOutputFile(file.getPath());
                            RecordingLayout.this.mMediaRecorder.prepare();
                            RecordingLayout.this.mMediaRecorder.setOnErrorListener(RecordingLayout.this.mErrorListener);
                            RecordingLayout.this.mMediaRecorder.start();
                            RecordingLayout.this.mRecordState = RecordingLayout.STATE_RECORDING;
                        } else {
                            Log.d(toString(), "create file fail");
                            RecordingLayout.this.mRecordHandler.postDelayed(RecordingLayout.this.mStopRecord, 0);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        RecordingLayout.this.deleteRecordFile();
                        RecordingLayout.this.mRecordHandler.postDelayed(RecordingLayout.this.mStopRecord, 0);
                        Log.d(toString(), "exception return");
                        return;
                    }
                }
                RecordingLayout.this.mRecordHandler.postDelayed(RecordingLayout.this.mUpdateRecordDisplay, 0);
                RecordingLayout.this.mRecordHandler.postDelayed(RecordingLayout.this.mUpdateRecordTimer, 10);
                Log.d(toString(), "post delay");
            }
        }).start();
    }

    void deleteRecordFile() {
        if (this.mRecordName != null) {
            File file = NoteUtil.getFile(this.mUUID, this.mRecordName);
            if (file.exists()) {
                File parentDir = file.getParentFile();
                file.delete();
                String[] childList = parentDir.list();
                if (childList == null || childList.length == 0) {
                    parentDir.delete();
                }
            }
        }
    }

    public void updateTimerView() {
        if (this.mMediaRecorder != null) {
            long time = (this.mRecordingFile.length()/2) / 1000;
            this.mDigitTimer.setText(RecordUtil.timeConvert(time));
            if (time >= 599) {
                this.mRecordHandler.removeCallbacks(this.mUpdateRecordTimer);
                this.mRecordHandler.postDelayed(this.mStopRecord, 400);
                return;
            }
            this.mRecordHandler.postDelayed(this.mUpdateRecordTimer, 100);
        }
    }

    private void internalStopRecording() {
        this.mRecordHandler.removeCallbacks(this.mStopRecord);
        try {
            if (this.mMediaRecorder != null) {
                Log.d(toString(), "stopRecording mMediaRecorder != null");
                this.mMediaRecorder.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mMediaRecorder != null) {
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
        }
        this.mRecordState = 0;
    }

    public void cancelRecording() {
        this.mRecordHandler.removeCallbacks(this.mUpdateRecordTimer);
        internalStopRecording();
        deleteRecordFile();
        this.mRecordName = null;
        deleteRecordView();
    }

    int deleteRecordView() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) {
            return -1;
        }
        int count = parent.getChildCount();
        int index = 0;
        while (index < count && parent.getChildAt(index) != this) {
            index += STATE_RECORDING;
        }
        if (index == count) {
            return -1;
        }
        parent.removeViewAt(index);
        return index;
    }

    public void stopRecording(boolean refresh) {
        Log.d(toString(), "stopRecording ");
        internalStopRecording();
        saveIntoFileDB();
        if (refresh) {
            int position = deleteRecordView();
            if (position != -1) {
                ((NoteEditActivity) getContext()).onRecordResult(this.mRecordName, position);
            }
            this.mRecordingFile = null;
            this.mRecordState = 0;
        }
    }

    void saveIntoFileDB() {
        if (this.mRecordName != null) {
            File file = NoteUtil.getFile(this.mUUID, this.mRecordName);
            if (file.exists() && file.length() > 0) {
                ((NoteEditActivity) getContext()).insertFileInDataBase(this.mUUID, this.mRecordName, STATE_RECORDING);
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mMediaRecorder != null) {
            try {
                if (this.mMediaRecorder != null) {
                    Log.d(toString(), "stopRecording mMediaRecorder != null");
                    this.mMediaRecorder.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
            saveIntoFileDB();
        }
    }

    public void pause() {
        if (this.mRecordState == STATE_RECORDING && ReflectUtils.pause(this.mMediaRecorder) != -1) {
            this.mRecordState = STATE_PAUSE;
            this.mPauseBtn.setImageResource(R.drawable.mz_ic_play);
        }
    }

    public boolean isRecording() {
        return this.mRecordState == STATE_RECORDING;
    }
}
