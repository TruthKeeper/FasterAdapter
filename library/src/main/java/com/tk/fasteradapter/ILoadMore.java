package com.tk.fasteradapter;

/**
 * <pre>
 *     author : TK
 *     time   : 2017/07/16
 *     desc   : 加载视图
 * </pre>
 */
public interface ILoadMore {
    /**
     * 显示加载视图，一般用于开始动画等操作
     */
    void onShow();

    /**
     * 加载完毕并且将要隐藏，用于动画资源等的回收
     */
    void onDismiss();

    /**
     * 到底了，用于上拉加载无数据
     */
    void onLoadEnd();

    /**
     * 上拉加载失败，用于网络异常等场景
     */
    void onFailure();
}
