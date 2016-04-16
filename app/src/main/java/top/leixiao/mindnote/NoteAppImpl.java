package top.leixiao.mindnote;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.ArrayList;

import top.leixiao.mindnote.catogery.CatogetyCustomActivity;
import top.leixiao.mindnote.common.ThreadPool;
import top.leixiao.mindnote.component.BaseApplication;
import top.leixiao.mindnote.label.LabelCustomActivity;
import top.leixiao.mindnote.label.LabelManager;


public class NoteAppImpl extends BaseApplication {
    private static NoteAppImpl mApp;
    private LabelManager mLabelManager;
    private Looper mSaveNoteDataLooper;
    private ThreadPool mThreadPool;

    public ArrayList<LabelCustomActivity.LabelHolder> mlabels;
    public ArrayList<Integer> mSelectLabelIds = new ArrayList<>();

    public ArrayList<CatogetyCustomActivity.CatogeryHolder> mCatogetys;

    public void onCreate() {
        super.onCreate();
        initContext();//单例模式，初始化mApp
        initThreadPool();//创建对象
        initLabelManager();//创建对象
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        enableLog(0);
    }

    private void initContext() {
        mApp = this;
    }

    private void initLabelManager() {
        this.mLabelManager = new LabelManager(this);
        this.mLabelManager.init();
    }

    private void initThreadPool() {
        this.mThreadPool = new ThreadPool();
    }

    public ThreadPool getThreadPool() {
        return this.mThreadPool;
    }


    public LabelManager getLabelManager() {
        return this.mLabelManager;
    }

    public static NoteAppImpl getContext() {
        return mApp;
    }

    public synchronized Looper getSaveNoteDataLooper() {
        if (this.mSaveNoteDataLooper == null) {
            HandlerThread handlerThread = new HandlerThread("save note data");
            handlerThread.start();
            this.mSaveNoteDataLooper = handlerThread.getLooper();
        }
        return this.mSaveNoteDataLooper;
    }

}
