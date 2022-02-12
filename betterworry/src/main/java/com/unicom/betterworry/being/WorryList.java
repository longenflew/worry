package com.unicom.betterworry.being;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 工单列表实体类
 **/
public class WorryList {
    /**是否可以访问 */
    boolean access; 
    /**分析状态 0为未猜想状态，1为正在猜想，2为全部猜想完成；
     * 10为部分猜想完成，11为存在子列表正在猜想；
     * 21为存在父列表正在猜想
     *  */
    int guessed;
    /**过滤条件 */
    List<Map<String,Object>> filter; 
    
    Date insertTime; //插入时间
    String ticket;//worrylist 唯一标识
    int size=0; //worrylist包含的worry的数量
    String ip; //记录哪个ip生成的
    int index=0; //worrylist是分段保存的，index表示该实体的在全部中的位置
    boolean fixed; //是否被固定，如果被固定了，ticket则不会随时间变化
    String sessionId; //生成worrylist的sessionId
    List<Worry> value=new ArrayList(0); //worrylist中的worry实体类集合
    String fatherTicket; //父worrylist的ticket
    String viewType="worry"; //查看形式，worry是以工单内容的形式，stream是以流水的形式

    public String getFatherTicket() {
        return fatherTicket;
    }

    public void setFatherTicket(String fatherTicket) {
        this.fatherTicket = fatherTicket;
    }

    public boolean isAccess() {
        return access;
    }

    public void setAccess(boolean access) {
        this.access = access;
    }

    public int getGuessed() {
        return guessed;
    }

    public void setGuessed(int guessed) {
        this.guessed = guessed;
    }

    public List<Map<String, Object>> getFilter() {
        return filter;
    }

    public void setFilter(List<Map<String, Object>> filter) {
        this.filter = filter;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Integer getSize() {
        return value==null?null: size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<Worry> getValue() {
        return value;
    }

    public void setValue(List<Worry> value) {
        this.value = value;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }
}
