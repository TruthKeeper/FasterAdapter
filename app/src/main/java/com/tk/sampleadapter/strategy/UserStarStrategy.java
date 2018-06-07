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
 *      time : 2018/1/31
 *      desc :
 * </pre>
 */

public class UserStarStrategy extends Strategy<User> {

    @Override
    public int layoutId() {
        return R.layout.item_user_star;
    }

    @Override
    protected FasterHolder onCreateHolder(ViewGroup parent) {
        return new FasterHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId(), parent, false)) {
            @Override
            protected void onCreate(View itemView) {
                final FasterHolder holder = this;
                setOnClickListener(R.id.btn_star, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) getAdapter().getListItem(getListPosition());
                        if (isSelect(holder, user.getId())) {
                            v.setSelected(false);
                            unSelect(holder, user.getId());
                        } else {
                            v.setSelected(true);
                            select(holder, user.getId());
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onBindViewHolder(final FasterHolder holder, final User data) {
        holder.setText(R.id.name, "明星： " + data.getNickname())
                .setSelected(R.id.btn_star, isSelect(holder, data.getId()));
    }
}
