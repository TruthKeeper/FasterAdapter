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
public class AlbumInsertStrategy extends Strategy<Void> {
    private OnInsertClickListener onInsertClickListener;

    @Override
    public int layoutId() {
        return R.layout.item_album_insert;
    }

    @Override
    protected FasterHolder onCreateHolder(ViewGroup parent) {
        return new FasterHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId(), parent, false)) {
            @Override
            protected void onCreate(View itemView) {
                setOnClickListener(R.id.btn_insert, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onInsertClickListener != null) {
                            onInsertClickListener.onInsert();
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onBindViewHolder(FasterHolder holder, Void data) {

    }

    public void setOnInsertClickListener(OnInsertClickListener onInsertClickListener) {
        this.onInsertClickListener = onInsertClickListener;
    }

    public interface OnInsertClickListener {
        void onInsert();
    }
}
