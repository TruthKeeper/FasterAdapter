package com.tk.fasteradapter;

/**
 * <pre>
 *     author : TK
 *     time   : 2017/9/13
 *     desc   : 集合处理工具类
 * </pre>
 */
public final class CollectionUtils {
    public interface Predicate<D> {
        /**
         * @param d
         * @return 是否执行
         */
        boolean process(D d);
    }

    private CollectionUtils() {
        throw new IllegalStateException();
    }


}
