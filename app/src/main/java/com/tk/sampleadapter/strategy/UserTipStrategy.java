package com.tk.sampleadapter.strategy;


import com.tk.fasteradapter.FasterHolder;
import com.tk.fasteradapter.Strategy;
import com.tk.sampleadapter.R;

/**
 * <pre>
 *      author : TK
 *      time : 2018/1/31
 *      desc :
 * </pre>
 */

public class UserTipStrategy extends Strategy<String> {

    @Override
    public int layoutId() {
        return R.layout.item_user_tip;
    }

    @Override
    public void onBindViewHolder(final FasterHolder holder, final String string) {
        holder.setText(R.id.tip, string);
    }
}
