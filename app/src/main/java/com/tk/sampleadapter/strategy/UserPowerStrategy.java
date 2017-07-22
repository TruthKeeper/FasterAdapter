package com.tk.sampleadapter.strategy;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

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

public class UserPowerStrategy extends Strategy<User> {
    @Override
    protected int getItemViewType() {
        return R.layout.item_user_power;
    }

    @Override
    public int layoutId() {
        return R.layout.item_user_power;
    }

    @Override
    protected FasterHolder onCreateHolder(ViewGroup parent) {
        return new FasterHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId(), parent, false)) {
            @Override
            protected void onCreate(View itemView) {
                //不在onBindViewHolder里面是因为在创建时定义相对节省性能
                this.<CompoundButton>findViewById(R.id.cbx_heart).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            getAdapter().getObjectArray().put(getAdapterPosition(), true);
                        } else {
                            getAdapter().getObjectArray().delete(getAdapterPosition());
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onBindViewHolder(final FasterHolder holder, final User data) {
        holder.setText(R.id.item, "大佬： " + data.getNickname())
                .<CompoundButton>findViewById(R.id.cbx_heart)
                .setChecked((Boolean) holder.getAdapter().getObjectArray().get(holder.getAdapterPosition(), false));
    }
}
