package com.unicom.betterworry.being;

import net.sf.json.JSONArray;
import java.util.*;

/**
 * 工单实体类
 */
public class Worry { // 工单类
    /**
     * 工单ID
     */
    private int processId;
    /**
     * 工单编号 OJS2020092401508
     */
    private String processKey;
    /**
     * 工单标题 70订单正式提交或下发失败或完工失败
     */
    private String processTitle;
    /**
     * 工单关联的工单，一般为普通事件单关联问题单
     */
    private Integer correlationId;
    /**
     * 工单类型,0为普通工单，1为问题单
     */
    private String type;
    private Date createTime;// "2020-08-11 16:50"
    private String creatorRegion;//江苏省-省本部
    private Integer processStatus;// 工单状态，0为待处理，3为未解决，1和2为已解决
    private String content; // 工单内容
    private List<Stream> stream;//工单流水
    private WorryItem worryItem;//工单属性
    private List<Remark> remarkList;//工单评论

    /**
     * Instantiates a new Worry.
     *
     * @param processId     the process id
     * @param processKey    the process key
     * @param processTitle  the process title
     * @param createTime    the create time
     * @param creatorRegion the creator region
     * @param processStatus the process status
     * @param content       the content
     * @param stream        the stream
     * @param category      the category
     * @param supercate     the supercate
     */
    Worry(int processId, String processKey, String processTitle, Date createTime, String creatorRegion,
          int processStatus, String content, ArrayList<Stream> stream, int category, int supercate) {
        this.processId = processId; // 工单编号
        this.processKey = processKey;// :OJS2020092401508
        this.processTitle = processTitle;// :70订单正式提交或下发失败或完工失败
        this.createTime = createTime;// "2020-08-11 16:50"
        this.creatorRegion = creatorRegion;// 江苏省-省本部
        this.processStatus = processStatus;// 已关单
        this.content = content; // 订单内容
        this.stream = stream; // 工单流水
        worryItem = new WorryItem(processId);
        worryItem.setCategorytype(category);
        worryItem.setSupercategory(supercate);
    }

    /**
     * Instantiates a new Worry.
     *
     * @param processId     the process id
     * @param processKey    the process key
     * @param processTitle  the process title
     * @param createTime    the create time
     * @param creatorRegion the creator region
     * @param processStatus the process status
     * @param content       the content
     * @param category      the category
     * @param supercate     the supercate
     */
    Worry(int processId, String processKey, String processTitle, Date createTime, String creatorRegion,
          int processStatus, String content, int category, int supercate) {
        this.processId = processId; // 工单编号
        this.processKey = processKey;// :OJS2020092401508
        this.processTitle = processTitle;// :70订单正式提交或下发失败或完工失败
        this.createTime = createTime;// "2020-08-11 16:50"
        this.creatorRegion = creatorRegion;// 江苏省-省本部
        this.processStatus = processStatus;// 已关单
        this.content = content; // 订单内容
        worryItem = new WorryItem(processId);
        worryItem.setCategorytype(category);
        worryItem.setSupercategory(supercate);
    }

    /**
     * Instantiates a new Worry.
     */
    public Worry() {
    }

    /**
     * Instantiates a new Worry.
     *
     * @param processId the process id
     */
    public Worry(int processId) {
        this.processId = processId;
    }

    /**
     * Stream tostring string.
     *
     * @param stream the stream
     * @return the string
     */
    public static String streamTostring(ArrayList<Stream> stream) {
        if (stream == null)
            return "";
        String text = "";
        for (int i = 0; i < stream.size(); i++) {
            text = text + stream.get(i).toString() + "\n";
        }
        return text;
    }

    /**
     * Sets process id.
     *
     * @param processId the process id
     */
    public void setProcessId(int processId) {
        this.processId = processId;
    }

