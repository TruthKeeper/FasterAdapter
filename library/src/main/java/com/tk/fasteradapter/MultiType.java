package com.tk.fasteradapter;

/**
 * <pre>
 *     author : TK
 *     time   : 2018/01/30
 *     desc   : xxxx描述
 * </pre>
 */
public interface MultiType<Data> {
    Strategy<Data> bind(Data data);
}
