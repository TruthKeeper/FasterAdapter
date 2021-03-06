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

import com.tk.fasteradapter.CollectionUtils;
import com.tk.fasteradapter.Entry;
import com.tk.fasteradapter.FasterAdapter;
import com.tk.sampleadapter.layout.EmptyLayout;
import com.tk.sampleadapter.layout.ErrorLayout;
import com.tk.sampleadapter.layout.FooterLayout;
import com.tk.sampleadapter.layout.HeaderLayout;
import com.tk.sampleadapter.layout.LoadMoreLayout;
import com.tk.sampleadapter.strategy.UserNormalStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <pre>
 *      author : TK
 *      time : 2017/7/15
 *      desc : 单类型
 * </pre>
 */
public class SingleActivity extends AppCompatActivity implements FasterAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, FasterAdapter.OnLoadListener, Toolbar.OnMenuItemClickListener {
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView recyclerview;

    private Handler handler = new Handler();
    private FunctionDialog dialog;
    private FasterAdapter<User> adapter;

    private UserNormalStrategy strategy = new UserNormalStrategy();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        dialog = new FunctionDialog(this);
        dialog.setOnClickListener(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_controller);
        toolbar.setOnMenuItemClickListener(this);
        swipeLayout = findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(this);
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        swipeLayout.setRefreshing(true);

        adapter = FasterAdapter.<User>build()
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
        initData(false);
    }

    private void initData(final boolean diff) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                List<User> list = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    list.add(new User("ID：" + i, i, false, false, null));
                }
                if (diff) {
                    adapter.setSourceDataByDiff(list, strategy);
                } else {
                    adapter.setSourceData(list, strategy);
                }
            }
        }, 1_000);
    }

    @Override
    public void onClick(View v) {
        dialog.dismiss();
        switch (v.getId()) {
            case R.id.btn_init:
                swipeLayout.setRefreshing(true);
                initData(false);
                break;
            case R.id.btn_clear:
                adapter.clear();
                break;
            case R.id.btn_init_diff:
                swipeLayout.setRefreshing(true);
                initData(true);
                break;
            case R.id.btn_add_head:
                adapter.add(0, Entry.create(new User("新增ID：-1", -1, false, false, null), strategy));
                break;
            case R.id.btn_add_foot:
                adapter.add(Entry.create(new User("新增ID：-1", -1, false, false, null), strategy));
                break;
            case R.id.btn_add_random:
                int addIndex = adapter.getListSize() == 0 ? 0 : new Random().nextInt(adapter.getListSize());

                adapter.add(addIndex, Entry.create(new User("新增ID：-1", -1, false, false, null), strategy));
                break;
            case R.id.btn_add_random_list:
                int addIndexCollection = adapter.getListSize() == 0 ? 0 : new Random().nextInt(adapter.getListSize());

                List<Entry<User>> list = new ArrayList<>();
                list.add(Entry.create(new User("新增组ID：-1", -2, false, false, null), strategy));
                list.add(Entry.create(new User("新增组ID：-1", -3, false, false, null), strategy));
                adapter.addAll(addIndexCollection, list);
                break;
            case R.id.btn_remove_head:
                adapter.remove(0);
                break;
            case R.id.btn_remove_foot:
                adapter.remove(adapter.getListSize() - 1);
                break;
            case R.id.btn_remove_random:
                int removeIndex = new Random().nextInt(adapter.getListSize());

                adapter.remove(removeIndex);
                break;
            case R.id.btn_remove_if:
                adapter.removeIf(new CollectionUtils.Predicate<User>() {
                    @Override
                    public boolean process(User user) {
                        return user.getId() % 2 == 0;
                    }
                });
                break;
            case R.id.btn_add_header:
                adapter.addHeaderView(new HeaderLayout(this));
                break;
            case R.id.btn_remove_header:
                int headerSize = adapter.getHeaderViewChildCount();
                if (0 != headerSize) {
                    adapter.removeHeaderView(--headerSize);
                }
                break;
            case R.id.btn_remove_header_all:
                adapter.removeAllHeaderView();
                break;
            case R.id.btn_switch_header:
                adapter.setHeaderFront(!adapter.isHeaderFront());
                break;
            case R.id.btn_add_footer:
                adapter.addFooterView(new FooterLayout(this));
                break;
            case R.id.btn_remove_footer:
                int footerSize = adapter.getFooterViewChildCount();
                if (0 != footerSize) {
                    adapter.removeFooterView(--footerSize);
                }
                break;
            case R.id.btn_remove_footer_all:
                adapter.removeAllFooterView();
                break;
            case R.id.btn_switch_footer:
                adapter.setFooterFront(!adapter.isFooterFront());
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
                if (adapter.getListSize() >= 25) {
                    adapter.loadMoreEnd();
                } else if (new Random().nextInt(6) > 1) {
                    adapter.add(Entry.create(new User("新增ID：-1", -1, false, false, null), strategy));
                    adapter.loadMoreDismiss();
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
