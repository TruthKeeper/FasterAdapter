package com.tk.fasteradapter;

/**
 * <pre>
 *      author : TK
 *      time : 2017/07/15
 *      desc : FasterAdapter中存放的数据包装
 * </pre>
 */

public class Entry<T> {
    private T data = null;
    private Strategy<T> strategy = null;

    public static <T> Entry<T> create(T data) {
        return new Entry<T>(data);
    }

    public static <T> Entry<T> create(T data, Strategy<T> strategy) {
        return new Entry<T>(data, strategy);
    }

    private Entry(T data) {
        this.data = data;
    }

    private Entry(T data, Strategy<T> strategy) {
        this.data = data;
        this.strategy = strategy;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Strategy<T> getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy<T> strategy) {
        this.strategy = strategy;
    }
}
