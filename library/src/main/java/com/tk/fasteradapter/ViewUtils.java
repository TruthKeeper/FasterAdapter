package com.tk.fasteradapter;

import android.support.annotation.Nullable;
import android.view.ViewGroup;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 *      author : TK
 *      time : 2017/11/1
 *      desc : View工具类
 * </pre>
 */

public final class ViewUtils {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private ViewUtils() {
        throw new IllegalStateException();
    }

    /**
     * 父容器是否为空
     *
     * @param viewGroup
     * @return
     */
    public static boolean isEmpty(@Nullable ViewGroup viewGroup) {
        return viewGroup == null || viewGroup.getChildCount() == 0;
    }
}