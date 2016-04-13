package top.leixiao.mindnote.common;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/*acquireResource 自己实现*/

public class ThreadPool {
    private static final int CORE_POOL_SIZE = 3;
    public static final JobContext JOB_CONTEXT_STUB = new JobContextStub();
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int MAX_POOL_SIZE = 6;
    public static final int MODE_CPU = 1;
    public static final int MODE_NETWORK = 2;
    public static final int MODE_NONE = 0;
    public static final int MODE_SINGLE = 3;
    private static final String TAG = "ThreadPool";
    ResourceCounter mCpuCounter;
    private final Executor mExecutor;
    ResourceCounter mNetworkCounter;
    ResourceCounter mSingleCounter;

    public interface CancelListener {
        void onCancel();
    }

    public interface Job<T> {
        T run(JobContext jobContext);
    }

    public interface JobContext {
        boolean isCancelled();

        void setCancelListener(CancelListener cancelListener);

        boolean setMode(int i);
    }

    private static class ResourceCounter {
        public int value;

        public ResourceCounter(int v) {
            this.value = v;
        }
    }

    private static class JobContextStub implements JobContext {
        private JobContextStub() {
        }

        public boolean isCancelled() {
            return false;
        }

        public void setCancelListener(CancelListener listener) {
        }

        public boolean setMode(int mode) {
            return true;
        }
    }

    private class Worker<T> implements Runnable, Future<T>, JobContext {
        private static final String TAG = "Worker";
        private CancelListener mCancelListener;
        private volatile boolean mIsCancelled;
        private boolean mIsDone;
        private Job<T> mJob;
        private FutureListener<T> mListener;
        private int mMode;
        private T mResult;
        private ResourceCounter mWaitOnResource;

        public Worker(Job<T> job, FutureListener<T> listener) {
            this.mJob = job;
            this.mListener = listener;
        }

        public void run() {
            Object result = null;
            if (setMode(ThreadPool.MODE_CPU)) {
                try {
                    result = this.mJob.run(this);
                } catch (Throwable ex) {
                    Log.w(TAG, "Exception in running a job", ex);
                }
            }
            synchronized (this) {
                setMode(ThreadPool.MODE_NONE);
                this.mResult = (T) result;/*cast to T*/
                this.mIsDone = true;
                notifyAll();
            }
            if (this.mListener != null) {
                this.mListener.onFutureDone(this);
            }
        }

        public synchronized void cancel() {
            if (!this.mIsCancelled) {
                this.mIsCancelled = true;
                if (this.mWaitOnResource != null) {
                    synchronized (this.mWaitOnResource) {
                        this.mWaitOnResource.notifyAll();
                    }
                }
                if (this.mCancelListener != null) {
                    this.mCancelListener.onCancel();
                }
            }
        }


        public boolean isCancelled() {
            return this.mIsCancelled;
        }

        public synchronized boolean isDone() {
            return this.mIsDone;
        }

        public synchronized T get() {
            while (!this.mIsDone) {
                try {
                    wait();
                } catch (Exception ex) {
                }
            }
            return this.mResult;
        }


        public void waitDone() {
            get();
        }

        public synchronized void setCancelListener(CancelListener listener) {
            this.mCancelListener = listener;
            if (this.mIsCancelled && this.mCancelListener != null) {
                this.mCancelListener.onCancel();
            }
        }

        public boolean setMode(int mode) {
            ResourceCounter rc = modeToCounter(this.mMode);
            if (rc != null) {
                releaseResource(rc);
            }
            this.mMode = ThreadPool.MODE_NONE;
            rc = modeToCounter(mode);
            if (rc != null) {
                if (!acquireResource(rc)) {
                    return false;
                }
                this.mMode = mode;
            }
            return true;
        }

        private ResourceCounter modeToCounter(int mode) {
            if (mode == ThreadPool.MODE_CPU) {
                return ThreadPool.this.mCpuCounter;
            }
            if (mode == ThreadPool.MODE_NETWORK) {
                return ThreadPool.this.mNetworkCounter;
            }
            if (mode == ThreadPool.MODE_SINGLE) {
                return ThreadPool.this.mSingleCounter;
            }
            return null;
        }
/*反编译错误*/
/*我根据jeb的goto代码改的，正确性未知*/
        private boolean acquireResource(ResourceCounter counter) {

            boolean bool;
            while(true) {
                synchronized (this) {
                    if (this.mIsCancelled) {
                        this.mWaitOnResource = null;
                        bool = false;
                        return bool;
                    } else {
                        this.mWaitOnResource = counter;
                    }
                }
                synchronized (counter) {
                    if (counter.value > 0) {
                        --counter.value;
                    } else {
                        try {
                            counter.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;//重头开始执行，进入下一次循环
                    }
                }
//                没有进入下一次循环
                synchronized (this) {
                    this.mWaitOnResource = null;
                    bool = true;
                }
                return bool;
            }
        }

        private void releaseResource(ResourceCounter counter) {
            synchronized (counter) {
                counter.value += ThreadPool.MODE_CPU;
                counter.notifyAll();
            }
        }
    }

    public ThreadPool() {
        this(MODE_SINGLE, MAX_POOL_SIZE);
    }

    public ThreadPool(int initPoolSize, int maxPoolSize) {
        this.mCpuCounter = new ResourceCounter(MODE_NETWORK);
        this.mNetworkCounter = new ResourceCounter(MODE_NETWORK);
        this.mSingleCounter = new ResourceCounter(MODE_CPU);
        this.mExecutor = new ThreadPoolExecutor(initPoolSize, maxPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("thread-pool", KEEP_ALIVE_TIME));
    }

    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        Worker<T> w = new Worker(job, listener);
        this.mExecutor.execute(w);
        return w;
    }

    public <T> Future<T> submit(Job<T> job) {
        return submit(job, null);
    }
}
