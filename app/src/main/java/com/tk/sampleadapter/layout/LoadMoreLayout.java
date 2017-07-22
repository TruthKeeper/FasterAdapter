package com.tk.sampleadapter.layout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tk.fasteradapter.ILoadMore;
import com.tk.sampleadapter.R;

/**
 * <pre>
 *     author : TK
 *     time   : 2017/07/15
 *     desc   : 上拉加载
 * </pre>
 */
public class LoadMoreLayout extends LinearLayout implements ILoadMore {
    private ImageView error;
    private ProgressBar progressbar;
    private TextView content;

    public LoadMoreLayout(Context context) {
        this(context, null);
    }

    public LoadMoreLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.load_more_layout, this);
        error = (ImageView) findViewById(R.id.error);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        content = (TextView) findViewById(R.id.content);

    }

    @Override
    public void onShow() {
        error.setVisibility(GONE);
        progressbar.setVisibility(VISIBLE);
        content.setText("加载中");
    }

    @Override
    public void onDismiss() {

    }

    @Override
    public void onLoadEnd() {
        error.setVisibility(GONE);
        progressbar.setVisibility(GONE);
        content.setText("-我是有底线的-");
    }

    @Override
    public void onFailure() {
        error.setVisibility(VISIBLE);
        progressbar.setVisibility(GONE);
        content.setText("加载失败，点击重试");
    }
}
