package com.tk.sampleadapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.tk.fasteradapter.Entry;
import com.tk.fasteradapter.FasterAdapter;
import com.tk.fasteradapter.ListCountTransformer;
import com.tk.fasteradapter.MultiType;
import com.tk.fasteradapter.Strategy;
import com.tk.sampleadapter.strategy.AlbumInsertStrategy;
import com.tk.sampleadapter.strategy.AlbumStrategy;
import com.tk.sampleadapter.strategy.UserNormalStrategy;

/**
 * <pre>
 *     author : TK
 *     time   : 2018/01/31
 *     desc   : xxxx描述
 * </pre>
 */
public class DynamicCountActivity extends AppCompatActivity {
    private RecyclerView recyclerviewAlbum;
    private RecyclerView recyclerviewHide;

    private AlbumStrategy albumStrategy;
    private AlbumInsertStrategy albumInsertStrategy;
    private FasterAdapter<Integer> albumAdapter;

    private UserNormalStrategy userNormalStrategy;
    private FasterAdapter<User> userAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_count);
        recyclerviewAlbum = findViewById(R.id.recyclerview_album);
        recyclerviewHide = findViewById(R.id.recyclerview_hide);
        recyclerviewAlbum.setHasFixedSize(true);
        recyclerviewHide.setHasFixedSize(true);
        recyclerviewAlbum.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerviewHide.setLayoutManager(new LinearLayoutManager(this));

        albumStrategy = new AlbumStrategy();
        albumInsertStrategy = new AlbumInsertStrategy();
        albumInsertStrategy.setOnInsertClickListener(new AlbumInsertStrategy.OnInsertClickListener() {
            @Override
            public void onInsert() {
                //插入一张图片
                //最大显示的数量
                int max = AlbumStrategy.IMAGE.length;
                if (albumAdapter.getListSize() == max - 1) {
                    //最后一张不能触发插入动画
                    albumAdapter.add(Entry.create(albumAdapter.getListSize(), albumStrategy), true);
                } else {
                    albumAdapter.add(Entry.create(albumAdapter.getListSize(), albumStrategy));
                }
            }
        });
        albumAdapter = FasterAdapter.<Integer>build()
                .emptyEnabled(false)
                .listCountTransformer(new ListCountTransformer() {
                    @Override
                    public int newListDataCount(int listDataCount) {
                        //最大显示的数量
                        int max = AlbumStrategy.IMAGE.length;
                        return Math.min(max, listDataCount + 1);
                    }

                    @Override
                    public Strategy<Void> newVoidDataStrategy() {
                        //多出来的无数据追加视图
                        return albumInsertStrategy;
                    }
                })
                .build();
        recyclerviewAlbum.setAdapter(albumAdapter);

        userNormalStrategy = new UserNormalStrategy();
        userAdapter = FasterAdapter.<User>build()
                .emptyEnabled(false)
                //单类型时也可以这样绑定（1）
                .bind(User.class, new MultiType<User>() {
                    @Override
                    public Strategy<User> bind(User user) {
                        return userNormalStrategy;
                    }
                })
                .listCountTransformer(new ListCountTransformer() {
                    @Override
                    public int newListDataCount(int listDataCount) {
                        return Math.min(listDataCount, 4);
                    }

                    @Override
                    public Strategy<Void> newVoidDataStrategy() {
                        return null;
                    }
                })
                .build();
        recyclerviewHide.setAdapter(userAdapter);
    }

    public void add(View v) {
        //单类型时也可以这样绑定（2）
        userAdapter.add(new User("新增ID：-1", -1, false, false, null));
        Toast.makeText(this, "当前Adapter数据集合大小：" + userAdapter.getListSize(), Toast.LENGTH_SHORT).show();
    }
}
