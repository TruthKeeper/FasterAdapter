package com.tk.fasteradapter;

import android.view.View;

/**
 * <pre>
 *     author : TK
 *     time   : 2018/01/31
 *     desc   : List集合数据大小变化器 , 注意与{@link FasterAdapter#addHeaderView(View)}、{@link FasterAdapter#addFooterView(View)} 、{@link FasterAdapter#setLoadMoreView(View)}共用会有Holder回收问题
 * </pre>
 */
public interface ListCountTransformer {
    /**
     * @param listDataCount {@link FasterAdapter#mList}真实数据集合大小
     * @return 新定义数据集合大小
     */
    int newListDataCount(int listDataCount);

    /**
     * {@link ListCountTransformer#newListDataCount(int)}返回的值大于数据集合真实长度时，返回视图加载策略
     *
     * @return {@link Strategy#getItemViewType()}被定义成{@link FasterAdapter#TYPE_VOID_DATA}
     */
    Strategy<Void> newVoidDataStrategy();
}
