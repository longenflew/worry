package com.unicom.betterworry.being;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 工单流水实体类
 **/
public class Stream { // 工单流水
    String personName;//处理人账号
    String personNick;//处理人姓名
    String luckyName; //转到的处理人账号
    String luckyNick=""; //转到的处理人姓名或组织
    int action; //处理人的处理方式，见s_category
    String actionName; //处理方式的名称
    int processId;//工单的processId
    String processKey;//工单的processKey
    String[] department; //处理部门
    String[] todepartment; //转到处理的部门
    Date startdate; //处理开始时间
    Date handledate; //处理完的时间
    String answer; //回复内容
    int streamId;//流水唯一标识

    public Stream(int streamid, String personName, String personNick, String luckyName, String luckyNick, int action, String answer, String[] department, String[] todepartment, Date sdate, Date hdate) {
        this.streamId = streamid;
        this.personName = personName;
        this.personNick = personNick;
        this.action = action;
        this.department = department;
        this.todepartment = todepartment;
        this.startdate = sdate;
        this.handledate = hdate;
        this.answer = answer;
        this.luckyName = luckyName;
        this.luckyNick = luckyNick;
    }

    public Stream(int streamid, String personName, String personNick, String luckyName, String luckyNick, int action, String answer, String[] department, Date sdate, Date hdate) {
        this.streamId = streamid;
        this.personName = personName;
        this.personNick = personNick;
        this.action = action;
        this.department = department;
        this.startdate = sdate;
        this.handledate = hdate;
        this.answer = answer;
        this.luckyName = luckyName;
        this.luckyNick = luckyNick;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public void setPersonNick(String personNick) {
        this.personNick = personNick;
    }

    public void setLuckyName(String luckyName) {
        this.luckyName = luckyName;
    }

    public void setLuckyNick(String luckyNick) {
        this.luckyNick = luckyNick;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public void setDepartment(String[] department) {
        this.department = department;
    }

    public String[] getTodepartment() {
        return todepartment;
    }

    public void setTodepartment(String[] todepartment) {
        this.todepartment = todepartment;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public void setHandledate(Date handledate) {
        this.handledate = handledate;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public Stream() {
    }

    public Stream(int processid) {
        this.processId = processid;
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer text = new StringBuffer();
        text.append("[" + personNick + "] 从");
        text.append(sdf.format(startdate) + "至");
        text.append(sdf.format(handledate) + " ");
        text.append(actionName + " ");
        if (!department[0].equals(""))
            text.append(department[0] + ">");
        if (!department[1].equals(""))
            text.append(department[1] + ">");
        if (!department[2].equals(""))
            text.append(department[2] + "> 下的");
        text.append(luckyNick);
        text.append(" ");
        if (!answer.equals(" "))
            text.append(answer);
        return text.toString();
    }

    public int getStreamId() {
        return streamId;
    }

    public String getProcessKey() {
        return processKey;
    }

    public int getProcessId() {
        return processId;
    }

    public String getPersonName() {
        return personName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getPersonNick() {
        return personNick;
    }

    public String getLuckyNick() {
        return luckyNick;
    }

    public String getLuckyName() {
        return luckyName;
    }

    public int getAction() {
        return action;
    }

    public String[] getDepartment() {
        return department;

    }

    public Date getStartdate() {
        return startdate;

    }

    public Date getHandledate() {
        return handledate;
    }

    public String getAnswer() {
        return answer;
    }

    public String getDepartments(){
        StringBuffer text = new StringBuffer();
        if (!getDepartment()[0].equals("")) {
            text.append(getDepartment()[0] + ">");
        }
        if (!getDepartment()[0].equals("")) {
            text.append(getDepartment()[1] + ">");
        }
        if (!getDepartment()[2].equals("")) {
            text.append(getDepartment()[2] + ">");
        }
        return text.toString();
    }
    public String getHandle() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return " 从" + sdf.format(getStartdate()) + "至" + sdf.format(getHandledate()) + " " + getActionName() + ":" + getLuckyNick();
    }
}