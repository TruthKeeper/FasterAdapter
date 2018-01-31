package com.tk.sampleadapter.strategy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tk.fasteradapter.FasterHolder;
import com.tk.fasteradapter.Strategy;
import com.tk.sampleadapter.R;

/**
 * <pre>
 *     author : TK
 *     time   : 2018/01/31
 *     desc   : xxxx描述
 * </pre>
 */
public class AlbumStrategy extends Strategy<Integer> {
    public static final int[] IMAGE = new int[]{R.drawable.emoji0,
            R.drawable.emoji1,
            R.drawable.emoji2,
            R.drawable.emoji3,
            R.drawable.emoji4,
            R.drawable.emoji5,
            R.drawable.emoji6,
            R.drawable.emoji7,
            R.drawable.emoji8};

    @Override
    public int layoutId() {
        return R.layout.item_album_content;
    }

    @Override
    protected FasterHolder createHolder(ViewGroup parent) {
        return new FasterHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId(), parent, false)) {
            @Override
            protected void onCreate() {
                setOnLongClickListener(R.id.content, new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        getAdapter().remove(getListPosition());
                        return false;
                    }
                });
            }
        };
    }

    @Override
    public void onBindViewHolder(FasterHolder holder, Integer data) {
        holder.setImage(R.id.content, IMAGE[data % IMAGE.length]);
    }
}