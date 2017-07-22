package com.tk.sampleadapter.strategy;


import com.tk.fasteradapter.FasterHolder;
import com.tk.fasteradapter.Strategy;
import com.tk.sampleadapter.R;

/**
 * <pre>
 *      author : TK
 *      time : 2017/7/22
 *      desc :
 * </pre>
 */

public class MeiziStrategy extends Strategy<Integer> {
    @Override
    protected int getItemViewType() {
        return R.layout.item_meizi;
    }

    @Override
    public int layoutId() {
        return R.layout.item_meizi;
    }

    @Override
    public void onBindViewHolder(final FasterHolder holder, final Integer integer) {
        holder.setText(R.id.item, "ID：" + integer);
    }
}
