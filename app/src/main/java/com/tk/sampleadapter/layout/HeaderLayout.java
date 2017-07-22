package com.tk.sampleadapter.layout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.tk.sampleadapter.R;

/**
 * <pre>
 *     author : TK
 *     time   : 2017/07/15
 *     desc   : 头视图
 * </pre>
 */
public class HeaderLayout extends LinearLayout {
    public HeaderLayout(Context context) {
        this(context, null);
    }

    public HeaderLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(HORIZONTAL);
        setPadding(30, 30, 30, 30);
        LayoutInflater.from(context).inflate(R.layout.header_layout, this);
    }
}
