package com.unicom.betterworry.dao;

import com.unicom.betterworry.being.Word;
import com.unicom.betterworry.being.WorryItem;
import com.unicom.betterworry.being.WorryList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * worry mongo数据库接口
 *@author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 */
public interface WorryMongo {


    /**
     * 查找满足条件的Worrylist
     * @param ticket {@code Worrylist.ticket}
     * @param index {@code Worrylist.index}
     * @return 返回满足条件的{@code Worrylist}
     */
    WorryList findWorryList(String ticket,int index);

    /**
     * Debris是分段的意思,查找满足条件的{@code Worrylist}
     * @param ticket Worrylist的ticket
     * @return 返回满足条件的 {@code List<Worrylist>}
     */
    List<WorryList> findWorryListDebris(String ticket);

    /**
     * 查找满足条件的全部{@code Worrylist}并整合成一个{@code WorryList}类
     * @param ticket  {@code Worrylist.ticket}
     * @return 返回满足条件的Worrylist
     */
    WorryList findWorryList(String ticket);

    /**
     * 根据提供的工单{@code Worry}，找出在一个worrylist中有相同词的工单
     * @param processId {@code Worry.processId}
     * @param ticket  {@code Worrylist.ticket}
     * @return {@code WorryItem}类
     */
    List<WorryItem> findWorryAllWord(int processId, String ticket);
    /**
     * 根据提供的工单的一个 {@code Worry.processId}，找出在一个{@code List<Integer> pid}中有相同词的工单
     * @param processId {@code Worry.processId}
     * @param pid  {@code Worry.processId}的集合
     * @return {@code WorryItem}类
     */
    List<WorryItem> findWorryAllWord(int processId, List<Integer> pid);

    /**
     * 封装的相等查找方法
     * @param args 键值对应相等的判断条件
     * @param collection 集合名称
     * @return 满足条件的集合
     */
    List findequal(Map<String, Object> args, String collection);
    /**
     * 封装的相等查找方法
     * @param args 键值对应相等的判断条件
     * @param collection 集合名称
     * @param fields 返回时投影的字段，为空则不限制
     * @return 满足条件的集合
     */
    List findequal(Map<String, Object> args, String collection, List<String> fields);
    /**
     * 封装的相等查找方法
     * @param args 键值对应相等的判断条件
     * @param collection 集合名称
     * @param fields 返回时投影的字段，为空则不限制
     * @param entityClass 返回的类型
     * @return 满足条件的集合
     */
    <T> List<T> findequal(Map<String, Object> args, Class<T> entityClass, String collection, List<String> fields);
    /**
     * 封装的相等查找方法
     * @param args 键值对应相等的判断条件
     * @param collection 集合名称
     * @param fields 返回时投影的字段，为空则不限制
     * @param limit 限制文档数量
     * @param entityClass 返回的类型
     * @return 满足条件的集合
     */
    <T> List<T> findequal(Map<String, Object> args, Class<T> entityClass, String collection, List<String> fields, int limit);

    /**
     * 查找worrylist的Guessed的值
     * @param ticket Worrylist.ticket
     * @return Guessed的值
     */
    Integer getWorryListGuessState(String ticket);

    /**
     *
     * @param ticket Worrylist.ticket
     * @param guessed guessed的值，值的含义在Worrylist类中有展示
     * @param index Worrylist.index
     */
    void setWorryListGuessState(String ticket,int guessed,int index);

    /**
     * 查找WorryItem
     * @param processId worry.processId
     * @return 满足条件的WorryItem 集合
     */
    List<WorryItem> findWorryItem(Integer processId);

    /**
     * 在方法findWorryAllWord 的基础上找出WorryItem 中category有值的工单
     * @param processId
     * @param pids
     * @return
     */
    List<WorryItem> findTwinsUserful(int processId, List<Integer> pids);

    /**
     * 保存worrylist
     * @param worrylist worrylist
     */
    void saveWorryList(WorryList worrylist);
    @Deprecated
    void saveMap(final Map in, String collection);
    @Deprecated
    void saveData(final List in, String collection);

    /**
     *
     * @param enity 保存类的实体
     * @param collection 集合名称
     * @param <T>
     */
    <T> void saveData(T enity,String collection);

    /**
     * 数据更新方法，仅仅一条
     * @param in in为index，判断条件
     * @param updata 更新的数据
     * @param collection 集合名称
     */
    void updateMap(final Map<String, Object> in, final Map<String, Object> updata, String collection);

    /**
     * 数据更新方法，更新所有满足条件的数据
     * @param in in为index，判断条件
     * @param updata 更新的数据
     * @param collection 集合名称
     */
    void updateMapAll(Map<String, Object> in, Map<String, Object> updata, String collection);

    /**
     * 数据更新方法，没有则插入一条新的
     * @param in in为index，判断条件，不存在则插入
     * @param updata 更新的数据
     * @param collection 集合名称
     */
    void upsertMap(Map<String, Object> in, Map<String, Object> updata, String collection);

    void effectWorrySP(int proId, List<Map.Entry<String, Integer>> contentsp, List<Map.Entry<String, Integer>> streamsp, List<String> Department);

    /**
     * 检查worrylist是否存在
     * @param key 键名
     * @param val 键名对应的值
     * @return 存在为true ，否则为false
     */
    boolean checkWorryList(String key, Object val);

    /**
     * 在集合collection中查询不包含param这个字段中value值的集合
     * @param param 查询的字段值
     * @param values 查询的值的集合
     * @param collection 集合名称
     * @param includeArs 结果返回时投影的字段
     * @return 满足条件的集合
     */
    List<Map> findNotIn(String param, List values, String collection, List<String> includeArs);

    /**
     * worryItem 更新方法
     * @param processId worryItem.processId
     * @param category worryItem.category
     * @param supercategory worryItem.supercategory
     * @param words worryItem.contentSPwords
     */
    void updateWorryItem(int processId, Integer category, Integer supercategory, List<Word> words);

    /**
     * 求和
     * @param param 字段名称
     * @param collection 集合名称
     * @return 求和的值
     */
    Integer sum(String param, String collection);

    /**
     * in 判断查询
     * @param param 字段名称
     * @param values 字段值的集合
     * @param clz 返回的类型
     * @param collection
     * @param includeArs
     * @param <T>
     * @return
     */
    <T> List<T> findIn(String param, List values, Class<T> clz, String collection, List<String> includeArs);

    /**
     * 修改worrylist的guessed
     * @param ticket worrylist.ticket
     * @param guessed 修改的值
     */
    void setWorryListGuessState(String ticket,int guessed);

    /**
     *找出所有正在识别的子列表
     * @param ticket worrylist.ticket
     * @return 子列表的ticket集合
     */
    Set<String> getWorryGuessedSonTicket(String ticket);
    /**
     * 找出worrylist的子worrylist
     * @param ticket worrylist.ticket
     * @return 子列表的ticket集合
     */
    Set<String> findWorryListSonTicket(String ticket);

    /**
     * 找出worrylist的父worrylist
     * @param ticket worrylist.ticket
     * @return 父列表的ticket集合
     */
    Set<String> findWorryListFatherTicket(String ticket);

    /**
     * 失效一个worrylist
     * @param ticket worrylist.ticket
     */
    void ineffectWorryList(String ticket);

    /**
     * 失效一个worrylist
     * @param ticket worrylist.ticket
     * @param index worrylist.index
     */
    void ineffectWorryList(String ticket,int index);
}
