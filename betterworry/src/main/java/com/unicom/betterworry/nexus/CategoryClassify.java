package com.unicom.betterworry.nexus;

import com.unicom.betterworry.being.Category;

import java.util.List;

public interface CategoryClassify {
    /**
     * 批量分析方法
     * @param contents
     * @return
     */
    List<Category> classifyBatch(List<String> contents);

    /**
     * 根据提供的内容进行分类
     * @param content
     * @return
     */
    Category classify(String content);

    /**
     * 更新数据样本
     * @throws Exception
     */
    void updateWordSample() throws Exception;
}
