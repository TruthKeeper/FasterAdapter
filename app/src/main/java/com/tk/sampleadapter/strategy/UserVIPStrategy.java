package com.tk.sampleadapter.strategy;


import com.tk.fasteradapter.FasterHolder;
import com.tk.fasteradapter.Strategy;
import com.tk.sampleadapter.R;
import com.tk.sampleadapter.User;

/**
 * <pre>
 *      author : TK
 *      time : 2017/7/22
 *      desc :
 * </pre>
 */

public class UserVIPStrategy extends Strategy<User> {
    @Override
    public int layoutId() {
        return R.layout.item_user_vip;
    }

    @Override
    public void onBindViewHolder(final FasterHolder holder, final User data) {
        holder.setText(R.id.name, data.getNickname())
                .setText(R.id.content, data.getDesc());
    }
}
