package com.tk.sampleadapter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * <pre>
 *     author : TK
 *     time   : 2017/7/22
 *     desc   : 嵌套
 * </pre>
 */

public class SimpleNestedScrollView extends NestedScrollView {
    private static final int CHECK_WHAT = 233;
    //50ms 校验一次
    private static final int DELAY = 50;
    private boolean isTouch;
    private int mLastY;
    private OnScrollListener onScrollListener;
    private Handler checkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CHECK_WHAT) {
                if (mLastY == getScrollY()) {
                    if (onScrollListener != null) {
                        onScrollListener.onScrollStateChanged(SimpleNestedScrollView.this, OnScrollListener.SCROLL_STATE_IDLE);
                    }
                }
            }
        }
    };

    public SimpleNestedScrollView(Context context) {
        this(context, null);
    }

    public SimpleNestedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isTouch = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouch = false;
                checkHandler.removeMessages(CHECK_WHAT);
                checkHandler.sendEmptyMessageDelayed(CHECK_WHAT, DELAY);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollListener == null) {
            return;
        }
        mLastY = t;
        if (isTouch) {
            if (t != oldt) {
                // 有手指触摸，并且位置有滚动
                onScrollListener.onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_DRAGGING);
            }
        } else {
            if (t != oldt) {
                // 没有手指触摸，并且位置有滚动，fling
                onScrollListener.onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_FLING);
                // 记住滑动的位置
                checkHandler.removeMessages(CHECK_WHAT);
                checkHandler.sendEmptyMessageDelayed(CHECK_WHAT, DELAY);
            }
        }
        onScrollListener.onScrollChanged(l, t, oldl, oldt);

    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    /**
     * 滚动扩展监听
     */
    public interface OnScrollListener {
        /**
         * 静止
         */
        int SCROLL_STATE_IDLE = RecyclerView.SCROLL_STATE_IDLE;
        /**
         * 拖拽
         */
        int SCROLL_STATE_DRAGGING = RecyclerView.SCROLL_STATE_DRAGGING;
        /**
         * 自由滑动
         */
        int SCROLL_STATE_FLING = 2;


        /**
         * 滑动状态回调
         *
         * @param view
         * @param scrollState 滚动的状态
         */
        void onScrollStateChanged(SimpleNestedScrollView view, int scrollState);

        /**
         * 滑动位置回调
         *
         * @param l
         * @param t
         * @param oldl
         * @param oldt
         */
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
