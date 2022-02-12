package com.unicom.betterworry.being;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * 工单属性实体类
 **/
public class WorryItem {
    private Integer processId; //工单processId 唯一标识
    private List<Word> contentSPword; //工单的内容的分词结果
    private List<String> streamDepartments; //工单流水出现的部门
    private Integer categorytype; //工单的基本分类
    private Integer supercategory;//工单的高级分类
    private Integer beautyTag;//工单是否为精品
    private Integer menu; //工单问题出现的菜单，暂未用到
    private Integer location; //工单问题归属方
    private String remarks; //工单备注

    public WorryItem(int processId) {
        this.processId = processId;
    }

    public WorryItem() {
    }

    public Integer getBeautyTag() {
        return beautyTag;
    }

    public void setBeautyTag(Integer beautyTag) {
        this.beautyTag = beautyTag;
    }

    public Integer getMenu() {
        return menu;
    }

    public void setMenu(Integer menu) {
        this.menu = menu;
    }

    public Integer getLocation() {
        return location;
    }

    public void setLocation(Integer location) {
        this.location = location;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<Word> getContentSPword() {
        return contentSPword;
    }

    public void setContentSPword(List<Word> contentSPword) {
        this.contentSPword = contentSPword;
    }

    public List<String> getStreamDepartments() {
        return streamDepartments;
    }

    public void setStreamDepartments(List<String> streamDepartments) {
        this.streamDepartments = streamDepartments;
    }

    public Integer getCategorytype() {
        return categorytype;
    }

    public void setCategorytype(Integer category) {
        this.categorytype = category;
    }

    public Integer getSupercategory() {
        return supercategory;
    }

    public void setSupercategory(Integer supercategory) {
        this.supercategory = supercategory;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public List findItemValue() {
        List<String> cols = new ArrayList();
        List<Object> vals = new ArrayList();
        if (getSupercategory() != null) {
            cols.add("superCategory");
            vals.add(getSupercategory());
        }
        if (getCategorytype() != null) {
            cols.add("truecategory");
            vals.add(getCategorytype());
        }
        if (getBeautyTag() != null) {
            cols.add("beautyTag");
            vals.add(getBeautyTag());
        }
        if (getLocation() != null) {
            cols.add("location");
            vals.add(getLocation());
        }
        if (getMenu() != null) {
            cols.add("menu");
            vals.add(getMenu());
        }
        if (getRemarks() != null) {
            cols.add("remarks");
            vals.add(getRemarks());
        }
        return Arrays.asList(cols, vals);
    }

    public int hashCode() {
        if (processId != null)//重写hashcode方法
            return Integer.hashCode(processId);
        else
            return super.hashCode();
    }


    public boolean equals(Object obj) {  //重写worry的equals方法
        if (!(obj instanceof WorryItem)) { //obj = null的情况
            return false;
        }
        WorryItem worryObj = (WorryItem) obj;// 地址相等
        if (this == worryObj) {
            return true;
        }
        return this.hashCode() == obj.hashCode();
    }
}
