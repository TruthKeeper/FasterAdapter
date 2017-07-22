package com.tk.fasteradapter;

import android.content.res.Resources;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.tk.fasteradapter.FasterAdapter.Status.LOAD_END;
import static com.tk.fasteradapter.FasterAdapter.Status.LOAD_ERROR;
import static com.tk.fasteradapter.FasterAdapter.Status.LOAD_ING;
import static com.tk.fasteradapter.FasterAdapter.Status.LOAD_STANDBY;

/**
 * <pre>
 *      author : TK
 *      time : 2017/07/15
 *      desc : Builder模式高度封装的Adapter:
 *              支持空视图（占位视图）配置；
 *              支持错误视图（占位视图）配置；
 *              支持占位视图优先级以及其它配置；
 *              支持头部View配置；
 *              支持足部View配置；
 *              支持上拉加载以及监听、NestedScroll场景下的配置；
 *              上拉加载内置异常处理：点击LoadMoreView重试，回调onLoad方法；
 *              支持
 *              支持常见数据集合增、删、以及Diff操作；
 *              支持对数据集合的FasterHolder的单击事件监听；
 *              特殊业务场景下，封装SparseArray用于对FasterHodler的数据保存（通过FasterHodler中的Adapter引用来操作）；
 *
 *
 * </pre>
 */