    /**
     * Sets process key.
     *
     * @param processKey the process key
     */
    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    /**
     * Gets correlation id.
     *
     * @return the correlation id
     */
    public Integer getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets correlation id.
     *
     * @param correlationId the correlation id
     */
    public void setCorrelationId(Integer correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * Gets worry item.
     *
     * @return the worry item
     */
    public WorryItem getWorryItem() {
        return worryItem;
    }

    /**
     * Sets worry item.
     *
     * @param worryItem the worry item
     */
    public void setWorryItem(WorryItem worryItem) {
        this.worryItem = worryItem;
    }

    /**
     * Sets process title.
     *
     * @param processTitle the process title
     */
    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    /**
     * Sets creator region.
     *
     * @param creatorRegion the creator region
     */
    public void setCreatorRegion(String creatorRegion) {
        this.creatorRegion = creatorRegion;
    }

    /**
     * Sets process status.
     *
     * @param processStatus the process status
     */
    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    /**
     * Sets categorytype.
     *
     * @param category the category
     */
    public void setCategorytype(Integer category) {
        if (worryItem == null)
            worryItem=new WorryItem(processId);
        this.worryItem.setCategorytype(category);
    }

    /**
     * Sets supercategory.
     *
     * @param supercategory the supercategory
     */
    public void setSupercategory(Integer supercategory) {
        if (worryItem == null)
            worryItem=new WorryItem(processId);
        this.worryItem.setSupercategory(supercategory);
    }

    /**
     * Gets process id.
     *
     * @return the process id
     */
    public int getProcessId() {
        return processId;
        // 工单编码
    }

    /**
     * Gets process key.
     *
     * @return the process key
     */
    public String getProcessKey() {
        return processKey;
        // :OJS2020092401508
    }

    /**
     * Gets process title.
     *
     * @return the process title
     */
    public String getProcessTitle() {
        return processTitle;
        // :70订单正式提交或下发失败或完工失败
    }

    /**
     * Sets create time.
     *
     * @param createTime the create time
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * Gets create time.
     *
     * @return the create time
     */
    public Date getCreateTime() {
        return createTime;

    }// :"2020-08-11 16:50"

    /**
     * Gets creator region.
     *
     * @return the creator region
     */
    public String getCreatorRegion() {
        return creatorRegion;
        // :江苏省-省本部
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets process status.
     *
     * @return the process status
     */
    public Integer getProcessStatus() {
        return processStatus;
        // 已关单
    }


    /**
     * Gets categorytype.
     *
     * @return the categorytype
     */
    public Integer getCategorytype() {
        if (worryItem == null)
            return null;
        return worryItem.getCategorytype();

    }

    /**
     * Gets supercategory.
     *
     * @return the supercategory
     */
    public Integer getSupercategory() {
        if (worryItem == null)
            return null;
        return worryItem.getSupercategory();
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
        // 订单内容
    }

    /**
     * Update stream.
     *
     * @param water the water
     */
    public void updateStream(JSONArray water) {
        int[] state = {0};
        state[0] = this.processStatus;
        stream=WorryFactory.waterHold(water, state);
    }

    /**
     * Gets remarks.
     *
     * @return the remarks
     */
    public String getRemarks() {
        if (worryItem == null)
            return null;
        return worryItem.getRemarks();
    }

    /**
     * Sets remarks.
     *
     * @param remarks the remarks
     */
    public void setRemarks(String remarks) {
        if (worryItem == null)
            worryItem=new WorryItem(processId);
        worryItem.setRemarks(remarks);
    }


    /**
     * Gets stream.
     *
     * @return the stream
     */
    public List<Stream> getStream() {
        return stream;
    }

    /**
     * Sets stream.
     *
     * @param stream the stream
     */
    public void setStream(List<Stream> stream) {
        this.stream = stream;
    }

    /**
     * Gets menu.
     *
     * @return the menu
     */
    public Integer getMenu() {
        if (worryItem == null)
            return null;
        return worryItem.getMenu();
    }

    /**
     * Sets menu.
     *
     * @param menu the menu
     */
    public void setMenu(Integer menu) {
        if (worryItem == null)
            worryItem=new WorryItem(processId);
        worryItem.setMenu(menu);
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public Integer getLocation() {
        if (worryItem == null)
            return null;
        return worryItem.getLocation();
    }

    /**
     * Sets location.
     *
     * @param location the location
     */
    public void setLocation(Integer location) {
        if (worryItem == null)
            worryItem=new WorryItem(processId);
        worryItem.setLocation(location);
    }

    /**
     * Gets beauty tag.
     *
     * @return the beauty tag
     */
    public Integer getBeautyTag() {
        if (worryItem == null)
            return null;
        return worryItem.getBeautyTag();
    }

    /**
     * Sets beauty tag.
     *
     * @param beautyTag the beauty tag
     */
    public void setBeautyTag(Integer beautyTag) {
        if (worryItem == null)
            worryItem=new WorryItem(processId);
        worryItem.setMenu(beautyTag);
    }

    /**
     * Stream tostring string.
     *
     * @return the string
     */
    public String streamTostring() {
        if (stream == null)
            return "";
        String text = "";
        for (int i = 0; i < stream.size(); i++) {
            text = text + stream.get(i).toString() + "\n";
        }
        return text;
    }

    /**
     * Find item value list.
     *
     * @return the list
     */
    public List findItemValue() {
        if(worryItem==null)
            return new ArrayList();
        return worryItem.findItemValue();
    }

    /**
     * Gets remark list.
     *
     * @return the remark list
     */
    public List<Remark> getRemarkList() {
        return remarkList;
    }

    /**
     * Sets remark list.
     *
     * @param remarkList the remark list
     */
    public void setRemarkList(List<Remark> remarkList) {
        this.remarkList = remarkList;
    }

    @Override
    public int hashCode() {              //重写hashcode方法
        return Integer.hashCode(processId);
    }

    public boolean equals(Object obj) {  //重写worry的equals方法
        if (!(obj instanceof Worry)) { //obj = null的情况
            return false;
        }
        Worry worryObj = (Worry) obj;// 地址相等
        if (this == worryObj) {
            return true;
        }
        return this.hashCode() == obj.hashCode();
    }


    @Override
    public String toString() {
        return "Worry{" +
                "processStatus=" + processStatus +
                ", category=" + getCategorytype() +
                ", supercategory=" + getSupercategory() +
                ", beautyTag=" + getBeautyTag() +
                ", menu=" + getMenu() +
                ", location=" + getLocation() +
                ", remarks='" + getRemarks() + '\'' +
                '}';
    }
}

