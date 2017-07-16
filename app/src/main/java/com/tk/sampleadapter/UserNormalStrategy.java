package com.tk.sampleadapter;


import android.view.View;

import com.tk.fasteradapter.FasterHolder;
import com.tk.fasteradapter.Strategy;

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
        return 1;
    }

    @Override
    public int layoutId() {
        return R.layout.item_user_normal;
    }

    @Override
    public void onBindViewHolder(final FasterHolder holder, final User data) {
        holder.setText(R.id.item, "普通用户：\n" + data.getNickname() + "\n性别：" + (data.isGender() ? "男" : "女"))
                .setOnClickListener(R.id.btn_delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.getAdapter().remove(data);
                    }
                });
    }
}
