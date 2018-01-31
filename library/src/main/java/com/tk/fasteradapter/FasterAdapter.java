package com.tk.fasteradapter;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * <pre>
 *      author : TK
 *      time : 2017/07/15
 *      desc : <ul>
 *          <li>支持占位(空、错误)视图</li>
 *          <li>支持追加(头部、足部)视图</li>
 *          <li>支持占位视图和追加视图的优先级调整(无数据时是否显示头部、底部，默认不显示)</li>
 *          <li>支持上拉加载视图以及监听</li>
 *          <li>支持上拉加载内置异常处理：点击LoadMoreView重试，回调onLoad方法</li>
 *          <li>支持数据类型与布局类型的绑定，一对一，一对多，多对多的方式</li>
 *          <li>封装通用的点击、长按事件</li>
 *          <li>封装常用的集合 API</li>
 *          <li>封装常用的ViewHolder API</li>
 *          <li>类似选中业务场景下，封装SparseArray记录对FasterHolder的数据保存</li>
 *          <li>final不允许继承FasterAdapter，支持Adapter变长模式{@link FasterAdapter.Builder#listCountTransformer(ListCountTransformer)}</li>
 *          </ul>
 * </pre>
 */

public final class FasterAdapter<T> extends RecyclerView.Adapter<FasterHolder> {
    private static final String TAG = "FasterAdapter";
    /**
     * 开发时注意不要占用
     */
    public static final int TYPE_EMPTY = -10000;
    public static final int TYPE_ERROR = -10001;
    public static final int TYPE_HEADER = -10002;
    public static final int TYPE_FOOTER = -10003;
    public static final int TYPE_LOAD = -10004;
    public static final int TYPE_VOID_DATA = -10005;
    public static final int[] INNER_TYPE = new int[]{TYPE_EMPTY, TYPE_ERROR, TYPE_HEADER, TYPE_FOOTER, TYPE_LOAD, TYPE_VOID_DATA};

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.LOAD_IDLE, Status.LOAD_ERROR, Status.LOAD_END, Status.LOAD_ING})
    public @interface Status {
        /**
         * 待命
         */
        int LOAD_IDLE = 0x00;
        /**
         * 加载失败
         */
        int LOAD_ERROR = 0x01;
        /**
         * 加载完毕
         */
        int LOAD_END = 0x02;
        /**
         * 加载中
         */
        int LOAD_ING = 0x03;
    }

    /**
     * 上拉加载的状态
     */
    private int mLoadStatus = Status.LOAD_IDLE;

    private OnItemClickListener mOnItemClickListener = null;
    private OnItemLongClickListener mOnItemLongClickListener = null;
    private OnLoadListener mOnLoadListener = null;

    private FrameLayout mEmptyContainer = null;
    private FrameLayout mErrorContainer = null;
    private LinearLayout mHeaderContainer = null;
    private LinearLayout mFooterContainer = null;
    private FrameLayout mLoadMoreContainer = null;
    /**
     * ILoadMore接口
     */
    private ILoadMore mILoadMore = null;
    /**
     * 空视图的包裹容器高度是否是{@link ViewGroup.LayoutParams#MATCH_PARENT},充满RecyclerView
     * <br>
     * 一般不用对其设置，只有在有头、足视图时需要注意
     */
    private boolean isEmptyMatchParent;
    /**
     * 错误视图的包裹容器高度是否是{@link ViewGroup.LayoutParams#MATCH_PARENT},充满RecyclerView
     * <br>
     * 一般不用对其设置，只有在有头、足视图时需要注意
     */
    private boolean isErrorMatchParent;
    /**
     * 是否启用空视图
     */
    private boolean isEmptyEnabled;
    /**
     * 头、足视图优先级是否大于占位图（空视图、错误视图）,true && 无数据时既显示头、足又显示空视图
     */
    private boolean headerFooterFront;

    /**
     * 是否启用上拉加载
     */
    private boolean isLoadMoreEnabled = false;
    /**
     * 数据封装集合
     */
    private List<Entry<T>> mList = null;
    /**
     * 多数据类型和多视图类型的一对多、多对多绑定关系
     */
    private final ArrayMap<Class, Pair<Strategy, MultiType>> mBindMap;
    /**
     * 是否显示错误视图，未设置errorView时无效
     */
    private boolean isDisplayError = false;
    /**
     * 存放对FasterHolder额外数据保存的Array
     */
    private final LongSparseArray<Object> array;
    /**
     * 上拉加载的滚动监听
     */
    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            if (null == mLayoutManager) {
                return;
            }
            int lastVisibleItemPosition = 0;
            int firstCompletelyVisibleItemPosition = 0;
            if (mLayoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager manager = (LinearLayoutManager) mLayoutManager;
                lastVisibleItemPosition = manager.findLastVisibleItemPosition();
                firstCompletelyVisibleItemPosition = manager.findFirstCompletelyVisibleItemPosition();

            } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) mLayoutManager;
                int[] pos = new int[manager.getSpanCount()];
                manager.findLastVisibleItemPositions(pos);
                Arrays.sort(pos);
                lastVisibleItemPosition = pos[manager.getSpanCount() - 1];
                manager.findFirstCompletelyVisibleItemPositions(pos);
                Arrays.sort(pos);
                firstCompletelyVisibleItemPosition = pos[0];
            }
            /*
             * 满足条件
             */
            if (SCROLL_STATE_IDLE == newState
                    && 1 == getLoadMoreSpace()
                    && 0 == Math.max(getEmptySpace(), getErrorSpace())
                    && (mLoadStatus == Status.LOAD_IDLE)
                    && (lastVisibleItemPosition == getItemCount() - 1
                    && 0 != firstCompletelyVisibleItemPosition)) {
                //开始加载
                mLoadStatus = Status.LOAD_ING;
                mLoadMoreContainer.setVisibility(VISIBLE);
                mILoadMore.onShow();
                if (null != mOnLoadListener) {
                    mOnLoadListener.onLoad();
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };
    /**
     * 布局管理器
     */
    private RecyclerView.LayoutManager mLayoutManager;
    /**
     * List集合数据大小变化器
     */
    private final ListCountTransformer mListCountTransformer;

    private FasterAdapter(Builder<T> builder) {
        mOnItemClickListener = builder.itemClickListener;
        mOnItemLongClickListener = builder.itemLongClickListener;
        mOnLoadListener = builder.loadListener;
        isEmptyMatchParent = builder.emptyMatchParent;
        isErrorMatchParent = builder.errorMatchParent;
        if (null != builder.emptyView) {
            mEmptyContainer = new FrameLayout(builder.emptyView.getContext());
            mEmptyContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, isEmptyMatchParent ? MATCH_PARENT : WRAP_CONTENT));
            putViewInGroup(mEmptyContainer, builder.emptyView);
        }
        if (null != builder.errorView) {
            mErrorContainer = new FrameLayout(builder.errorView.getContext());
            mErrorContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, isErrorMatchParent ? MATCH_PARENT : WRAP_CONTENT));
            putViewInGroup(mErrorContainer, builder.errorView);
        }
        if (null != builder.headerViews && (!builder.headerViews.isEmpty())) {
            mHeaderContainer = new LinearLayout(builder.headerViews.get(0).getContext());
            mHeaderContainer.setOrientation(LinearLayout.VERTICAL);
            mHeaderContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            for (View child : builder.headerViews) {
                putViewInGroup(mHeaderContainer, child);
            }
        }
        if (null != builder.footerViews && (!builder.footerViews.isEmpty())) {
            mFooterContainer = new LinearLayout(builder.footerViews.get(0).getContext());
            mFooterContainer.setOrientation(LinearLayout.VERTICAL);
            mFooterContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            for (View child : builder.footerViews) {
                putViewInGroup(mFooterContainer, child);
            }
        }
        if (null != builder.loadMoreView) {
            mLoadMoreContainer = new FrameLayout(builder.loadMoreView.getContext());
            mLoadMoreContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            putViewInGroup(mLoadMoreContainer, builder.loadMoreView);
            mLoadMoreContainer.setVisibility(View.GONE);
            builder.loadMoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMoreWhileFailure();
                }
            });
            mILoadMore = (ILoadMore) builder.loadMoreView;
        }

        isEmptyEnabled = builder.emptyEnabled;
        headerFooterFront = builder.headerFooterFront;
        isLoadMoreEnabled = builder.loadMoreEnabled;
        if (null == builder.list) {
            mList = new ArrayList<>();
        } else {
            mList = builder.list;
        }
        mBindMap = builder.bindMap;
        array = new LongSparseArray<>(2);
        mListCountTransformer = builder.listCountTransformer;
    }

    public static <D> FasterAdapter.Builder<D> build() {
        return new FasterAdapter.Builder<D>();
    }

    /**
     * 放入Group
     *
     * @param viewGroup
     * @param child
     */
    private void putViewInGroup(ViewGroup viewGroup, View child) {
        putViewInGroup(viewGroup, child, -1);
    }

    /**
     * 放入Group
     *
     * @param viewGroup
     * @param child
     * @param index
     */
    private void putViewInGroup(ViewGroup viewGroup, View child, int index) {
        viewGroup.addView(child, index);
    }

    /**
     * 设置空视图（占位视图）
     *
     * @param emptyView
     */
    public void setEmptyView(@Nullable View emptyView) {
        //当前是否需要显示空视图
        final boolean needShowEmpty = isEmptyEnabled && (!isDisplayError) && EmptyUtils.isEmpty(mList);
        if (null == emptyView) {
            //移除空视图操作
            if (!ViewUtils.isEmpty(mEmptyContainer)) {
                mEmptyContainer.removeAllViews();
                if (needShowEmpty) {
                    //立即刷新
                    notifyDataSetChanged();
                }
            }
        } else {
            if (null == mEmptyContainer) {
                //添加
                mEmptyContainer = new FrameLayout(emptyView.getContext());
                mEmptyContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, isEmptyMatchParent ? MATCH_PARENT : WRAP_CONTENT));
                putViewInGroup(mEmptyContainer, emptyView);
                if (needShowEmpty) {
                    notifyDataSetChanged();
                }
            } else {
                mEmptyContainer.removeAllViews();
                putViewInGroup(mEmptyContainer, emptyView);
            }
        }
    }

    /**
     * 启用空视图（占位视图）
     *
     * @param emptyEnabled
     */
    public void setEmptyEnabled(boolean emptyEnabled) {
        if (this.isEmptyEnabled != emptyEnabled) {
            //当前是否应该显示空视图
            boolean needShowEmpty = !ViewUtils.isEmpty(mEmptyContainer) && (!isDisplayError) && EmptyUtils.isEmpty(mList);
            isEmptyEnabled = emptyEnabled;
            if (needShowEmpty) {
                notifyDataSetChanged();
            }
        }
    }

    /**
     * 是否启用空视图
     *
     * @return
     */
    public boolean isEmptyEnabled() {
        return isEmptyEnabled;
    }

    /**
     * 设置错误视图（占位视图）
     *
     * @param errorView
     */
    public void setErrorView(@Nullable View errorView) {
        if (null == errorView) {
            //移除错误视图
            if (!ViewUtils.isEmpty(mErrorContainer)) {
                mErrorContainer.removeAllViews();
                if (isDisplayError) {
                    isDisplayError = false;
                    notifyDataSetChanged();
                }
            }
        } else {
            if (null == mErrorContainer) {
                mErrorContainer = new FrameLayout(errorView.getContext());
                mErrorContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, isErrorMatchParent ? MATCH_PARENT : WRAP_CONTENT));
                putViewInGroup(mErrorContainer, errorView);
                if (isDisplayError) {
                    notifyDataSetChanged();
                }
            } else {
                mErrorContainer.removeAllViews();
                putViewInGroup(mErrorContainer, errorView);
            }
        }
    }

    /**
     * 显示错误视图(占位视图)，立即生效
     *
     * @param displayError
     */
    public void setDisplayError(boolean displayError) {
        if (this.isDisplayError != displayError) {
            if (ViewUtils.isEmpty(mErrorContainer)) {
                return;
            }
            isDisplayError = displayError;
            notifyDataSetChanged();
        }
    }

    /**
     * 是否显示错误视图
     *
     * @return
     */
    public boolean isDisplayError() {
        return isDisplayError;
    }

    /**
     * 设置占位视图优先级
     *
     * @param headerFooterFront
     */
    public void setHeaderFooterFront(boolean headerFooterFront) {
        if (this.headerFooterFront != headerFooterFront) {
            this.headerFooterFront = headerFooterFront;
            if (1 == Math.max(getHeaderSpace(), getFooterSpace())
                    && 1 == Math.max(getEmptySpace(), getErrorSpace())) {
                //刷新
                notifyDataSetChanged();
            }
        }
    }

    /**
     * 获取占位视图的优先级
     *
     * @return
     */
    public boolean isHeaderFooterFront() {
        return headerFooterFront;
    }

    /**
     * 添加头视图
     *
     * @param headerView
     */
    public void addHeaderView(@NonNull View headerView) {
        addHeaderView(0 == getHeaderSpace() ? 0 : mHeaderContainer.getChildCount() - 1, headerView);
    }

    /**
     * 添加头视图
     *
     * @param index
     * @param headerView
     */
    public void addHeaderView(int index, @NonNull View headerView) {
        boolean init = 0 == getHeaderSpace();
        boolean emptyShow = 1 == getEmptySpace();
        boolean errorShow = 1 == getErrorSpace();
        if (null == mHeaderContainer) {
            mHeaderContainer = new LinearLayout(headerView.getContext());
            mHeaderContainer.setOrientation(LinearLayout.VERTICAL);
            mHeaderContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
        putViewInGroup(mHeaderContainer, headerView, index);
        if (init) {
            if (emptyShow || errorShow) {
                if (headerFooterFront) {
                    notifyItemInserted(0);
                } else {
                    //避免占位视图ItemAnimator
                    notifyDataSetChanged();
                }
            } else {
                notifyItemInserted(0);
            }
        }
    }

    /**
     * 移除头视图
     *
     * @param headerView
     */
    public void removeHeaderView(@NonNull View headerView) {
        if (0 == getHeaderSpace()) {
            return;
        }
        removeHeaderView(mHeaderContainer.indexOfChild(headerView));
    }

    /**
     * 移除头视图
     *
     * @param index
     */
    public void removeHeaderView(int index) {
        if (0 > index || 0 == getHeaderSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptySpace();
        boolean errorShow = 1 == getErrorSpace();
        mHeaderContainer.removeViewAt(index);
        if (0 == mHeaderContainer.getChildCount()) {
            if (emptyShow || errorShow) {
                if (headerFooterFront) {
                    notifyItemRemoved(0);
                }
            } else {
                notifyItemRemoved(0);
            }
        }
    }

    /**
     * 移除所有头视图
     */
    public void removeAllHeaderView() {
        if (0 == getHeaderSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptySpace();
        boolean errorShow = 1 == getErrorSpace();
        mHeaderContainer.removeAllViews();
        if (emptyShow || errorShow) {
            if (headerFooterFront) {
                notifyItemRemoved(0);
            }
        } else {
            notifyItemRemoved(0);
        }
    }

    /**
     * 获取头视图数量
     *
     * @return
     */
    public int getHeaderViewSize() {
        if (1 == getHeaderSpace()) {
            return mHeaderContainer.getChildCount();
        }
        return 0;
    }

    /**
     * 添加足视图
     *
     * @param footerView
     */
    public void addFooterView(@NonNull View footerView) {
        addFooterView(0 == getFooterSpace() ? 0 : mFooterContainer.getChildCount() - 1, footerView);
    }

    /**
     * 添加足视图
     *
     * @param index
     * @param footerView
     */
    public void addFooterView(int index, @NonNull View footerView) {
        boolean init = 0 == getFooterSpace();
        boolean emptyShow = 1 == getEmptySpace();
        boolean errorShow = 1 == getErrorSpace();
        if (null == mFooterContainer) {
            mFooterContainer = new LinearLayout(footerView.getContext());
            mFooterContainer.setOrientation(LinearLayout.VERTICAL);
            mFooterContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
        putViewInGroup(mFooterContainer, footerView, index);
        if (init) {
            if (emptyShow || errorShow) {
                if (headerFooterFront) {
                    notifyItemInserted(getHeaderSpace() + 1);
                }
            } else {
                notifyItemInserted(getHeaderSpace() + mList.size());
            }
        }
    }

    /**
     * 移除足视图
     *
     * @param footerView
     */
    public void removeFooterView(@NonNull View footerView) {
        if (0 == getFooterSpace()) {
            return;
        }
        removeFooterView(mFooterContainer.indexOfChild(footerView));
    }

    /**
     * 移除足视图
     *
     * @param index
     */
    public void removeFooterView(int index) {
        if (0 > index || 0 == getFooterSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptySpace();
        boolean errorShow = 1 == getErrorSpace();
        mFooterContainer.removeViewAt(index);
        if (0 == mFooterContainer.getChildCount()) {
            if (emptyShow || errorShow) {
                if (headerFooterFront) {
                    notifyItemRemoved(getHeaderSpace() + 1);
                }
            } else {
                notifyItemRemoved(getHeaderSpace() + mList.size());
            }
        }
    }

    /**
     * 移除所有足视图
     */
    public void removeFooterView() {
        if (0 == getFooterSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptySpace();
        boolean errorShow = 1 == getErrorSpace();
        mFooterContainer.removeAllViews();
        if (emptyShow || errorShow) {
            if (headerFooterFront) {
                notifyItemRemoved(0);
            }
        } else {
            notifyItemRemoved(getHeaderSpace() + mList.size());
        }
    }

    /**
     * 获取足视图数量
     *
     * @return
     */
    public int getFooterViewSize() {
        if (1 == getFooterSpace()) {
            return mFooterContainer.getChildCount();
        }
        return 0;
    }

    /**
     * 加载结束
     */
    public void loadMoreDismiss() {
        if (1 == getLoadMoreSpace()) {
            mILoadMore.onDismiss();
            mLoadMoreContainer.setVisibility(View.GONE);
            mLoadStatus = Status.LOAD_IDLE;
        }
    }

    /**
     * 加载失败
     */
    public void loadMoreFailure() {
        if (1 == getLoadMoreSpace()) {
            mILoadMore.onFailure();
            mLoadStatus = Status.LOAD_ERROR;
        }
    }

    /**
     * 加载到底了
     */
    public void loadMoreEnd() {
        if (1 == getLoadMoreSpace()) {
            mILoadMore.onLoadEnd();
            mLoadStatus = Status.LOAD_END;
        }
    }

    /**
     * 重新加载
     */
    public void loadMoreWhileFailure() {
        if (1 == getLoadMoreSpace() && Status.LOAD_ERROR == mLoadStatus) {
            mLoadStatus = Status.LOAD_ING;
            mILoadMore.onShow();
            if (null != mOnLoadListener) {
                mOnLoadListener.onLoad();
            }
        }
    }

    /**
     * 设置上拉加载视图
     *
     * @param loadMoreView
     */
    public void setLoadMoreView(@Nullable View loadMoreView) {
        boolean loadShow = 1 == getLoadMoreSpace() && 0 == Math.max(getEmptySpace(), getErrorSpace());
        if (null == loadMoreView) {
            //移除原有视图
            if (null != mLoadMoreContainer && 0 != mLoadMoreContainer.getChildCount()) {
                mLoadMoreContainer.removeAllViews();
                if (loadShow) {
                    notifyItemRemoved(getItemCount() - 1);
                }
            }
        } else {
            if (!(loadMoreView instanceof ILoadMore)) {
                throw new IllegalStateException("LoadMoreView must implements ILoadMore !");
            }
            mILoadMore = (ILoadMore) loadMoreView;
            if (null == mLoadMoreContainer) {
                mLoadMoreContainer = new FrameLayout(loadMoreView.getContext());
                mLoadMoreContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mLoadMoreContainer.removeAllViews();
            }
            putViewInGroup(mLoadMoreContainer, loadMoreView);
            loadMoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMoreWhileFailure();
                }
            });
            if (0 == Math.max(getEmptySpace(), getErrorSpace())) {
                notifyItemInserted(getItemCount() - 1);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mLayoutManager = recyclerView.getLayoutManager();
        if (null == mLayoutManager) {
            return;
        }
        if (mLayoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) mLayoutManager;
            final GridLayoutManager.SpanSizeLookup lookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch (getItemViewType(position)) {
                        case TYPE_EMPTY:
                        case TYPE_ERROR:
                        case TYPE_HEADER:
                        case TYPE_FOOTER:
                        case TYPE_LOAD:
                            //独占一行
                            return gridLayoutManager.getSpanCount();
                        default:
                            if (null != lookup) {
                                return lookup.getSpanSize(position - getHeaderSpace());
                            }
                            return 1;
                    }
                }
            });
        }
        recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(onScrollListener);
    }

    @Override
    public void onViewAttachedToWindow(FasterHolder holder) {
        //支持瀑布流
        LayoutParams params = holder.itemView.getLayoutParams();
        if (null != params && params instanceof StaggeredGridLayoutManager.LayoutParams) {
            switch (holder.getItemViewType()) {
                case TYPE_EMPTY:
                case TYPE_ERROR:
                case TYPE_HEADER:
                case TYPE_FOOTER:
                case TYPE_LOAD:
                    StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) params;
                    //独占一行、列
                    p.setFullSpan(true);
                default:
                    break;
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(FasterHolder holder) {
        holder.onDetach();
    }

    @Override
    public void onViewRecycled(FasterHolder holder) {
        holder.onRecycle();
    }

    @Override
    public FasterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_EMPTY:
                return createHolder(this, mEmptyContainer);
            case TYPE_ERROR:
                return createHolder(this, mErrorContainer);
            case TYPE_HEADER:
                return createHolder(this, mHeaderContainer);
            case TYPE_FOOTER:
                return createHolder(this, mFooterContainer);
            case TYPE_LOAD:
                return createHolder(this, mLoadMoreContainer);
            case TYPE_VOID_DATA:
                if (mListCountTransformer == null) {
                    throw new NullPointerException("position >  mList.size() - 1!");
                }
                final Strategy<Void> voidStrategy = mListCountTransformer.newVoidDataStrategy();
                if (voidStrategy == null) {
                    throw new NullPointerException("ListCountTransformer newVoidDataStrategy return null !");
                }
                final FasterHolder voidStrategyHolder = voidStrategy.createHolder(parent);
                //依附Adapter
                voidStrategyHolder.attach(this);
                //设置监听
                if (null != mOnItemClickListener) {
                    voidStrategyHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnItemClickListener.onItemClick(FasterAdapter.this, v, voidStrategyHolder.getAdapterPosition() - getHeaderSpace());
                        }
                    });
                }
                if (null != mOnItemLongClickListener) {
                    voidStrategyHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            mOnItemLongClickListener.onItemLongClick(FasterAdapter.this, v, voidStrategyHolder.getAdapterPosition() - getHeaderSpace());
                            return false;
                        }
                    });
                }
                voidStrategyHolder.onCreate();
                return voidStrategyHolder;
            default:
                for (final Entry<T> entry : mList) {
                    if (null != entry.getStrategy()) {
                        //在getItemViewType方法中必须注入Strategy
                        if (entry.getStrategy().getItemViewType() == viewType) {
                            //创建FasterHolder
                            final FasterHolder holder = entry.getStrategy().createHolder(parent);
                            //依附Adapter
                            holder.attach(this);
                            //设置监听
                            if (null != mOnItemClickListener) {
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mOnItemClickListener.onItemClick(FasterAdapter.this, v, holder.getAdapterPosition() - getHeaderSpace());
                                    }
                                });
                            }
                            if (null != mOnItemLongClickListener) {
                                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        mOnItemLongClickListener.onItemLongClick(FasterAdapter.this, v, holder.getAdapterPosition() - getHeaderSpace());
                                        return false;
                                    }
                                });
                            }
                            holder.onCreate();
                            return holder;
                        }
                    } else {
                        throw new NullPointerException("Entry has not strategy !");
                    }
                }
                throw new NullPointerException("no FasterHolder found !");
        }
    }

    /**
     * 创建Holder
     *
     * @param adapter
     * @param view
     * @return
     */
    private static FasterHolder createHolder(FasterAdapter adapter, View view) {
        FasterHolder holder = new FasterHolder(view);
        holder.attach(adapter);
        holder.onCreate();
        return holder;
    }

    @Override
    public void onBindViewHolder(FasterHolder holder, int position, List<Object> payloads) {
        switch (holder.getItemViewType()) {
            case TYPE_EMPTY:
                break;
            case TYPE_ERROR:
                break;
            case TYPE_HEADER:
                break;
            case TYPE_FOOTER:
                break;
            case TYPE_LOAD:
                break;
            case TYPE_VOID_DATA:
                //onCreateViewHolder已经校验过了
                final Strategy<Void> voidStrategy = mListCountTransformer.newVoidDataStrategy();
                voidStrategy.onBindViewHolder(holder, null, payloads);
                break;
            default:
                final int listPosition = position - getHeaderSpace();
                Strategy<T> strategy = mList.get(listPosition).getStrategy();
                if (null == strategy) {
                    throw new NullPointerException("Entry has not strategy !");
                } else {
                    strategy.onBindViewHolder(holder, mList.get(listPosition).getData(), payloads);
                }
        }
    }

    @Override
    public void onBindViewHolder(FasterHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        boolean hasEmpty = 1 == getEmptySpace();
        boolean hasError = 1 == getErrorSpace();
        boolean hasHeader = 1 == getHeaderSpace();
        if (headerFooterFront) {
            if (hasError) {
                switch (position) {
                    case 0:
                        return hasHeader ? TYPE_HEADER : TYPE_ERROR;
                    case 1:
                        return hasHeader ? TYPE_ERROR : TYPE_FOOTER;
                    case 2:
                        return TYPE_FOOTER;
                    default:
                        //理论不出现
                        return TYPE_ERROR;
                }
            }
            if (hasEmpty) {
                switch (position) {
                    case 0:
                        return hasHeader ? TYPE_HEADER : TYPE_EMPTY;
                    case 1:
                        return hasHeader ? TYPE_EMPTY : TYPE_FOOTER;
                    case 2:
                        return TYPE_FOOTER;
                    default:
                        //理论不出现
                        return TYPE_EMPTY;
                }
            }
            return getRealItemViewType(position);
        } else {
            if (hasError) {
                //错误视图
                return TYPE_ERROR;
            }
            if (hasEmpty) {
                //空视图
                return TYPE_EMPTY;
            }
            return getRealItemViewType(position);
        }
    }

    /**
     * 排除headerFooterFront后的获取Type逻辑
     *
     * @param position
     * @return
     */
    private int getRealItemViewType(int position) {
        if (0 == position && 1 == getHeaderSpace()) {
            //头视图
            return TYPE_HEADER;
        }
        //足视图位置
        int footerPosition = 1 == getFooterSpace() ? getHeaderSpace() + getValidListCount() : -1;
        if (-1 != footerPosition) {
            //存在足视图
            if (position < footerPosition) {
                //数据视图的type类型
                return getItemViewTypeFromList(position);
            } else if (position == footerPosition) {
                //足视图
                return TYPE_FOOTER;
            } else {
                //上拉加载视图
                return TYPE_LOAD;
            }
        } else {
            //不存在足视图
            if (1 == getLoadMoreSpace() && position == getItemCount() - 1) {
                //加载视图
                return TYPE_LOAD;
            } else {
                //数据视图的type类型
                return getItemViewTypeFromList(position);
            }
        }
    }

    /**
     * 从数据集合中获取类型
     *
     * @param position
     * @return
     */
    private int getItemViewTypeFromList(int position) {
        int listPosition = position - getHeaderSpace();
        if (listPosition > mList.size() - 1) {
            //超过限制，即mListCountTransformer#newListDataCount返回了更大的数值
            return TYPE_VOID_DATA;
        }
        Entry<T> entry = mList.get(listPosition);
        //方法会先于onCreateViewHolder调用
        if (entry.getStrategy() == null && mBindMap != null) {
            Pair<Strategy, MultiType> multiTypePair = mBindMap.get(entry.getData().getClass());
            if (multiTypePair == null) {
                throw new NullPointerException("FasterAdapter Builder no bind !");
            } else {
                if (multiTypePair.first != null) {
                    entry.setStrategy((Strategy<T>) multiTypePair.first);
                } else if (multiTypePair.second != null) {
                    Strategy strategy = multiTypePair.second.bind(entry.getData());
                    if (strategy == null) {
                        throw new NullPointerException("FasterAdapter Builder bind MultiType no found !");
                    }
                    entry.setStrategy(strategy);
                }
            }
        }
        return entry.getStrategy().getItemViewType();

    }

    @Override
    public int getItemCount() {
        if (headerFooterFront) {
            //头、足、占位视图可以同时显示
            if (1 == Math.max(getErrorSpace(), getEmptySpace())) {
                //占位视图(最多显示一个)+头+足
                return getHeaderSpace() + 1 + getFooterSpace();
            }
            //头+体+足+加载视图
            return getHeaderSpace() + getValidListCount() + getFooterSpace() + getLoadMoreSpace();
        } else {
            if (1 == Math.max(getErrorSpace(), getEmptySpace())) {
                //占位视图
                return 1;
            }
            //头+体+足+加载视图
            return getHeaderSpace() + getValidListCount() + getFooterSpace() + getLoadMoreSpace();
        }
    }

    private int getValidListCount() {
        int count = mList.size();
        if (mListCountTransformer != null) {
            return mListCountTransformer.newListDataCount(count);
        } else {
            return count;
        }
    }

    /**
     * 获取存放额外类型的Array
     *
     * @return
     */
    public LongSparseArray<Object> getObjectArray() {
        return array;
    }


    /**
     * 获取空视图的占用position
     *
     * @return
     */
    public int getEmptySpace() {
        if (ViewUtils.isEmpty(mEmptyContainer) || (!isEmptyEnabled) || isDisplayError) {
            return 0;
        }
        return EmptyUtils.isEmpty(mList) ? 1 : 0;
    }

    /**
     * 获取错误视图的占用position
     *
     * @return
     */
    public int getErrorSpace() {
        if (ViewUtils.isEmpty(mErrorContainer) || (!isDisplayError)) {
            return 0;
        }
        return 1;
    }

    /**
     * 获取头View的占用position
     *
     * @return
     */
    public int getHeaderSpace() {
        return ViewUtils.isEmpty(mHeaderContainer) ? 0 : 1;
    }

    /**
     * 获取足View的占用position
     *
     * @return
     */
    public int getFooterSpace() {
        return ViewUtils.isEmpty(mFooterContainer) ? 0 : 1;
    }

    /**
     * 获取上拉加载View的占用position
     *
     * @return
     */
    public int getLoadMoreSpace() {
        return ViewUtils.isEmpty(mLoadMoreContainer) || (!isLoadMoreEnabled) ? 0 : 1;
    }

    /**
     * 设置数据源
     *
     * @param list
     * @param strategy
     */
    public void setData(@Nullable List<T> list, @Nullable Strategy<T> strategy) {
        if (null == list) {
            clear(false);
        } else {
            mList = convertList(list, strategy);
            notifyDataSetChanged();
        }
    }

    /**
     * 设置数据源
     *
     * @param list
     */
    public void setData(@Nullable List<Entry<T>> list) {
        if (null == list) {
            clear(false);
        } else {
            mList = list;
            notifyDataSetChanged();
        }
    }

    /**
     * 设置数据源
     *
     * @param list
     * @param strategy
     */
    public void setDataByDiff(@Nullable final List<T> list, @Nullable Strategy<T> strategy) {
        if (null == list) {
            clear(false);
        } else {
            setDataByDiff(convertList(list, strategy));
        }
    }

    /**
     * 设置数据源
     *
     * @param list
     */
    public void setDataByDiff(@Nullable final List<Entry<T>> list) {
        if (null == list) {
            clear(false);
        } else {
            if (1 == getErrorSpace()) {
                mList = list;
                return;
            }
            if (1 == getEmptySpace()) {
                //避免占位视图ItemAnimator
                mList = list;
                notifyDataSetChanged();
                return;
            }
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return list.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    //equals比较
                    return mList.get(oldItemPosition).getData().equals(list.get(newItemPosition).getData());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return false;
                }

            }, true);
            mList = list;
            result.dispatchUpdatesTo(new ListUpdateCallback() {
                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position + getHeaderSpace(), count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position + getHeaderSpace(), count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition + getHeaderSpace(), toPosition);
                }

                @Override
                public void onChanged(int position, int count, Object payload) {
                    notifyItemRangeChanged(position + getHeaderSpace(), count, payload);
                }
            });
        }
    }

    /**
     * 添加数据
     *
     * @param data
     */
    public void add(@Nullable T data) {
        if (data == null) {
            return;
        }
        add(mList.size(), Entry.create(data), false);
    }

    /**
     * 添加数据
     *
     * @param data
     * @param immediately
     */
    public void add(@Nullable T data, boolean immediately) {
        if (data == null) {
            return;
        }
        add(mList.size(), Entry.create(data), immediately);
    }

    /**
     * 添加数据
     *
     * @param index
     * @param data
     */
    public void add(int index, @Nullable T data) {
        if (data == null) {
            return;
        }
        add(index, Entry.create(data), false);
    }

    /**
     * 添加数据
     *
     * @param index
     * @param data
     * @param immediately
     */
    public void add(int index, @Nullable T data, boolean immediately) {
        if (data == null) {
            return;
        }
        add(index, Entry.create(data), immediately);
    }

    /**
     * 添加数据
     *
     * @param entry
     */
    public void add(@Nullable Entry<T> entry) {
        add(mList.size(), entry, false);
    }

    /**
     * 添加数据
     *
     * @param entry
     * @param immediately
     */
    public void add(@Nullable Entry<T> entry, boolean immediately) {
        add(mList.size(), entry, immediately);
    }

    /**
     * 添加数据
     *
     * @param index
     * @param entry
     */
    public void add(int index, @Nullable Entry<T> entry) {
        add(index, entry, false);
    }

    /**
     * 添加数据
     *
     * @param index
     * @param entry
     * @param immediately
     */
    public void add(int index, @Nullable Entry<T> entry, boolean immediately) {
        if (entry == null || entry.getData() == null) {
            return;
        }
        int emptySpace = getEmptySpace();
        mList.add(index, entry);

        if (immediately || 1 == emptySpace || 1 == getErrorSpace()) {
            //避免占位视图ItemAnimator
            notifyDataSetChanged();
        } else {
            notifyItemInserted(getHeaderSpace() + index);
        }
    }

    private static <T> List<Entry<T>> convertList(@Nullable List<T> list, @Nullable Strategy<T> strategy) {
        if (list == null) {
            return null;
        }
        List<Entry<T>> result = new LinkedList<>();
        for (T data : list) {
            result.add(Entry.create(data, strategy));
        }
        return result;
    }

    /**
     * 添加数据集
     *
     * @param list
     * @param strategy
     */
    public void addAll(@Nullable List<T> list, @Nullable Strategy<T> strategy) {
        addAll(mList.size(), convertList(list, strategy), false);
    }

    /**
     * 添加数据集
     *
     * @param list
     * @param strategy
     * @param immediately
     */
    public void addAll(@Nullable List<T> list, @Nullable Strategy<T> strategy, boolean immediately) {
        addAll(mList.size(), convertList(list, strategy), immediately);
    }

    /**
     * 添加数据集
     *
     * @param index
     * @param list
     * @param strategy
     * @param immediately
     */
    public void addAll(int index, @Nullable List<T> list, @Nullable Strategy<T> strategy, boolean immediately) {
        addAll(index, convertList(list, strategy), immediately);
    }

    /**
     * 添加数据集
     *
     * @param list
     */
    public void addAll(@Nullable List<? extends Entry<T>> list) {
        addAll(mList.size(), list, false);
    }

    /**
     * 添加数据集
     *
     * @param list
     * @param immediately
     */
    public void addAll(@Nullable List<? extends Entry<T>> list, boolean immediately) {
        addAll(mList.size(), list, immediately);
    }

    /**
     * 添加数据集
     *
     * @param index
     * @param list
     */
    public void addAll(int index, @Nullable List<? extends Entry<T>> list) {
        addAll(index, list, false);
    }

    /**
     * 添加数据集
     *
     * @param index
     * @param list
     * @param immediately
     */
    public void addAll(int index, @Nullable List<? extends Entry<T>> list, boolean immediately) {
        if (EmptyUtils.isEmpty(list)) {
            return;
        }
        int emptySpace = getEmptySpace();
        mList.addAll(index, list);

        if (immediately || 1 == emptySpace || 1 == getErrorSpace()) {
            //避免占位视图ItemAnimator
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(getHeaderSpace() + index, list.size());
        }
    }

    /**
     * 移除
     *
     * @param position
     */
    public Entry<T> remove(int position) {
        return remove(position, false);
    }

    /**
     * 移除
     *
     * @param position
     * @param immediately
     * @return
     */
    public Entry<T> remove(int position, boolean immediately) {
        Entry<T> t = mList.remove(position);

        if (immediately || 1 == getEmptySpace() || 1 == getErrorSpace()) {
            //避免占位视图ItemAnimator
            notifyDataSetChanged();
        } else {
            notifyItemRemoved(getHeaderSpace() + position);
        }
        return t;
    }

    /**
     * equals 移除
     *
     * @param t
     * @return
     */
    public boolean remove(@Nullable T t) {
        return remove(t, false);
    }

    /**
     * equals 移除
     *
     * @param t
     * @param immediately
     * @return
     */
    public boolean remove(@Nullable T t, boolean immediately) {
        if (t == null) {
            return true;
        }
        boolean remove = false;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getData().equals(t)) {
                remove(i, immediately);
                remove = true;
            }
        }

        return remove;
    }

    /**
     * 条件移除
     *
     * @param predicate
     * @return
     */
    public boolean removeIf(@NonNull CollectionUtils.Predicate<T> predicate) {
        return removeIf(predicate, false);
    }

    /**
     * 条件移除
     *
     * @param predicate
     * @param immediately
     * @return
     */
    public boolean removeIf(@NonNull CollectionUtils.Predicate<T> predicate, boolean immediately) {
        boolean removed = false;
        final ListIterator<Entry<T>> listIterator = mList.listIterator();
        int nextIndex;
        while (listIterator.hasNext()) {
            nextIndex = listIterator.nextIndex();
            if (predicate.process(listIterator.next().getData())) {
                listIterator.remove();
                if (!immediately && Math.max(getEmptySpace(), getErrorSpace()) == 0) {
                    notifyItemRemoved(nextIndex + getHeaderSpace());
                }
                removed = true;
            }
        }

        if (immediately || 1 == getEmptySpace() || 1 == getErrorSpace()) {
            //避免占位视图ItemAnimator
            notifyDataSetChanged();
        }
        return removed;
    }

    /**
     * 清理数据集合
     */
    public void clear() {
        clear(false);
    }

    /**
     * 清理数据集合
     *
     * @param immediately
     */
    public void clear(boolean immediately) {
        if (immediately) {
            mList.clear();
            notifyDataSetChanged();
        } else {
            int size = mList.size();
            mList.clear();
            if (1 == getEmptySpace() || 1 == getErrorSpace()) {
                //避免占位视图ItemAnimator
                notifyDataSetChanged();
            } else {
                notifyItemRangeRemoved(getHeaderSpace(), size);
            }
        }
    }

    /**
     * 获取数据封装集合
     *
     * @return
     */
    public List<Entry<T>> getEntryList() {
        return mList;
    }

    /**
     * 获取浅拷贝的数据实体集合
     *
     * @return
     */
    public List<T> getSnapList() {
        List<T> result = new ArrayList<>();
        for (Entry<T> entry : mList) {
            result.add(entry.getData());
        }
        return result;
    }

    /**
     * 获取数据实体集合的浅拷贝
     *
     * @return
     */
    public T getListItem(int listPosition) {
        return mList.get(listPosition).getData();
    }

    /**
     * 获取数据实体的数量
     *
     * @return
     */
    public int getListSize() {
        return mList.size();
    }

    /**
     * 条目点击监听器
     */
    public interface OnItemClickListener {
        /**
         * 单击
         *
         * @param adapter
         * @param view
         * @param listPosition
         */
        void onItemClick(FasterAdapter adapter, View view, int listPosition);
    }

    /**
     * 条目长按点击监听器
     */
    public interface OnItemLongClickListener {
        /**
         * 长按
         *
         * @param adapter
         * @param view
         * @param listPosition
         */
        void onItemLongClick(FasterAdapter adapter, View view, int listPosition);
    }

    /**
     * 上拉加载监听器
     */
    public interface OnLoadListener {
        /**
         * 触发上拉加载
         */
        void onLoad();
    }

    public static final class Builder<D> {
        private OnItemClickListener itemClickListener = null;
        private OnItemLongClickListener itemLongClickListener = null;
        private OnLoadListener loadListener = null;

        private View emptyView = null;
        private View errorView = null;
        private View loadMoreView = null;
        private List<View> headerViews = null;
        private List<View> footerViews = null;
        private boolean emptyMatchParent = true;
        private boolean errorMatchParent = true;
        private boolean emptyEnabled = true;
        private boolean headerFooterFront = false;
        private boolean loadMoreEnabled = false;
        private List<Entry<D>> list = new ArrayList<>();
        private ArrayMap<Class, Pair<Strategy, MultiType>> bindMap = null;
        private ListCountTransformer listCountTransformer = null;

        private Builder() {
        }

        /**
         * 设置数据实体的ItemView的点击监听，其他场景下建议通过对Strategy设置监听回调
         *
         * @param listener
         * @return
         */
        public Builder<D> itemClickListener(@Nullable OnItemClickListener listener) {
            this.itemClickListener = listener;
            return this;
        }

        /**
         * 设置数据实体的ItemView的长按点击监听，其他场景下建议通过对Strategy设置监听回调
         *
         * @param listener
         * @return
         */
        public Builder<D> itemClickListener(@Nullable OnItemLongClickListener listener) {
            this.itemLongClickListener = listener;
            return this;
        }

        /**
         * 设置上拉加载的监听，需设置LoadMoreView才有效
         *
         * @param listener
         * @return
         */
        public Builder<D> loadListener(@Nullable OnLoadListener listener) {
            this.loadListener = listener;
            return this;
        }

        /**
         * 空视图（占位图）
         *
         * @param emptyView
         * @return
         */
        public Builder<D> emptyView(@Nullable View emptyView) {
            this.emptyView = emptyView;
            return this;
        }

        /**
         * 错误视图（占位图）
         *
         * @param errorView
         * @return
         */
        public Builder<D> errorView(@Nullable View errorView) {
            this.errorView = errorView;
            return this;
        }

        /**
         * 设置上拉加载视图，不设置isLoadMoreEnabled为true也不生效
         *
         * @param loadMoreView
         * @return
         */
        public Builder<D> loadMoreView(@Nullable View loadMoreView) {
            if (!(loadMoreView instanceof ILoadMore)) {
                throw new IllegalStateException("loadMoreView must implements ILoadMore !");
            }
            this.loadMoreView = loadMoreView;
            return this;
        }

        /**
         * 添加头View
         *
         * @param headerView
         * @return
         */
        public Builder<D> addHeaderView(@NonNull View headerView) {
            if (null == headerViews) {
                headerViews = new ArrayList<>();
            }
            headerViews.add(headerView);
            return this;
        }

        /**
         * 添加足View
         *
         * @param footView
         * @return
         */
        public Builder<D> addFooterView(@NonNull View footView) {
            if (null == footerViews) {
                footerViews = new ArrayList<>();
            }
            footerViews.add(footView);
            return this;
        }

        /**
         * 空视图的包裹容器高度是否是{@link ViewGroup.LayoutParams#MATCH_PARENT},充满RecyclerView
         * <br>
         * 一般不用对其设置，只有在有头、足视图时需要注意
         *
         * @param emptyMatchParent default true
         * @return
         */
        public Builder<D> emptyMatchParent(boolean emptyMatchParent) {
            this.emptyMatchParent = emptyMatchParent;
            return this;
        }

        /**
         * 错误视图的包裹容器高度是否是{@link ViewGroup.LayoutParams#MATCH_PARENT},充满RecyclerView
         * <br>
         * 一般不用对其设置，只有在有头、足视图时需要注意
         *
         * @param errorMatchParent default true
         * @return
         */
        public Builder<D> errorMatchParent(boolean errorMatchParent) {
            this.errorMatchParent = errorMatchParent;
            return this;
        }

        /**
         * 在初始化数据之前可以先设置false来不显示空视图
         *
         * @param emptyEnabled default true
         * @return
         */
        public Builder<D> emptyEnabled(boolean emptyEnabled) {
            this.emptyEnabled = emptyEnabled;
            return this;
        }

        /**
         * 头、足视图优先级是否大于占位视图，即在无数据或者错误视图时是否显示头、足视图
         *
         * @param headerFooterFront default false
         * @return
         */
        public Builder<D> headerFooterFront(boolean headerFooterFront) {
            this.headerFooterFront = headerFooterFront;
            return this;
        }

        /**
         * 是否启用上拉加载，需设置LoadMoreView才生效
         *
         * @param loadMoreEnabled default false
         * @return
         */
        public Builder<D> loadMoreEnabled(boolean loadMoreEnabled) {
            this.loadMoreEnabled = loadMoreEnabled;
            return this;
        }

        /**
         * 默认数据
         *
         * @param es
         * @return
         */
        public Builder<D> data(@Nullable Entry<D>... es) {
            if (null == es) {
                return this;
            }
            this.list = new ArrayList<>(Arrays.asList(es));
            return this;
        }

        /**
         * 默认数据
         *
         * @param list
         * @return
         */
        public Builder<D> data(@Nullable List<Entry<D>> list) {
            if (null == list) {
                return this;
            }
            this.list = list;
            return this;
        }


        /**
         * 一对多的数据类型、视图类型绑定
         *
         * @param cls
         * @param multiType
         * @return
         */
        public <T> Builder<D> bind(@NonNull Class<T> cls, @NonNull MultiType<T> multiType) {
            if (null == bindMap) {
                bindMap = new ArrayMap<>();
            }
            bindMap.put(cls, new Pair<Strategy, MultiType>(null, multiType));
            return this;
        }

        /**
         * 多对多的数据类型、视图类型绑定
         *
         * @param cls
         * @param strategy
         * @return
         */
        public <T> Builder<D> bind(@NonNull Class<T> cls, @NonNull Strategy<T> strategy) {
            if (null == bindMap) {
                bindMap = new ArrayMap<>();
            }
            bindMap.put(cls, new Pair<Strategy, MultiType>(strategy, null));
            return this;
        }

        /**
         * 设置数据实体集合大小的变化器，注意与{@link FasterAdapter#addHeaderView(View)}、{@link FasterAdapter#addFooterView(View)} 、{@link FasterAdapter#setLoadMoreView(View)}共用会有Holder回收问题
         *
         * @param listCountTransformer
         * @return
         */
        public Builder<D> listCountTransformer(@Nullable ListCountTransformer listCountTransformer) {
            this.listCountTransformer = listCountTransformer;
            return this;
        }

        /**
         * 构建
         *
         * @return
         */
        public FasterAdapter<D> build() {
            return new FasterAdapter<D>(this);
        }
    }
}

