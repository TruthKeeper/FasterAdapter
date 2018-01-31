package com.tk.fasteradapter;

import android.support.annotation.Nullable;

import java.util.Collection;

/**
 * <pre>
 *      author : TK
 *      time : 2017/11/6
 *      desc : 判空工具类
 * </pre>
 */

public final class EmptyUtils {
    private EmptyUtils() {
        throw new IllegalStateException();
    }

    /**
     * @param charSequence
     * @return
     */
    public static boolean isEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * @param array
     * @return
     */
    public static boolean isEmpty(@Nullable Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * @param collection
     * @return
     */
    public static boolean isEmpty(@Nullable Collection collection) {
        return collection == null || collection.isEmpty();
    }

}
