package com.tk.fasteradapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * <pre>
 *     author : TK
 *     time   : 2017/07/15
 *     desc   : RecyclerView Holder的高度封装：
 *              封装findViewById;
 *              封装常见View操作；
 *              持有FasterAdapter引用应对特殊业务场景；
 *              封装SparseArray放置对象集合；
 *              封装ViewHolder的生命周期回调，有需求重写方法；
 *
 * </pre>
 */
public class FasterHolder extends RecyclerView.ViewHolder {
    /**
     * 持有FasterAdapter的引用，考虑到业务场景也都是内部类隐式持有外部类引用
     */
    private FasterAdapter mAdapter = null;
    /**
     * 存放view对象
     */
    private final SparseArray<View> mViews;
    /**
     * 放置额外的对象标记
     */
    private SparseArray<Object> mExtra = null;

    public FasterHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    /**
     * 依附
     *
     * @param adapter
     */
    void attach(FasterAdapter adapter) {
        this.mAdapter = adapter;
    }

    /**
     * 获取持有的FasterAdapter引用
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <T> FasterAdapter<T> getAdapter() {
        return (FasterAdapter<T>) mAdapter;
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public final Context getContext() {
        return itemView.getContext();
    }

    /**
     * 获取集合中的位置
     *
     * @return
     */
    public final int getListPosition() {
        return Math.max(getAdapterPosition() - mAdapter.getHeaderSpace(), 0);
    }

    /**
     * 通过id获取控件
     *
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <T extends View> T findViewById(@IdRes int viewId) {
        View view = mViews.get(viewId);
        if (null == view) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 设置图像加载
     *
     * @param viewId
     * @param drawable
     * @return
     */
    public final FasterHolder setImage(@IdRes int viewId, Drawable drawable) {
        this.<ImageView>findViewById(viewId).setImageDrawable(drawable);
        return this;
    }

    /**
     * 设置图像
     *
     * @param viewId
     * @param resId
     * @return
     */
    public final FasterHolder setImage(@IdRes int viewId, @DrawableRes int resId) {
        this.<ImageView>findViewById(viewId).setImageResource(resId);
        return this;
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param resId
     * @return
     */
    public final FasterHolder setText(@IdRes int viewId, @StringRes int resId) {
        this.<TextView>findViewById(viewId).setText(resId);
        return this;
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param text
     * @return
     */
    public final FasterHolder setText(@IdRes int viewId, CharSequence text) {
        this.<TextView>findViewById(viewId).setText(text);
        return this;
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param text
     * @param nullText
     * @return
     */
    public final FasterHolder setTextOrNull(@IdRes int viewId, CharSequence text, CharSequence nullText) {
        this.<TextView>findViewById(viewId).setText(EmptyUtils.isEmpty(text) ? nullText : text);
        return this;
    }

    /**
     * 设置文本颜色
     *
     * @param viewId
     * @param colorRes
     * @return
     */
    public final FasterHolder setTextColorByRes(@IdRes int viewId, @ColorRes int colorRes) {
        this.<TextView>findViewById(viewId).setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        return this;
    }

    /**
     * 设置文本颜色
     *
     * @param viewId
     * @param textColor
     * @return
     */
    public final FasterHolder setTextColorByInt(@IdRes int viewId, @ColorInt int textColor) {
        this.<TextView>findViewById(viewId).setTextColor(textColor);
        return this;
    }

    /**
     * 设置文本大小
     *
     * @param viewId
     * @param textSize
     * @return
     */
    public final FasterHolder setTextSize(@IdRes int viewId, float textSize) {
        this.<TextView>findViewById(viewId).setTextSize(textSize);
        return this;
    }

    /**
     * 设置背景
     *
     * @param viewId
     * @return
     */
    public FasterHolder setBackground(@IdRes int viewId, Drawable background) {
        ViewCompat.setBackground(this.findViewById(viewId), background);
        return this;
    }

    /**
     * 设置进度
     *
     * @param viewId
     * @param progress
     * @return
     */
    public final FasterHolder setProgress(@IdRes int viewId, int progress) {
        this.<ProgressBar>findViewById(viewId).setProgress(progress);
        return this;
    }

    /**
     * 设置是否可见
     *
     * @param viewId
     * @param visible
     * @return
     */
    public final FasterHolder setVisible(@IdRes int viewId, boolean visible) {
        findViewById(viewId).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        return this;
    }

    /**
     * 设置是否占位
     *
     * @param viewId
     * @param visible
     * @return
     */
    public final FasterHolder setGone(@IdRes int viewId, boolean visible) {
        findViewById(viewId).setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    /**
     * 设置是否被选中
     *
     * @param viewId
     * @param checked
     * @return
     */
    public final FasterHolder setChecked(@IdRes int viewId, boolean checked) {
        this.<CompoundButton>findViewById(viewId).setChecked(checked);
        return this;
    }

    /**
     * 设置是否被选中
     *
     * @param viewId
     * @param selected
     * @return
     */
    public final FasterHolder setSelected(@IdRes int viewId, boolean selected) {
        findViewById(viewId).setSelected(selected);
        return this;
    }

    /**
     * 设置是否可用
     *
     * @param viewId
     * @param enabled
     * @return
     */
    public final FasterHolder setEnabled(@IdRes int viewId, boolean enabled) {
        findViewById(viewId).setEnabled(enabled);
        return this;
    }

    /**
     * 设置透明度
     *
     * @param viewId
     * @param alpha
     * @return
     */
    public final FasterHolder setAlpha(@IdRes int viewId, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        findViewById(viewId).setAlpha(alpha);
        return this;
    }

    /**
     * 设置额外数据
     *
     * @param viewId
     * @param tag
     */
    public final FasterHolder setTag(@IdRes int viewId, Object tag) {
        findViewById(viewId).setTag(tag);
        return this;
    }

    /**
     * 设置额外数据
     *
     * @param viewId
     * @param key
     * @param tag
     */
    public final FasterHolder setTag(@IdRes int viewId, int key, Object tag) {
        findViewById(viewId).setTag(key, tag);
        return this;
    }

    /**
     * 设置点击监听
     *
     * @param viewId
     * @param listener
     * @return
     */
    public final FasterHolder setOnClickListener(@IdRes int viewId, View.OnClickListener listener) {
        findViewById(viewId).setOnClickListener(listener);
        return this;
    }

    /**
     * 设置长点击监听
     *
     * @param viewId
     * @param listener
     * @return
     */
    public final FasterHolder setOnLongClickListener(@IdRes int viewId, View.OnLongClickListener listener) {
        findViewById(viewId).setOnLongClickListener(listener);
        return this;
    }

    /**
     * 设置额外存储数据
     *
     * @param key
     * @param extra
     */
    public final FasterHolder setExtra(int key, Object extra) {
        if (null == mExtra) {
            mExtra = new SparseArray<>(2);
        }
        mExtra.put(key, extra);
        return this;
    }

    /**
     * 获取额外存储数据
     *
     * @param key
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <T> T getExtra(int key) {
        if (null == mExtra) {
            return null;
        }
        return (T) mExtra.get(key);
    }

    /**
     * FasterHolder创建，可以扩展用于监听点击事件等等
     */
    protected void onCreate() {
    }

    /**
     * FasterHolder视图被回收时的回调
     */
    protected void onDetach() {
    }

    /**
     * FasterHolder资源被释放的回调
     */
    protected void onRecycle() {
    }
}
