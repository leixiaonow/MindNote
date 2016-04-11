package top.leixiao.mindnote.widget;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import top.leixiao.mindnote.R;
import top.leixiao.mindnote.utils.NoteUtil;
import top.leixiao.mindnote.utils.RecordUtil;

public class RecordLinearLayout extends LinearLayout implements OnCompletionListener {
    public static final int DISABLE = 3;
    public static final int NORMAL = 0;
    public static final int PLAYING = 1;
    public static final int PLAYPAUSE = 2;
    final String TAG = "RecordLinearLayout";
    final Handler mHandler = new Handler();
    public int mPlayingState;
    boolean mFromTouch = false;
    String mRecordFileName;
    String mUUID;
    private ImageButton mDeleteBtn;
    private TextView mPassView;
    private ImageButton mPauseBtn;
    private MediaPlayer mPlayer;
    private RecordPlayManager mRecordPlayManager;
    private SeekBar mSeekBar;
    private long mTotalTime = 0;
    private TextView mTotalView;
    Runnable mUpdateTimer = new Runnable() {
        public void run() {
            RecordLinearLayout.this.updateProgress();
        }
    };

    public RecordLinearLayout(Context context) {
        super(context);
    }

    public RecordLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void init() {
        this.mDeleteBtn = (ImageButton) findViewById(R.id.delete);
        this.mPauseBtn = (ImageButton) findViewById(R.id.player_control);
        this.mTotalView = (TextView) findViewById(R.id.player_totaltime);
        this.mTotalView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RecordLinearLayout.this.onClick();
            }
        });
        this.mPassView = (TextView) findViewById(R.id.player_passtime);
        this.mPassView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RecordLinearLayout.this.onClick();
            }
        });
        this.mSeekBar = (SeekBar) findViewById(R.id.player_seekbar);
        setPlayState(NORMAL);
        this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                RecordLinearLayout.this.mFromTouch = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                int dest = seekBar.getProgress();
                if (RecordLinearLayout.this.mPlayer != null) {
                    RecordLinearLayout.this.mPlayer.seekTo(dest);
                }
                RecordLinearLayout.this.mFromTouch = false;
            }
        });
        this.mPauseBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                switch (RecordLinearLayout.this.mPlayingState) {
                    case RecordLinearLayout.NORMAL /*0*/:
                        RecordLinearLayout.this.mRecordPlayManager.startPlay(RecordLinearLayout.this);
                        return;
                    case RecordLinearLayout.PLAYING /*1*/:
                        RecordLinearLayout.this.mRecordPlayManager.pausePlay(RecordLinearLayout.this);
                        return;
                    case RecordLinearLayout.PLAYPAUSE /*2*/:
                        RecordLinearLayout.this.mRecordPlayManager.startPlay(RecordLinearLayout.this);
                        return;
                    default:
                        return;
                }
            }
        });
        this.mDeleteBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Builder builder = new Builder(RecordLinearLayout.this.getContext());
                builder.setMessage(R.string.tip_delete_record);
                builder.setPositiveButton("删除录音", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((RichFrameLayout) RecordLinearLayout.this.getParent()).deleteRichLayout();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show().getButton(-1).setTextColor(RecordLinearLayout.this.getResources().getColorStateList(R.color.mz_button_text_color_coral));
            }
        });
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public void setRecordPlayManager(RecordPlayManager ram) {
        this.mRecordPlayManager = ram;
    }

    public void setUUIDandName(String uuid, String name) {
        this.mUUID = uuid;
        this.mRecordFileName = name;
        File file = NoteUtil.getFile(this.mUUID, this.mRecordFileName);
        if (file.exists()) {
            this.mSeekBar.setProgress(NORMAL);
            this.mTotalTime = (file.length() / 8) / 1000;
            this.mTotalView.setText(RecordUtil.timeConvert(this.mTotalTime));
            return;
        }
        setPlayState(DISABLE);
        this.mTotalView.setText(null);
    }

    public String getRecordFileName() {
        return this.mRecordFileName;
    }

    public void setPlayState(int playState) {
        this.mPlayingState = playState;
        switch (this.mPlayingState) {
            case NORMAL /*0*/:
                this.mPauseBtn.setImageResource(R.drawable.btn_record_play);
                this.mDeleteBtn.setVisibility(VISIBLE);
                this.mTotalView.setText(RecordUtil.timeConvert(this.mTotalTime));
                this.mPassView.setVisibility(GONE);
                return;
            case PLAYING /*1*/:
                this.mPauseBtn.setImageResource(R.drawable.btn_record_pause);
                this.mDeleteBtn.setVisibility(GONE);
                this.mTotalView.setText(NoteUtil.RECORD_DIV + RecordUtil.timeConvert(this.mTotalTime));
                this.mPassView.setVisibility(VISIBLE);
                this.mPassView.setText(RecordUtil.timeConvert((long) (this.mSeekBar.getProgress() / 1000)));
                return;
            case PLAYPAUSE /*2*/:
                this.mPauseBtn.setImageResource(R.drawable.btn_record_play);
                this.mDeleteBtn.setVisibility(VISIBLE);
                this.mTotalView.setText(RecordUtil.timeConvert(this.mTotalTime));
                this.mPassView.setVisibility(GONE);
                return;
            case DISABLE /*3*/:
                this.mPauseBtn.setImageResource(R.drawable.btn_record_play);
                this.mPauseBtn.setEnabled(false);
                this.mDeleteBtn.setVisibility(VISIBLE);
                this.mTotalView.setText(RecordUtil.timeConvert(this.mTotalTime));
                this.mPassView.setVisibility(GONE);
                return;
            default:
                return;
        }
    }

    public void startPlay() {
        if (this.mPlayer != null) {
            this.mPlayer.start();
            setPlayState(PLAYING);
            updateProgress();
            return;
        }
        this.mPlayer = new MediaPlayer();
        File file = NoteUtil.getFile(this.mUUID, this.mRecordFileName);
        if (file.exists()) {
            try {
                this.mPlayer.setDataSource(file.getPath());
                this.mPlayer.setOnCompletionListener(this);
                this.mPlayer.setAudioStreamType(DISABLE);
                this.mPlayer.prepare();
                this.mPlayer.start();
                this.mSeekBar.setMax(this.mPlayer.getDuration());
                this.mSeekBar.setProgress(NORMAL);
                setPlayState(PLAYING);
                updateProgress();
                return;
            } catch (IllegalArgumentException e) {
                this.mPlayer = null;
                return;
            } catch (IOException e2) {
                this.mPlayer = null;
                return;
            }
        }
        Toast.makeText(getContext(), getContext().getResources().getString(R.string.tip_no_file), Toast.LENGTH_SHORT).show();
    }

    public void pausePlay() {
        if (this.mPlayer != null) {
            this.mPlayer.pause();
            setPlayState(PLAYPAUSE);
        }
    }

    public void stopPlay() {
        if (this.mPlayer != null) {
            this.mPlayer.stop();
            this.mPlayer.release();
            this.mPlayer = null;
            setPlayState(NORMAL);
            this.mHandler.removeCallbacks(this.mUpdateTimer);
            this.mSeekBar.setProgress(NORMAL);
        }
    }

    public void updateProgress() {
        if (this.mPlayingState == PLAYING) {
            this.mHandler.postDelayed(this.mUpdateTimer, 50);
            if (!this.mFromTouch) {
                int time = this.mPlayer.getCurrentPosition();
                int temp = this.mSeekBar.getProgress();
                int max = this.mSeekBar.getMax();
                if (time >= max) {
                    Log.d("RecordLinearLayout", "time >= max");
                    this.mRecordPlayManager.stopPlay(this);
                    return;
                } else if (time >= max - 100) {
                    temp += 50;
                    if (temp >= max) {
                        temp = max;
                    }
                    this.mSeekBar.setProgress(temp);
                    Log.d("RecordLinearLayout", "temp setProgress: time-" + time + " temp--" + temp + " max--" + max);
                } else {
                    this.mSeekBar.setProgress(time);
                    Log.d("RecordLinearLayout", "temp setProgress: time-" + time);
                }
            }
            this.mTotalView.setText(NoteUtil.RECORD_DIV + RecordUtil.timeConvert(this.mTotalTime));
            this.mPassView.setText(RecordUtil.timeConvert((long) (this.mSeekBar.getProgress() / 1000)));
        }
    }

    //增加super.onDetachedFromWindow();
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPlayingState == PLAYING || this.mPlayingState == PLAYPAUSE) {
            this.mRecordPlayManager.stopPlay(this);
        }
    }

    public void onCompletion(MediaPlayer mp) {
        this.mRecordPlayManager.stopPlay(this);
    }

    public void onClick() {
        View rootView = getRootView();
        getGlobalVisibleRect(new Rect());
        View view = (View) getParent();
        if (view instanceof RichFrameLayout) {
            ((RichFrameLayout) view).onFocus();
        }
    }

    public interface RecordPlayManager {
        void pausePlay(RecordLinearLayout recordLinearLayout);

        void startPlay(RecordLinearLayout recordLinearLayout);

        void stopPlay(RecordLinearLayout recordLinearLayout);
    }
}
