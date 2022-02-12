package com.unicom.betterworry.service;

import com.unicom.betterworry.being.Remark;
import com.unicom.betterworry.being.Worry;
import com.unicom.betterworry.being.WorryItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @author 
 */
public interface WorryService {
    /**
     * 工单查询，根据工单唯一标识查询工单的详细信息
     * @param id {@code Worry.processId}
     * @return
     */
    Worry worryFind(int id);

    /**
     * 工单查询，根据processKey查询工单的详细信息
     * @param key  {@code Worry.processId}
     * @return
     */
    Worry worryFind(String key);

    /**
     * 找出工单中在集合luck的处理人
     * @param id {@code Worry.processId} 
     * @param luck 账号集合
     * @return map的键值对应是在第几条流水有luck的人
     */
    Map<Integer, String> getWorrySolvePerson(int id, Set<String> luck);
    /**
     * 找出工单中在集合luck的处理人
     * @param id {@code worry.processId}
     * @param orgid 组织名称
     * @return map的键值对应是在第几条流水有luck的人
     */
    Map<Integer, String> getWorrySolvePerson(int id, String orgid);

    /**
     * 堆提供的内容进行工单关键词切词
     * @param content 文本内容
     * @return
     */
    List<Map> findspWordinContent(String content);

    List<Map<String, Object>> findchangedPerson(int id);

    /**
     * 找出相似工单
     * @param id {@code Worry.processId}
     * @param ticket worrylist.ticket
     * @return
     */
    List<Map> findSimialrWorry(int id, String ticket);

    /**
     * 设置工单的属性
     * @param worry {@code worryTtem}的列表
     * @param ip 操作的ip
     * @param personName 操作的用户名
     * @throws Exception
     */
    void setworryItem(List<WorryItem> worry, String ip, String personName) throws Exception;
/**
 * 让工单内容的关键词成为其工单的特性
 * @param processId {@code Worry.processId} 
 * @param spwords 
 * @throws Exception
 */
    void worryContentSPwordEffect(int processId, List<Map.Entry<String, Integer>> spwords) throws Exception;
/**
 * 猜想提供的工单集合的类别
 * @param proIds
 * @return 包含这些工单的分类结果
 * @throws SQLException
 */
    List<WorryItem> guessWorryCategory(List<Integer> proIds) throws SQLException;

    /**
     * 根据通过的ticket对worrylist中的工单类别猜想
     * @param ticket Worrylist.ticket
     * @throws Exception
     */
    void guessWorryCategoryBayesian(String ticket) throws Exception;

    /**
     * 保存工单的特性
     * @param pids 工单唯一标识的集合
     * @throws SQLException
     */
    void saveWorryCharacteristic(List<Integer> pids) throws SQLException;

    /**
     * 保存已猜想完的工单属性
     * @param ticket {@code WorryList.code}
     * @throws Exception
     */
    void saveGuessedWorryCategory(String ticket) throws Exception;

    /**
     * 获取工单评论
     * @param processId
     * @param type
     * @return
     */
    List<Remark> getWorryRemark(int processId,Integer type);

    /**
     * 工单评论
     * @param processId 
     * @param remark 
     * @param username
     * @param remarktype
     * @throws SQLException
     */
    void commentWorry(int processId, String remark,String username, int remarktype) throws SQLException;

    /**
     * 评论点赞
     * @param remarksId
     * @throws SQLException
     */
    void approveRemark(int remarksId) throws SQLException;
}
