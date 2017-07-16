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
 *     desc   : 足视图
 * </pre>
 */
public class FooterLayout extends LinearLayout {
    public FooterLayout(Context context) {
        this(context, null);
    }

    public FooterLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(HORIZONTAL);
        setPadding(30, 30, 30, 30);
        LayoutInflater.from(context).inflate(R.layout.footer_layout, this);
    }
}
