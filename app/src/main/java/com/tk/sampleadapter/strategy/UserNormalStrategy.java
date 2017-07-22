package com.tk.sampleadapter.strategy;


import android.view.View;

import com.tk.fasteradapter.FasterHolder;
import com.tk.fasteradapter.Strategy;
import com.tk.sampleadapter.R;
import com.tk.sampleadapter.User;

/**
 * <pre>
 *      author : TK
 *      time : 2017/7/15
 *      desc :
 * </pre>
 */

public class UserNormalStrategy extends Strategy<User> {
    @Override
    protected int getItemViewType() {
        return R.layout.item_user_normal;
    }

    @Override
    public int layoutId() {
        return R.layout.item_user_normal;
    }


    @Override
    public void onBindViewHolder(final FasterHolder holder, final User data) {
        holder.setText(R.id.item, "菜鸡： " + data.getNickname())
                .setOnClickListener(R.id.btn_delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.getAdapter().remove(data);
                    }
                });
    }
}
