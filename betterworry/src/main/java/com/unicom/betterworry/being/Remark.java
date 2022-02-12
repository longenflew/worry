package com.unicom.betterworry.being;

import java.util.Date;

/**
 * 工单评论实体类
 **/
public class Remark {
    int remarkId; //评论唯一标识
    Integer processId; //工单processId
    String processKey;//工单processKey
    Integer remarkType; //评论类型，见s_category
    String remark; //评论内容
    String username; //评论账号
    Date lastTime; //评论的时间

    Remark() {

    }

    Remark(int remarkId, Integer processid) {
        this.remarkId = remarkId;
        processId = processid;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRemarkId() {
        return remarkId; }

    public void setRemarkId(int remarkId) {
        this.remarkId = remarkId;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public String getProcessKey() {
        return processKey;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public Integer getRemarkType() {
        return remarkType;
    }

    public void setRemarkType(Integer remarkType) {
        this.remarkType = remarkType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
