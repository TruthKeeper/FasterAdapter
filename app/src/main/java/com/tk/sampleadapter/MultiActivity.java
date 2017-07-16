package com.tk.sampleadapter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.tk.fasteradapter.FasterAdapter;
/**
 * <pre>
 *      author : TK
 *      time : 2017/7/15
 *      desc :
 * </pre>
 */
public class MultiActivity extends AppCompatActivity implements FasterAdapter.OnItemClickListener {
    private RecyclerView recyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onClick(FasterAdapter adapter, View view, int position) {
        Log.e("onClick", "position：" + position);
    }
}
