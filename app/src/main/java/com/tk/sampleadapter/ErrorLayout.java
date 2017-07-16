package com.tk.sampleadapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * <pre>
 *     author : TK
 *     time   : 2017/07/15
 *     desc   : 错误视图
 * </pre>
 */
public class ErrorLayout extends LinearLayout {
    public ErrorLayout(Context context) {
        this(context, null);
    }

    public ErrorLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ErrorLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.error_layout, this);
    }
}
