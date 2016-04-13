package top.leixiao.mindnote.common;

public interface FutureListener<T> {
    void onFutureDone(Future<T> future);
}
