package com.unicom.betterworry.service;

import com.unicom.betterworry.being.Worry;
import com.unicom.betterworry.being.WorryList;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工单列表服务接口
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 */
public interface WorryArrService {
    /**
     * 根据ticket来获取WorryList
     * @param ticket
     * @param start 工单列表的开始位置
     * @param end 结束位置 如果{@code start==end&&end==0}的话，就是全部数据
     * @return
     */
    WorryList getWorryArr(String ticket, int start, int end);

    /**
     * 建造 {@code WorryList} 实体类
     * @param readyWorrylist
     * @param limitProid
     * @param searchtype
     */
    void bulidWorryArr(WorryList readyWorrylist, List<Integer> limitProid, int searchtype);

    /**
     *
     * @param woArr {@code Worry.processId}的{@code List}数据
     * @param type 分析统计类型，参数可查看s_category where type=gt
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> analysisArrwithType(List<Integer> woArr, String type) throws Exception;

    /**
     * 对已经猜想完的worryList进行分析统计
     * @param ticket
     * @param type 分析统计类型，参数可查看s_category where type=gt
     * @return
     * @throws Exception
     */
    List<Map<String,Object>> guessedWorryanalysis(String ticket, String type) throws Exception;

    /**
     * 工单筛选
     * @param args 筛选条件
     * @param worrylist 筛选的范围，如果为{@code null}则没有限制
     * @param viewType 查看的样式，@see WorryList
     * @return
     * @throws ParseException
     * @throws SQLException
     */
    List<Worry> worryFilter(List<Map<String,Object>> args, List<Integer> worrylist, String viewType) throws ParseException, SQLException;

    /**
     * 工单特殊筛选，根据统计分组的条件进行筛选
     * @param args 
     * @param worrylist
     * @return
     */
    List<Worry> worrySpecialFilter(List<Map<String,Object>> args, List<Integer> worrylist);

    /**
     * 将{@code List<Worry>}输出为二进制流的形式
     * @param worrylist 
     * @return
     */
    byte[] worryarroutfile(List<Integer> worrylist);

    /**
     * 查看工单是否存在
     * @param ticket
     * @return
     */
    boolean checkWorryArr(String ticket);

    /**
     * 设置工单列表的访问
     * @param ticket
     * @param fixed
     */
    void setWorryArrAccess(String ticket,boolean fixed);

    /**
     * 获取工单猜想状态
     * @param ticket
     * @return @see WorryList
     */
    Integer getWorryitemGuessState(String ticket);

    /**
     * 获取工单猜想的子列表
     * @param ticket
     * @return
     */
    Set<String> getWorryGuessedSonTicket(String ticket);

    /**
     * 获取猜想的父列表
     * @param ticket
     * @return
     */
    Set<String> getWorryGuessedFatherTicket(String ticket);

    void recoverGuessedState(String ticket);
}
