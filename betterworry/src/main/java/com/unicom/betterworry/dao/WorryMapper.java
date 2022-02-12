package com.unicom.betterworry.dao;

import com.unicom.betterworry.being.Remark;
import com.unicom.betterworry.being.Worry;
import com.unicom.betterworry.being.WorryItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * worry 关系型数据库接口
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 */
public interface WorryMapper {
    /**
     *
     * @param Ids 工单processId的List
     * @return {@code List<Worry>} 返回提供的processId的详细工单
     */
    List<Worry> findWorryList(List<Integer> Ids);

    /**
     *
     * @param sql 可以执行的sql语句
     * @param val sql语句中的变量
     * @return 满足条件的 {@code List<Worry>}
     * @throws SQLException
     */
    List<Worry> findWorryBySql(String sql, Object val[]) throws SQLException;

    /**
     *
     * @param sql 可以执行的sql语句
     * @param val sql语句中的变量
     * @return 满足条件的 {@code List<Stream>}
     * @throws SQLException
     */
    List<Worry> findStreamBySql(String sql, Object val[]) throws SQLException;

    /**
     *
     * @param sql 可以执行的sql语句
     * @param val sql语句中的变量
     * @return 满足条件的{@code List<Map>} 满足条件的值
     * @throws SQLException
     */
    List<Map<String, Object>> find(String sql, Object[] val) throws SQLException;

    /**
     *
     * @param sql 可以执行的sql语句
     * @param val sql语句中的变量
     * @throws SQLException
     */
    void excuteSql(String sql, List<Object[]> val) throws SQLException;

    /**
     *
     * @param Id Worry类中的processId
     * @return 找到数据库是processId的Worry
     */
    Worry findbyId(int Id);

    /**
     *
     * @param k  Worry类中的processKey
     * @return 找到数据库中processKey的Worry
     */
    Worry findbyKey(String k);

    /**
     *
     * @param sql 可以执行的sql语句
     * @param val sql语句中的变量
     * @throws SQLException
     */
    void excuteSqlone(String sql, List<Object> val) throws SQLException;

    /**
     *
     * @param type category中类型值，可查看数据库表s_category
     * @return 满足条件的Map 格式为{  cat:1
     *                             catname:需求类问题
     *                             keytag:t
     *                             }
     *
     *
     */
    List<Map<String, Object>> getCategoryinfo(List<String> type);

    /**
     *
     * @param type category中类型值，可以批量查询
     * @return 满足条件的{@code List<Map>}
     */
    List<Map<String,Object>> getCategoryinfo(String type);
    /**
     * 工单属性修改方法
     * @param worrys 入参为WorryItem的List
     * @throws SQLException
     */
    void setworryItem(List<WorryItem> worrys) throws SQLException;

    /**
     * 新增工单属性修改的日志
     * @param worrys 入参为WorryItem的List
     * @param ip 用户Ip
     * @param person 用户账号
     * @throws SQLException
     */
    void insertItemLog(List<WorryItem> worrys, String ip, String person) throws SQLException;

    /**
     * 工单评论查找
     * @param processId Worry的processId
     * @param type 为评论类型，可查看数据库表s_category
     * @return 返回满足processId的评论
     * @throws SQLException
     */
    List<Remark> findRemarks(int processId, Integer type) throws SQLException;

    /**
     * 工单评论方法
     * @param processId Worry的processId
     * @param remark 评论内容
     * @param username 用户账号
     * @param remarktype  为评论类型，可查看数据库表s_category
     * @throws SQLException
     */
    void commentWorry(int processId, String remark, String username, int remarktype) throws SQLException;

    /**
     * 工单评论赞同方法，类似与点赞
     * @param remarksId remark的唯一标识
     * @throws SQLException
     */
    void approveRemark(int remarksId) throws SQLException;

}
