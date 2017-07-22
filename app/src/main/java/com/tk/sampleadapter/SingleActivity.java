package com.tk.sampleadapter;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.tk.fasteradapter.Entry;
import com.tk.fasteradapter.FasterAdapter;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_controller);
        toolbar.setOnMenuItemClickListener(this);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(this);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        swipeLayout.setRefreshing(true);

        adapter = new FasterAdapter.Builder<User>()
                .itemClickListener(this)
                .emptyView(new EmptyLayout(this))
                .errorView(new ErrorLayout(this))
                .loadMoreView(new LoadMoreLayout(this))
                .loadListener(this)
                .build();
        recyclerview.setAdapter(adapter);

        onRefresh();
    }

    @Override
    public void onClick(FasterAdapter adapter, View view, int listPosition) {
        Toast.makeText(this, "点击了列表中第" + listPosition + "项", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                List<User> list = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    list.add(new User("第" + (i + 1) + "个菜鸡", i, 0));
                }
                adapter.setData(FasterAdapter.fillBySingleStrategy(list, strategy));
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
                List<User> list = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    list.add(new User("第" + (i + 1) + "个菜鸡", i, 0));
                }
                adapter.setDataByDiff(FasterAdapter.fillBySingleStrategy(list, strategy));
                break;
            case R.id.btn_add:
                adapter.add(new Entry<User>(new User("新增的菜鸡", 1, 0), strategy));
                break;
            case R.id.btn_remove:
                if (0 == adapter.getListSnapSize()) {
                    return;
                }
                adapter.remove(adapter.getListSnapSize() - 1);
                break;
            case R.id.btn_add_random:
                int addIndex = adapter.getListSnapSize() == 0 ? 0 : new Random().nextInt(adapter.getListSnapSize());
                adapter.add(addIndex, new Entry<User>(new User("新增的菜鸡", 1, 0), strategy));
                break;
            case R.id.btn_remove_random:
                if (0 == adapter.getListSnapSize()) {
                    return;
                }
                int removeIndex = new Random().nextInt(adapter.getListSnapSize());
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
                if (adapter.getListSnapSize() >= 22) {
                    adapter.loadMoreEnd();
                } else {
                    adapter.loadMoreDismiss();
                    adapter.add(new Entry<User>(new User("新增的菜鸡", 1, 0), strategy));
                }
            }
        }, 1000);
    }

    @Override
    public void onReLoad() {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_switch:
                break;
            case R.id.btn_function:
                dialog.show();
                break;
        }
        return false;
    }
}
