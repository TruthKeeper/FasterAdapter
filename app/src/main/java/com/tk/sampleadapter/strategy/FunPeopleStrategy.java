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

public class FunPeopleStrategy extends Strategy<String> {
    @Override
    protected int getItemViewType() {
        return R.layout.item_fun_people;
    }

    @Override
    public int layoutId() {
        return R.layout.item_fun_people;
    }


    @Override
    public void onBindViewHolder(final FasterHolder holder, final String string) {
        holder.setText(R.id.item, "吃瓜群众：" + string);
    }
}
