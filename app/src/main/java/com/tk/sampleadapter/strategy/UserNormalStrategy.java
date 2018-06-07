package com.tk.sampleadapter.strategy;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public int layoutId() {
        return R.layout.item_user_normal;
    }

    @Override
    protected FasterHolder onCreateHolder(ViewGroup parent) {
        return new FasterHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId(), parent, false)) {
            @Override
            protected void onCreate(View itemView) {
                setOnClickListener(R.id.btn_delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       getAdapter().remove(getListPosition());
                    }
                });
            }
        };
    }

    @Override
    public void onBindViewHolder(final FasterHolder holder, final User data) {
        holder.setText(R.id.name, "普通用户： " + data.getNickname());
    }
}
