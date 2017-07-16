package com.tk.fasteradapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * <pre>
 *      author : TK
 *      time : 2017/07/15
 *      desc : 数据对应的视图实现策略
 * </pre>
 */

public abstract class Strategy<T> {
    protected int getItemViewType() {
        return 0;
    }

    public abstract int layoutId();

    protected FasterHolder onCreateHolder(ViewGroup parent) {
        //需要扩展FasterHolder时重写
        return new FasterHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId(), parent, false));
    }

    public abstract void onBindViewHolder(FasterHolder holder, T data);

}
