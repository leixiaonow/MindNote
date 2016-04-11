package top.leixiao.mindnote.widget;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;

import top.leixiao.mindnote.R;


public class PopupPaperWindow extends PopupWindow {
    View mAnimView;
    Context mContext;
    OnPopupStateChangeListener mPopupStateChangeListener;

    public PopupPaperWindow(Context context) {
        super(context);
        this.mContext = context;
    }

    public void setAnimateView(View view) {
        this.mAnimView = view;
        Animation ani = AnimationUtils.loadAnimation(this.mContext, R.anim.background_popup_enter);
        ani.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                if (PopupPaperWindow.this.mPopupStateChangeListener != null) {
                    PopupPaperWindow.this.mPopupStateChangeListener.onPopup();
                }
            }

            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.setAnimation(ani);
    }

    public void setPopupStateChangeListener(OnPopupStateChangeListener l) {
        this.mPopupStateChangeListener = l;
    }

    public void dismiss() {
        if (this.mAnimView != null) {
            this.mAnimView.clearAnimation();
            Animation ani = AnimationUtils.loadAnimation(this.mContext, R.anim.background_popup_exit);
            ani.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (PopupPaperWindow.this.mAnimView != null) {
                        PopupPaperWindow.this.mAnimView.clearAnimation();
                        PopupPaperWindow.this.mAnimView = null;
                        PopupPaperWindow.this.dismiss();
                        if (PopupPaperWindow.this.mPopupStateChangeListener != null) {
                            PopupPaperWindow.this.mPopupStateChangeListener.onPopDown();
                        }
                    }
                }
            });
            this.mAnimView.startAnimation(ani);
            return;
        }
        super.dismiss();
    }

    public interface OnPopupStateChangeListener {
        void onPopDown();

        void onPopup();
    }
}
