package com.unicom.betterworry.dao;

import com.unicom.betterworry.being.Category;
import com.unicom.betterworry.being.Word;
import com.unicom.betterworry.being.WorryItem;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 * 分类mongo数据库接口
 */
public interface CategoryClassfiyMongo {
    /**
     * @return 所有在集合中的{@code Word}集合
     */
    List<Word> findPossessionsWord();

    /**
     * @return 找出所有不重复的 {@code Category.supcategory}类别的集合
     */
    List<Integer> findDistinctSupcate();

    /**
     * 更新{@code Word}的概率 {@code Word.percent}
     *
     * @param words      {@code Word}的集合
     * @param collection 集合名称
     */
    void updateWordClassProbability(List<Word> words, String collection);

    /**
     * 更新{@code Category}的概率 {@code Category.categoryProbability||Category.supercategoryProbability}
     *
     * @param category   {@code Category}实例
     * @param collection 集合名称
     */
    void updateWorryClassProbability(Category category, String collection);

    /**
     * 计算在时间time之前新增的 {@code WorryItem} 数量
     *
     * @param time 截至时间
     * @return 新增的 {@code WorryItem} 数量
     */
    long countNewWorryItem(Timestamp time);

    /**
     * @param time 截至时间
     * @return 在新增的worryItem集合
     */
    List<WorryItem> findNewWorryItem(Timestamp time);

    /**
     * @param param      字段名称，在集合必须为数字类型
     * @param collection 集合名称
     * @return 求出某个字段的和
     */
    Integer sum(String param, String collection);

    /** 通用相等查询，封装的相等查询，尽量在此基础上开发
     * @see WorryMongo#findequal(Map, String)
     * @param args
     * @param collection
     * @return
     */
    List findequal(Map<String, Object> args, String collection);

    /** 通用相等查询
     * @see WorryMongo#findequal(Map, String, List)
     * @param args 
     * @param collection
     * @param fields
     * @return
     */
    List findequal(Map<String, Object> args, String collection, List<String> fields);

    /**
     * 通用相等查询
     * @see WorryMongo#findequal(Map, Class, String, List)
     * @param args
     * @param entityClass
     * @param collection
     * @param fields
     * @param <T>
     * @return
     */
    <T> List<T> findequal(Map<String, Object> args, Class<T> entityClass, String collection, List<String> fields);

    /** 通用数据保存
     * @see WorryMongo#saveData(Object, String)
     * @param in         数据集合
     * @param collection 集合名称
     */
    void saveData(final List in, String collection);

    /**
     * @param processId 工单唯一标识
     * @return woryItem实体类的集合
     */
    List<WorryItem> findWorryItem(Integer processId);

    /**
     * 清空数据库中的word集合
     */
    void clearWordCollection();

    /**
     * 数据更新
     * @see WorryMongo#updateMap(Map, Map, String)
     * @param in         查询条件
     * @param updata     更新的数据
     * @param collection 集合名称
     */
    void updateMap(final Map<String, Object> in, final Map<String, Object> updata, String collection);

    <T> List<T> findIn(String param, Collection values, Class<T> clz, String collection, List<String> includeArs);

    /**
     * 查找具有word特性的分类
     *
     * @param words {@code Word}实体类集合
     * @param type  {@code type.equal("super")} 为高级类别，{@code type.equal("base")}为基础类别
     * @return 满足条件的category类实体
     */
    List<Category> findCategoryinWord(Collection words, String type); //type=super 为高级类别，type=base 为基础类别
}
