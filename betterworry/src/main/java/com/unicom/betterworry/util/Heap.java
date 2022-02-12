package com.unicom.betterworry.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 最大/小堆数据结构
 * @param <K>
 * @param <T>
 */
public interface Heap<K,T> extends Collection<K> {
    /**
     * 插入一个元素
     * @param key 用了排序的数据
     * @param data 元素存储的数据
     * @return 返回被抛弃的元素
     */
    Map insert(K key, T data);

    /**
     * 删除一个元素
     * @param index
     * @return
     */
    Map<T,K> delete(int index);

    /**
     * 生成并返回最大或最小的元素
     * @return
     */
    Map<T,K> deleteMinOrMax();

    /**
     * 找到最顶端的元素
     * @return
     */
    Map<T,K> findHighest();
    boolean isEmpty();

    /**
     * 获取二叉树的数组
     * @return
     */
    List getList();
}
interface Entry<K,V>{
    K getKey();
    V getValue();
}
