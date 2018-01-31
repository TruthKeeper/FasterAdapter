package com.tk.sampleadapter;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.tk.fasteradapter.FasterAdapter;
import com.tk.fasteradapter.MultiType;
import com.tk.fasteradapter.Strategy;
import com.tk.sampleadapter.layout.EmptyLayout;
import com.tk.sampleadapter.layout.ErrorLayout;
import com.tk.sampleadapter.layout.FooterLayout;
import com.tk.sampleadapter.layout.HeaderLayout;
import com.tk.sampleadapter.layout.LoadMoreLayout;
import com.tk.sampleadapter.strategy.UserNormalStrategy;
import com.tk.sampleadapter.strategy.UserTipStrategy;
import com.tk.sampleadapter.strategy.UserVIPStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <pre>
 *      author : TK
 *      time : 2017/7/22
 *      desc :
 * </pre>
 */
public class MultiBindActivity extends AppCompatActivity implements FasterAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, FasterAdapter.OnLoadListener, Toolbar.OnMenuItemClickListener {
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView recyclerview;

    private Handler handler = new Handler();
    private FunctionDialog dialog;
    private FasterAdapter<Object> adapter;

    private UserNormalStrategy normalStrategy = new UserNormalStrategy();
    private UserVIPStrategy vipStrategy = new UserVIPStrategy();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_bind);
        dialog = new FunctionDialog(this);
        dialog.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_controller);
        toolbar.setOnMenuItemClickListener(this);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(this);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        swipeLayout.setRefreshing(true);

        adapter = new FasterAdapter.Builder<>()
                .bind(String.class, new UserTipStrategy())
                .bind(User.class, new MultiType<User>() {
                    @Override
                    public Strategy<User> bind(User user) {
                        if (user.isVip()) {
                            return vipStrategy;
                        } else {
                            return normalStrategy;
                        }
                    }
                })
                .itemClickListener(this)
                .emptyView(new EmptyLayout(this))
                .errorView(new ErrorLayout(this))
                .loadMoreEnabled(true)
                .loadMoreView(new LoadMoreLayout(this))
                .loadListener(this)
                .build();
        recyclerview.setAdapter(adapter);

        onRefresh();
    }

    @Override
    public void onItemClick(FasterAdapter adapter, View view, int listPosition) {
        Toast.makeText(this, "点击了列表中第" + listPosition + "项", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                List<Object> list = new ArrayList<Object>();
                //根据需求设置不同的视图装载策略
                for (int i = 0; i < 20; i++) {
                    if (0 == i % 2) {
                        list.add(new User("ID：" + i, i, false, false, null));
                    } else if (0 == i % 3) {
                        list.add(new User("ID：" + i, i, true, false, "VIP简介：哈哈哈哈"));
                    } else {
                        list.add("现在充值成为VIP还可以抽大奖~");
                    }
                }
                adapter.setData(list, null);
            }
        }, 1_000);
    }


    @Override
    public void onClick(View v) {
        dialog.dismiss();
        switch (v.getId()) {
            case R.id.btn_init:
                swipeLayout.setRefreshing(true);
                onRefresh();
                break;
            case R.id.btn_clear:
                adapter.clear();
                break;
            case R.id.btn_init_random:
                //diff
                //根据需求设置不同的视图装载策略
                List<Object> list = new ArrayList<Object>();
                for (int i = 0; i < 20; i++) {
                    if (0 == i % 2) {
                        list.add(new User("ID：" + i, i, false, false, null));
                    } else if (0 == i % 3) {
                        list.add(new User("ID：" + i, i, true, false, "VIP简介：哈哈哈哈"));
                    } else {
                        list.add("现在充值成为VIP还可以抽大奖~");
                    }
                }
                adapter.setData(list, null);
                break;
            case R.id.btn_add:
                adapter.add("快去充值呀");
                break;
            case R.id.btn_remove:
                if (0 == adapter.getListSize()) {
                    return;
                }
                adapter.remove(adapter.getListSize() - 1);
                break;
            case R.id.btn_add_random:
                int addIndex = adapter.getListSize() == 0 ? 0 : new Random().nextInt(adapter.getListSize());
                adapter.add(addIndex, "快去充值呀");
                break;
            case R.id.btn_remove_random:
                if (0 == adapter.getListSize()) {
                    return;
                }
                int removeIndex = new Random().nextInt(adapter.getListSize());
                adapter.remove(removeIndex);
                break;
            case R.id.btn_add_header:
                adapter.addHeaderView(new HeaderLayout(this));
                break;
            case R.id.btn_remove_header:
                int headerSize = adapter.getHeaderViewSize();
                if (0 != headerSize) {
                    adapter.removeHeaderView(--headerSize);
                }
                break;
            case R.id.btn_add_footer:
                adapter.addFooterView(new FooterLayout(this));
                break;
            case R.id.btn_remove_footer:
                int footerSize = adapter.getFooterViewSize();
                if (0 != footerSize) {
                    adapter.removeFooterView(--footerSize);
                }
                break;
            case R.id.btn_switch:
                adapter.setHeaderFooterFront(!adapter.isHeaderFooterFront());
                break;
            case R.id.btn_empty_switch:
                adapter.setEmptyEnabled(!adapter.isEmptyEnabled());
                break;
            case R.id.btn_error_switch:
                adapter.setDisplayError(!adapter.isDisplayError());
                break;
        }
    }

    @Override
    public void onLoad() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getListSize() >= 22) {
                    adapter.loadMoreEnd();
                } else if (new Random().nextInt(6) > 1) {
                    adapter.loadMoreDismiss();
                    adapter.add("快去充值呀");
                } else {
                    adapter.loadMoreFailure();
                }
            }
        }, 1000);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_switch:
                if (recyclerview.getLayoutManager() instanceof GridLayoutManager) {
                    recyclerview.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                } else if (recyclerview.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    recyclerview.setLayoutManager(new LinearLayoutManager(this));
                } else {
                    recyclerview.setLayoutManager(new GridLayoutManager(this, 2));
                }
                //LayoutManager发生变化，手动调用
                adapter.onAttachedToRecyclerView(recyclerview);
                break;
            case R.id.btn_function:
                dialog.show();
                break;
        }
        return false;
    }
}