public final class FasterAdapter<T> extends RecyclerView.Adapter<FasterHolder> {
    public static final int TYPE_EMPTY = -10000;
    public static final int TYPE_ERROR = -20000;
    public static final int TYPE_HEADER = -30000;
    public static final int TYPE_FOOTER = -40000;
    public static final int TYPE_LOAD = -50000;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOAD_STANDBY, LOAD_ERROR, LOAD_END, LOAD_ING})
    public @interface Status {
        /**
         * 待命
         */
        int LOAD_STANDBY = 0x00;
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
    private int mLoadStatus = LOAD_STANDBY;
    private OnItemClickListener mOnItemClickListener = null;
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
     * 空视图的包裹容器高度是否充满RecyclerView，一般不用对其设置，只有在有头、足视图时需要注意
     */
    private boolean isEmptyMatchParent;
    /**
     * 错误视图的包裹容器高度是否充满RecyclerView，一般不用对其设置，只有在有头、足视图时需要注意
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
    private boolean isLoadMoreEnabled;
    /**
     * 数据封装集合
     */
    private List<Entry<T>> mList = null;
    /**
     * 数据类型 - 视图策略的绑定关系
     */
    private Map<Class<?>, Strategy<?>> mBindMap;
    /**
     * 是否显示错误，true时优先级大于空视图的优先级，未设置errorView时无效
     */
    private boolean isDisplayError = false;
    /**
     * 存放对FasterHolder额外数据保存的Array
     */
    private SparseArray<Object> array = null;
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
                    && 1 == getLoadMoreViewSpace()
                    && 0 == Math.max(getEmptyViewSpace(), getErrorViewSpace())
                    && (mLoadStatus == LOAD_STANDBY)
                    && (lastVisibleItemPosition == getItemCount() - 1
                    && 0 != firstCompletelyVisibleItemPosition)) {
                //开始加载
                mLoadStatus = LOAD_ING;
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

    private FasterAdapter(Builder<T> builder) {
        mOnItemClickListener = builder.itemListener;
        mOnLoadListener = builder.loadListener;
        isEmptyMatchParent = builder.isEmptyMatchParent;
        isErrorMatchParent = builder.isErrorMatchParent;
        if (null != builder.emptyView) {
            mEmptyContainer = new FrameLayout(builder.emptyView.getContext());
            mEmptyContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, isEmptyMatchParent ? MATCH_PARENT : WRAP_CONTENT));
            mEmptyContainer.addView(builder.emptyView);
        }
        if (null != builder.errorView) {
            mErrorContainer = new FrameLayout(builder.errorView.getContext());
            mErrorContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, isErrorMatchParent ? MATCH_PARENT : WRAP_CONTENT));
            mErrorContainer.addView(builder.errorView);
        }
        if (null != builder.headerViews && (!builder.headerViews.isEmpty())) {
            mHeaderContainer = new LinearLayout(builder.headerViews.get(0).getContext());
            mHeaderContainer.setOrientation(LinearLayout.VERTICAL);
            mHeaderContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            for (View child : builder.headerViews) {
                mHeaderContainer.addView(child);
            }
        }
        if (null != builder.footerViews && (!builder.footerViews.isEmpty())) {
            mFooterContainer = new LinearLayout(builder.footerViews.get(0).getContext());
            mFooterContainer.setOrientation(LinearLayout.VERTICAL);
            mFooterContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            for (View child : builder.footerViews) {
                mFooterContainer.addView(child);
            }
        }
        if (null != builder.loadMoreView) {
            mLoadMoreContainer = new FrameLayout(builder.loadMoreView.getContext());
            mLoadMoreContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mLoadMoreContainer.addView(builder.loadMoreView);
            mLoadMoreContainer.setVisibility(View.GONE);
            builder.loadMoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMoreWhileFailure();
                }
            });
            mILoadMore = (ILoadMore) builder.loadMoreView;
        }

        isEmptyEnabled = builder.isEmptyEnabled;
        headerFooterFront = builder.headerFooterFront;
        isLoadMoreEnabled = builder.isLoadMoreEnabled;
        if (null == builder.list) {
            mList = new ArrayList<>();
        } else {
            mList = builder.list;
        }
        mBindMap = builder.bindMap;
        array = new SparseArray<>(2);
    }

    /**
     * 设置空视图（占位视图）
     *
     * @param emptyView
     */
    public void setEmptyView(@Nullable View emptyView) {
        boolean showEmpty = isEmptyEnabled && (!isDisplayError) && mList.isEmpty();
        if (null == emptyView) {
            //移除空视图
            if (null != mEmptyContainer && 0 != mEmptyContainer.getChildCount()) {
                mEmptyContainer.removeAllViews();
                if (showEmpty) {
                    notifyDataSetChanged();
                }
            }
        } else {
            if (null == mEmptyContainer) {
                mEmptyContainer = new FrameLayout(emptyView.getContext());
                mEmptyContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, isEmptyMatchParent ? MATCH_PARENT : WRAP_CONTENT));
                mEmptyContainer.addView(emptyView);
                if (showEmpty) {
                    notifyDataSetChanged();
                }
            } else {
                mEmptyContainer.removeAllViews();
                mEmptyContainer.addView(emptyView);
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
            isEmptyEnabled = emptyEnabled;
            if (1 == getEmptyViewSpace()) {
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
            if (null != mErrorContainer && 0 != mErrorContainer.getChildCount()) {
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
                mErrorContainer.addView(errorView);
                if (isDisplayError) {
                    notifyDataSetChanged();
                }
            } else {
                mEmptyContainer.removeAllViews();
                mEmptyContainer.addView(errorView);
            }
        }
    }

    /**
     * 是否显示错误视图(占位视图)，注意：立即生效
     *
     * @param displayError
     */
    public void setDisplayError(boolean displayError) {
        if (this.isDisplayError != displayError) {
            if (null == mErrorContainer || 0 == mErrorContainer.getChildCount()) {
                throw new IllegalStateException("no error view set !");
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
            if ((1 == getHeaderViewSpace() || 1 == getFooterViewSpace())
                    && (1 == getEmptyViewSpace() || 1 == getErrorViewSpace())) {
                //立即刷新
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
        addHeaderView(0 == getHeaderViewSpace() ? 0 : mHeaderContainer.getChildCount() - 1, headerView);
    }

    /**
     * 添加头视图
     *
     * @param index
     * @param headerView
     */
    public void addHeaderView(int index, @NonNull View headerView) {
        boolean init = 0 == getHeaderViewSpace();
        boolean emptyShow = 1 == getEmptyViewSpace();
        boolean errorShow = 1 == getErrorViewSpace();
        if (null == mHeaderContainer) {
            mHeaderContainer = new LinearLayout(headerView.getContext());
            mHeaderContainer.setOrientation(LinearLayout.VERTICAL);
            mHeaderContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
        mHeaderContainer.addView(headerView, index);
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
        if (0 == getHeaderViewSpace()) {
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
        if (0 > index || 0 == getHeaderViewSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptyViewSpace();
        boolean errorShow = 1 == getErrorViewSpace();
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
        if (0 == getHeaderViewSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptyViewSpace();
        boolean errorShow = 1 == getErrorViewSpace();
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
        if (1 == getHeaderViewSpace()) {
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
        addFooterView(0 == getFooterViewSpace() ? 0 : mFooterContainer.getChildCount() - 1, footerView);
    }

    /**
     * 添加足视图
     *
     * @param index
     * @param footerView
     */
    public void addFooterView(int index, @NonNull View footerView) {
        boolean init = 0 == getFooterViewSpace();
        boolean emptyShow = 1 == getEmptyViewSpace();
        boolean errorShow = 1 == getErrorViewSpace();
        if (null == mFooterContainer) {
            mFooterContainer = new LinearLayout(footerView.getContext());
            mFooterContainer.setOrientation(LinearLayout.VERTICAL);
            mFooterContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
        mFooterContainer.addView(footerView, index);
        if (init) {
            if (emptyShow || errorShow) {
                if (headerFooterFront) {
                    notifyItemInserted(getHeaderViewSpace() + 1);
                }
            } else {
                notifyItemInserted(getHeaderViewSpace() + mList.size());
            }
        }
    }

    /**
     * 移除足视图
     *
     * @param footerView
     */
    public void removeFooterView(@NonNull View footerView) {
        if (0 == getFooterViewSpace()) {
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
        if (0 > index || 0 == getFooterViewSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptyViewSpace();
        boolean errorShow = 1 == getErrorViewSpace();
        mFooterContainer.removeViewAt(index);
        if (0 == mFooterContainer.getChildCount()) {
            if (emptyShow || errorShow) {
                if (headerFooterFront) {
                    notifyItemRemoved(0);
                }
            } else {
                notifyItemRemoved(getHeaderViewSpace() + mList.size());
            }
        }
    }

    /**
     * 移除所有足视图
     */
    public void removeFooterView() {
        if (0 == getFooterViewSpace()) {
            return;
        }
        boolean emptyShow = 1 == getEmptyViewSpace();
        boolean errorShow = 1 == getErrorViewSpace();
        mFooterContainer.removeAllViews();
        if (emptyShow || errorShow) {
            if (headerFooterFront) {
                notifyItemRemoved(0);
            }
        } else {
            notifyItemRemoved(getHeaderViewSpace() + mList.size());
        }
    }

    /**
     * 获取足视图数量
     *
     * @return
     */
    public int getFooterViewSize() {
        if (1 == getFooterViewSpace()) {
            return mFooterContainer.getChildCount();
        }
        return 0;
    }

    /**
     * 加载结束
     */
    public void loadMoreDismiss() {
        if (1 == getLoadMoreViewSpace()) {
            mILoadMore.onDismiss();
            mLoadMoreContainer.setVisibility(View.GONE);
            mLoadStatus = LOAD_STANDBY;
        }
    }

    /**
     * 加载失败
     */
    public void loadMoreFailure() {
        if (1 == getLoadMoreViewSpace()) {
            mILoadMore.onFailure();
            mLoadStatus = LOAD_ERROR;
        }
    }

    /**
     * 加载到底了
     */
    public void loadMoreEnd() {
        if (1 == getLoadMoreViewSpace()) {
            mILoadMore.onLoadEnd();
            mLoadStatus = LOAD_END;
        }
    }

    /**
     * 重新加载
     */
    public void loadMoreWhileFailure() {
        if (1 == getLoadMoreViewSpace() && LOAD_ERROR == mLoadStatus) {
            mLoadStatus = LOAD_ING;
            mILoadMore.onShow();
            if (null != mOnLoadListener) {
                mOnLoadListener.onLoad();
            }
        }
    }

    /**
     * 用于嵌套Nested场景
     *
     * @param scrollState NestedScrollView的滚动状态
     */
    public void applyInNestedScroll(int scrollState) {
        if (null == mLayoutManager) {
            return;
        }

        /*
         * 满足条件
         */
        if (SCROLL_STATE_IDLE == scrollState
                && 1 == getLoadMoreViewSpace()
                && 0 == Math.max(getEmptyViewSpace(), getErrorViewSpace())
                && (mLoadStatus == LOAD_STANDBY)) {
            //上拉加载条目在屏幕上可见
            int[] location = new int[2];
            mLoadMoreContainer.getLocationInWindow(location);
            if (location[1] <= Resources.getSystem().getDisplayMetrics().heightPixels) {
                //开始加载
                mLoadStatus = LOAD_ING;
                mLoadMoreContainer.setVisibility(VISIBLE);
                mILoadMore.onShow();
                if (null != mOnLoadListener) {
                    mOnLoadListener.onLoad();
                }
            }
        }
    }

    /**
     * 设置上拉加载视图
     *
     * @param loadMoreView
     */
    public void setLoadMoreView(@Nullable View loadMoreView) {
        boolean loadShow = 1 == getLoadMoreViewSpace() && 0 == Math.max(getEmptyViewSpace(), getErrorViewSpace());
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
                throw new IllegalStateException("loadMoreView must implements ILoadMore !");
            }
            mILoadMore = (ILoadMore) loadMoreView;
            if (null == mLoadMoreContainer) {
                mLoadMoreContainer = new FrameLayout(loadMoreView.getContext());
                mLoadMoreContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                mLoadMoreContainer.addView(loadMoreView);
                loadMoreView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMoreWhileFailure();
                    }
                });
                if (0 == Math.max(getEmptyViewSpace(), getErrorViewSpace())) {
                    notifyItemInserted(getItemCount() - 1);
                }
            } else {
                mEmptyContainer.removeAllViews();
                mEmptyContainer.addView(loadMoreView);
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
                                return lookup.getSpanSize(position - getHeaderViewSpace());
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
        recyclerView.clearOnScrollListeners();
        onScrollListener = null;
    }

    @Override
    public void onViewAttachedToWindow(FasterHolder holder) {
        //支持瀑布流
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
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
                return createHolder(mEmptyContainer);
            case TYPE_ERROR:
                return createHolder(mErrorContainer);
            case TYPE_HEADER:
                return createHolder(mHeaderContainer);
            case TYPE_FOOTER:
                return createHolder(mFooterContainer);
            case TYPE_LOAD:
                return createHolder(mLoadMoreContainer);
            default:
                for (final Entry<T> entry : mList) {
                    if (null != entry.getStrategy()) {
                        if (entry.getStrategy().getItemViewType() == viewType) {
                            final FasterHolder holder = entry.getStrategy().onCreateHolder(parent);
                            holder.setAdapter(this);
                            if (null != mOnItemClickListener) {
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mOnItemClickListener.onClick(FasterAdapter.this, v, holder.getAdapterPosition() - getHeaderViewSpace());
                                    }
                                });
                            }
                            return holder;
                        }
                    } else {
                        throw new IllegalStateException("must has strategy !");
                    }
                }
                throw new IllegalStateException("no holder found !");
        }
    }

    /**
     * 持有Adapter的引用
     *
     * @param view
     * @return
     */
    private FasterHolder createHolder(View view) {
        FasterHolder holder = new FasterHolder(view);
        holder.setAdapter(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(FasterHolder holder, int position) {
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
            default:
                final int listPosition = position - getHeaderViewSpace();
                Strategy<T> strategy = mList.get(listPosition).getStrategy();
                if (null == strategy) {
                    throw new IllegalStateException("must has strategy !");
                } else {
                    strategy.onBindViewHolder(holder, mList.get(listPosition).getData());
                }
        }
    }

    @Override
    public int getItemViewType(int position) {
        boolean hasEmpty = 1 == getEmptyViewSpace();
        boolean hasError = 1 == getErrorViewSpace();
        boolean hasHeader = 1 == getHeaderViewSpace();
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
        if (0 == position && 1 == getHeaderViewSpace()) {
            //头视图
            return TYPE_HEADER;
        }
        //足视图位置
        int footerPostion = 1 == getFooterViewSpace() ? getHeaderViewSpace() + mList.size() : -1;
        if (-1 != footerPostion) {
            //存在足视图
            if (position < footerPostion) {
                //数据视图的type类型
                return getItemViewTypeFromList(position);
            } else if (position == footerPostion) {
                //足视图
                return TYPE_FOOTER;
            } else {
                //上拉加载视图
                return TYPE_LOAD;
            }
        } else {
            //不存在足视图
            if (1 == getLoadMoreViewSpace() && position == getItemCount() - 1) {
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
        int listPosition = position - getHeaderViewSpace();
        Entry<T> entry = mList.get(listPosition);
        if (null == entry.getStrategy()) {
            throw new IllegalStateException("must has strategy !");
        } else {
            return entry.getStrategy().getItemViewType();
        }
    }

    @Override
    public int getItemCount() {
        if (headerFooterFront) {
            //头、足、占位视图可以同时显示
            if (1 == Math.max(getErrorViewSpace(), getEmptyViewSpace())) {
                //占位视图(最多显示一个)+头+足
                return getHeaderViewSpace() + 1 + getFooterViewSpace();
            }
            //头+体+足+加载视图
            return getHeaderViewSpace() + mList.size() + getFooterViewSpace() + getLoadMoreViewSpace();
        } else {
            if (1 == Math.max(getErrorViewSpace(), getEmptyViewSpace())) {
                //占位视图
                return 1;
            }
            //头+体+足+加载视图
            return getHeaderViewSpace() + mList.size() + getFooterViewSpace() + getLoadMoreViewSpace();
        }
    }

    /**
     * 获取存放额外类型的Array
     *
     * @return
     */
    public SparseArray<Object> getObjectArray() {
        return array;
    }


    /**
     * 获取空视图的占用position
     *
     * @return
     */
    public int getEmptyViewSpace() {
        if (null == mEmptyContainer || 0 == mEmptyContainer.getChildCount()
                || (!isEmptyEnabled) || isDisplayError) {
            return 0;
        }
        return mList.isEmpty() ? 1 : 0;
    }

    /**
     * 获取错误视图的占用position
     *
     * @return
     */
    public int getErrorViewSpace() {
        if (null == mErrorContainer || 0 == mErrorContainer.getChildCount() || (!isDisplayError)) {
            return 0;
        }
        return 1;
    }

    /**
     * 获取头View的占用position
     *
     * @return
     */
    public int getHeaderViewSpace() {
        return null == mHeaderContainer || 0 == mHeaderContainer.getChildCount() ? 0 : 1;
    }

    /**
     * 获取足View的占用position
     *
     * @return
     */
    public int getFooterViewSpace() {
        return null == mFooterContainer || 0 == mFooterContainer.getChildCount() ? 0 : 1;
    }

    /**
     * 获取上拉加载View的占用position
     *
     * @return
     */
    public int getLoadMoreViewSpace() {
        return null == mLoadMoreContainer || 0 == mLoadMoreContainer.getChildCount() || (!isLoadMoreEnabled) ? 0 : 1;
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
     */
    public void setDataByDiff(@Nullable final List<Entry<T>> list) {
        if (null == list) {
            clear(false);
        } else {
            if (1 == getErrorViewSpace()) {
                mList = list;
                return;
            }
            if (1 == getEmptyViewSpace()) {
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
                    notifyItemRangeInserted(position + getHeaderViewSpace(), count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position + getHeaderViewSpace(), count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition + getHeaderViewSpace(), toPosition);
                }

                @Override
                public void onChanged(int position, int count, Object payload) {
                    notifyItemRangeChanged(position + getHeaderViewSpace(), count, payload);
                }
            });
        }
    }

    /**
     * 添加数据
     *
     * @param entry
     */
    public void add(@NonNull Entry<T> entry) {
        add(mList.size(), entry, false);
    }

    /**
     * 添加数据
     *
     * @param entry
     * @param immediately
     */
    public void add(@NonNull Entry<T> entry, boolean immediately) {
        add(mList.size(), entry, immediately);
    }

    /**
     * 添加数据
     *
     * @param index
     * @param entry
     */
    public void add(int index, @NonNull Entry<T> entry) {
        add(index, entry, false);
    }

    /**
     * 添加数据
     *
     * @param index
     * @param entry
     * @param immediately
     */
    public void add(int index, @NonNull Entry<T> entry, boolean immediately) {
        int emptySpace = getEmptyViewSpace();
        mList.add(index, entry);

        if (immediately || 1 == emptySpace || 1 == getErrorViewSpace()) {
            //避免占位视图ItemAnimator
            notifyDataSetChanged();
        } else {
            notifyItemInserted(getHeaderViewSpace() + index);
        }
    }

    /**
     * 添加数据集
     *
     * @param collection
     */
    public void addAll(@NonNull Collection<? extends Entry<T>> collection) {
        addAll(mList.size(), collection, false);
    }

    /**
     * 添加数据集
     *
     * @param collection
     * @param immediately
     */
    public void addAll(@NonNull Collection<? extends Entry<T>> collection, boolean immediately) {
        addAll(mList.size(), collection, immediately);
    }

    /**
     * 添加数据集
     *
     * @param index
     * @param collection
     */
    public void addAll(int index, @NonNull Collection<? extends Entry<T>> collection) {
        addAll(index, collection, false);
    }

    /**
     * 添加数据集
     *
     * @param index
     * @param collection
     * @param immediately
     */
    public void addAll(int index, @NonNull Collection<? extends Entry<T>> collection, boolean immediately) {
        int emptySpace = getEmptyViewSpace();
        mList.addAll(index, collection);

        if (immediately || 1 == emptySpace || 1 == getErrorViewSpace()) {
            //避免占位视图ItemAnimator
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(getHeaderViewSpace() + index, collection.size());
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

        if (immediately || 1 == getEmptyViewSpace() || 1 == getErrorViewSpace()) {
            //避免占位视图ItemAnimator
            notifyDataSetChanged();
        } else {
            notifyItemRemoved(getHeaderViewSpace() + position);
        }
        return t;
    }

    /**
     * equals 移除
     *
     * @param t
     * @return
     */
    public boolean remove(@NonNull T t) {
        return remove(t, false);
    }

    /**
     * equals 移除
     *
     * @param t
     * @param immediately
     * @return
     */
    public boolean remove(@NonNull T t, boolean immediately) {
        Entry<T> entry;
        int index = -1;
        for (int i = 0; i < mList.size(); i++) {
            entry = mList.get(i);
            if (entry.getData().equals(t)) {
                index = i;
                break;
            }
        }
        if (-1 != index) {
            remove(index);
            return true;
        }
        return false;
    }

    /**
     * 条件移除
     *
     * @param predicate
     * @return
     */
    public boolean removeIf(@NonNull Predicate<T> predicate) {
        return removeIf(predicate, false);
    }

    /**
     * 条件移除
     *
     * @param predicate
     * @param immediately
     * @return
     */
    public boolean removeIf(@NonNull Predicate<T> predicate, boolean immediately) {
        boolean removed = false;
        final ListIterator<Entry<T>> listIterator = mList.listIterator();
        int nextIndex;
        while (listIterator.hasNext()) {
            nextIndex = listIterator.nextIndex();
            if (predicate.removeConfirm(listIterator.next().getData())) {
                listIterator.remove();
                if (!immediately && 0 == getErrorViewSpace()) {
                    notifyItemRemoved(nextIndex);
                }
                removed = true;
            }
        }

        if (immediately || 1 == getEmptyViewSpace() || 1 == getErrorViewSpace()) {
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

            if (1 == getEmptyViewSpace() || 1 == getErrorViewSpace()) {
                //避免占位视图ItemAnimator
                notifyDataSetChanged();
            } else {
                notifyItemRangeRemoved(getHeaderViewSpace(), size);
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
     * 获取数据实体集合的浅拷贝
     *
     * @return
     */
    public List<T> getListSnap() {
        List<T> result = new ArrayList<>();
        for (Entry<T> entry : mList) {
            result.add(entry.getData());
        }
        return result;
    }

    /**
     * 获取数据实体的数量
     *
     * @return
     */
    public int getListSnapSize() {
        return mList.size();
    }

    /**
     * 根据绑定关系来填充数据集合
     *
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Entry<T>> fillByBindStrategy(@Nullable List<T> list) {
        if (null == mBindMap || mBindMap.isEmpty()) {
            throw new IllegalStateException("FasterAdapter Builder no bind Class of Strategy !");
        }
        List<Entry<T>> result = new ArrayList<>();
        if (null != list) {
            Entry<T> entry;
            for (T t : list) {
                Strategy<T> strategy = (Strategy<T>) mBindMap.get(t.getClass());
                if (null == strategy) {
                    throw new IllegalStateException("no Class of Strategy bind found!");
                }
                entry = new Entry<>(t, strategy);
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * 根据绑定关系来填充数据实体
     *
     * @param t
     * @return
     */
    @SuppressWarnings("unchecked")
    public Entry<T> fillByBindStrategy(@NonNull T t) {
        if (null == mBindMap || mBindMap.isEmpty()) {
            throw new IllegalStateException("FasterAdapter Builder no bind Class of Strategy !");
        }
        Strategy<T> strategy = (Strategy<T>) mBindMap.get(t.getClass());
        if (null == strategy) {
            throw new IllegalStateException("no Class of Strategy bind found!");
        }
        return new Entry<>(t, strategy);
    }

    /**
     * 单类型策略的集合的方式填充
     *
     * @param list
     * @param strategy
     * @param <D>
     * @return
     */
    public static <D> List<Entry<D>> fillBySingleStrategy(@NonNull List<D> list, @NonNull Strategy<D> strategy) {
        List<Entry<D>> result = new ArrayList<>();
        Entry<D> entry;
        for (D d : list) {
            entry = new Entry<D>(d, strategy);
            result.add(entry);
        }
        return result;
    }

    /**
     * 单类型策略的集合的方式填充
     *
     * @param ds
     * @param strategy
     * @param <D>
     * @return
     */
    public static <D> List<Entry<D>> fillBySingleStrategy(@NonNull D[] ds, @NonNull Strategy<D> strategy) {
        List<Entry<D>> result = new ArrayList<>();
        Entry<D> entry;
        for (D d : ds) {
            entry = new Entry<D>(d, strategy);
            result.add(entry);
        }
        return result;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.mOnLoadListener = onLoadListener;
    }

    /**
     * 移除器，兼容实现
     *
     * @param <D>
     */
    public interface Predicate<D> {
        boolean removeConfirm(D data);
    }

    /**
     * 条目点击监听器
     */
    public interface OnItemClickListener {
        void onClick(FasterAdapter adapter, View view, int listPosition);
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
        private OnItemClickListener itemListener = null;
        private OnLoadListener loadListener = null;
        private View emptyView = null;
        private View errorView = null;
        private View loadMoreView = null;
        private List<View> headerViews = null;
        private List<View> footerViews = null;
        private boolean isEmptyMatchParent = true;
        private boolean isErrorMatchParent = true;
        private boolean isEmptyEnabled = true;
        private boolean headerFooterFront = false;
        private boolean isLoadMoreEnabled = true;
        private List<Entry<D>> list = new ArrayList<>();
        private Map<Class<?>, Strategy<?>> bindMap = null;

        public Builder() {
        }

        /**
         * 设置数据实体的ItemView的点击监听，其他场景下建议通过对Strategy设置监听回调
         *
         * @param listener
         * @return
         */
        public Builder<D> itemClickListener(@Nullable OnItemClickListener listener) {
            this.itemListener = listener;
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
         * 空视图的包裹容器高度是否充满RecyclerView，一般不用对其设置，只有在有头、足视图时需要注意
         *
         * @param isEmptyMatchParent
         * @return
         */
        public Builder<D> isEmptyMatchParent(boolean isEmptyMatchParent) {
            this.isEmptyMatchParent = isEmptyMatchParent;
            return this;
        }

        /**
         * 错误视图的包裹容器高度是否充满RecyclerView，一般不用对其设置，只有在有头、足视图时需要注意
         *
         * @param isErrorMatchParent
         * @return
         */
        public Builder<D> isErrorMatchParent(boolean isErrorMatchParent) {
            this.isErrorMatchParent = isErrorMatchParent;
            return this;
        }

        /**
         * 在初始化数据之前可以先设置false来不显示空视图，默认开启
         *
         * @param isEmptyEnabled
         * @return
         */
        public Builder<D> isEmptyEnabled(boolean isEmptyEnabled) {
            this.isEmptyEnabled = isEmptyEnabled;
            return this;
        }

        /**
         * 头、足视图优先级是否大于占位视图，默认false
         *
         * @param headerFooterFront
         * @return
         */
        public Builder<D> headerFooterFront(boolean headerFooterFront) {
            this.headerFooterFront = headerFooterFront;
            return this;
        }

        /**
         * 是否启用上拉加载，默认开启，需设置LoadMoreView才生效
         *
         * @param isLoadMoreEnabled
         * @return
         */
        public Builder<D> isLoadMoreEnabled(boolean isLoadMoreEnabled) {
            this.isLoadMoreEnabled = isLoadMoreEnabled;
            return this;
        }

        /**
         * 默认数据
         *
         * @param es
         * @return
         */
        public Builder<D> data(@NonNull Entry<D>... es) {
            this.list = new ArrayList<>(Arrays.asList(es));
            return this;
        }

        /**
         * 默认数据
         *
         * @param list
         * @return
         */
        public Builder<D> data(@NonNull List<Entry<D>> list) {
            this.list = list;
            return this;
        }

        /**
         * 单类型策略的集合的方式填充
         *
         * @param list
         * @param strategy
         * @return
         */
        public Builder<D> fillBySingleStrategy(@NonNull List<D> list, @NonNull Strategy<D> strategy) {
            this.list = FasterAdapter.fillBySingleStrategy(list, strategy);
            return this;
        }

        /**
         * 单类型策略的集合的方式填充
         *
         * @param ds
         * @param strategy
         * @return
         */
        public Builder<D> fillBySingleStrategy(@NonNull D[] ds, @NonNull Strategy<D> strategy) {
            this.list = FasterAdapter.fillBySingleStrategy(ds, strategy);
            return this;
        }

        /**
         * 通过数据类型 - 视图策略 来绑定
         *
         * @param cls
         * @param strategy
         * @param <T>
         * @return
         */
        public <T> Builder<D> bind(@NonNull Class<T> cls, @NonNull Strategy<T> strategy) {
            if (null == bindMap) {
                bindMap = new HashMap<>();
            }
            bindMap.put(cls, strategy);
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
