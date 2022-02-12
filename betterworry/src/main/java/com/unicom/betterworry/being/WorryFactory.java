package com.unicom.betterworry.being;

import com.unicom.betterworry.util.ClassUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
*  @ worry静态工厂类，可以根据MAP键值映射生成being包下所有实体类
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 */
@Component
public class WorryFactory {
    public static Map<Integer, Worry> works;
    private static final Set<String> stableValueSet = new HashSet();

    static {
        stableValueSet.add("processid");
        stableValueSet.add("processkey");
        stableValueSet.add("processtitle");
        stableValueSet.add("content");
        stableValueSet.add("stream");
        stableValueSet.add("creatorregion");
        stableValueSet.add("createtime");
        stableValueSet.add("type");
        stableValueSet.add("lasttime");
        stableValueSet.add("correlationid");
        stableValueSet.add("processstatus");
        stableValueSet.add("reamrkid");
    }

    /**
     * 通过data的键值来创建worry实体类
     * @param data
     * @return
     * @throws ParseException
     */
    public static Worry GetWorryFromMap(Map<String, ?> data) throws ParseException {
        Worry worrys;
        String regx = "^[0-9]+$";
        Map<String, Object> cpData = new HashMap();
        data.forEach((key, value) -> cpData.put(key.toLowerCase(), value));
        worrys = new Worry();
        Class<Worry> worryclz = Worry.class;
        Method methods[] = worryclz.getDeclaredMethods();
        Arrays.stream(methods).forEach(method -> {
            String argname = method.getName().toLowerCase();
            int i = argname.indexOf("set");
            if (i >= 0) {
                argname = argname.substring(i + "set".length());
                Object val = cpData.get(argname);
                Class argclz[] = method.getParameterTypes();
                try {
                    if (val != null && argclz.length == 1) {
                        if (val.toString().matches(regx) &&
                                (argclz[0].getName().equals("java.lang.Integer") || argclz[0].getName().equals("java.lang.Long")|| argclz[0].getName().equals("int")|| argclz[0].getName().equals("long"))){
                            val = Integer.parseInt(val.toString());
                            method.invoke(worrys, val);
                        }
                        if ((val.getClass() == argclz[0])) {
                            method.invoke(worrys, val);
                        }
                        else if(argclz[0].getName().equals("java.lang.String"))
                            method.invoke(worrys, val.toString());
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
        if (data.containsKey("categorytype"))
            worrys.setCategorytype(Integer.parseInt( data.get("categorytype").toString()));
        if (data.containsKey("createTime")) {
            Date tempc;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            tempc = sdf.parse(data.get("createTime").toString());
            worrys.setCreateTime(tempc);
        }
        return worrys;
    }

    /**
     * 根据提供的map创建实体类。注意，该系列方法只会赋值单一数据，复杂的类不会赋值。如worry中的streams就不会生成，需要外面拼接
     * @param enityclz 实体类的类型
     * @param data 数据
     * @param <T>
     * @return T的实体类
     * @throws Exception
     */
    public static <T>T univerCreateFromMap(Class<T> enityclz,Map<String, ?> data) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        String regx = "^[0-9]*$";
        Map<String, Object> cpData = new HashMap();
        data.forEach((key, value) -> cpData.put(key.toLowerCase(), value));
        T entity;
        Set<Class<?>> clzSet=ClassUtil.getClassSet(WorryFactory.class.getPackage().getName());
        if(!clzSet.contains(enityclz))
            throw new Exception( enityclz.getSimpleName()+" is not supported");
        if (!cpData.containsKey("processid"))
            entity=enityclz.getDeclaredConstructor().newInstance();
        else
            entity=enityclz.getDeclaredConstructor(int.class).newInstance( cpData.get("processid"));
        return newClass((Class<T>) enityclz, regx, cpData, entity);
    }

    private static <T> T newClass(Class<T> enityclz, String regx, Map<String, Object> cpData, T entity) {
        Method methods[] = enityclz.getDeclaredMethods();
        boolean b = false;
        for (Method method : methods) {
            String argname = method.getName().toLowerCase();
            int i = argname.indexOf("set");
            if (i >= 0) {
                argname = argname.substring(i + "set".length());
                if (!stableValueSet.contains(argname)) {
                    Object val = cpData.get(argname);
                    Class argclz[] = method.getParameterTypes();
                    b = true;
                    try {
                        if (val != null && argclz.length == 1) {
                            if (val.toString().matches(regx) && (argclz[0].getName().equals("java.lang.Integer") || argclz[0].getName().equals("java.lang.Long")))
                                val = Integer.parseInt(val.toString());
                            if ((val.getClass() == argclz[0])) {
                                method.invoke(entity, val);
                            }

                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return entity;
    }

    public static WorryItem GetWorryItemFromMap(Map<String, ?> data) {

        try {
            return univerCreateFromMap(WorryItem.class,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Worry GetWorryFromSearch(JSONObject data, JSONArray water, int category) throws ParseException { //存储方法创建worry
        int processId;
        processId = Integer.parseInt(data.getString("processId"));
        if (works.containsKey(processId))  //如果这个工单不是这边的，就跳过
            return null;
        Worry worry;
        Date tempc, temph;
        int[] state = {0};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        tempc = sdf.parse(data.getString("createDateStr"));
        //temph = sdf.parse(data.getString("dealDateStr"));

        state[0] = Integer.parseInt(data.getString("status"));
        ArrayList<Stream> streams = waterHold(water, state);
        if (streams == null) {
            return null;
        }
        String con = data.getString("content");
        if (state[0] == 0)
            category = 11;
        worry = new Worry(processId, data.getString("processKey"), data.getString("title"), tempc,
                data.getString("userProvince"), state[0], con, streams, category, 100);
        //if (!process.add(worry)) // 判断这个工单是否存在，存在就不添加
        return worry;
    }

    public static Worry GetWorry(JSONObject data, JSONObject content, JSONArray water) throws ParseException {
        Worry worry;
        Date tempc;
        int state[] = {0};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int processId;
        tempc = sdf.parse(data.getString("createTime"));
        processId = Integer.parseInt(data.getString("processId"));

        if (content.getJSONObject("data").getString("resovledLabelType").equals("2"))
            state[0] = 2;
        else if (content.getJSONObject("data").getString("resovledLabelType").equals("1"))
            state[0] = 1;
        else if (content.getJSONObject("data").getString("resovledLabelType").equals("3"))
            state[0] = 3;
        ArrayList<Stream> streams = waterHold(water, state);
        String con = content.getJSONObject("data").getString("content");
        worry = new Worry(processId, data.getString("processKey"), data.getString("processTitle"), tempc,
                data.getString("creatorRegion"), state[0], purify(con), streams, 11, 100);
        return worry;

    }

    public static Remark createRemark(Map<String, ?> data) {
        Remark r=null;
        try {
            r=univerCreateFromMap(Remark.class,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public static Remark createWorryItem(Map<String, ?> data) {
        Remark remark;
        String regx = "^[0-9]*$";
        Map<String, Object> cpData = new HashMap();
        data.entrySet().forEach(m -> cpData.put(m.getKey().toLowerCase(), m.getValue()));
        if (!cpData.containsKey("processid"))
            return null;
        remark = new Remark((int) cpData.get("remarkid"), (int) cpData.get("processid"));
        Class worryclz = Remark.class;
        return newClass(worryclz, regx, cpData, remark);
    }

    static String purify(String in) {
        String scriptRegex = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义style的正则表达式，去除style样式，防止css代码过多时只截取到css样式代码
        String styleRegex = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义HTML标签的正则表达式，去除标签，只提取文字内容
        String htmlRegex = "<[^>]+>"; // 定义空格,回车,换行符,制表符
        String spaceRegex = "\\s*|\t|\r|\n";
        //in = in.replaceAll(scriptRegex, "");
        //in = in.replaceAll(htmlRegex, "");
        in = in.replaceAll(spaceRegex, "");
        //in = in.replaceAll("&nbsp;", "");
        //in = in.replaceAll(styleRegex, "");
        return in;
    }

    public static ArrayList<Stream> waterHold(JSONArray water, int start, int end, int[] state) {
        if (end == start)
            return null;
        ArrayList<Stream> streams;
        streams = new ArrayList<Stream>();
        Long num;
        Date stime, htime;
        String answer;
        int statustag = 1, tag = 0;
        num = Long.parseLong(water.getJSONObject(0).getString("handleDate"));
        htime = new Date(num);
        for (int i = start; i < end; i++) {
            String[] department = new String[3];
            String[] todepartment = new String[3];
            if (!water.getJSONObject(i).getString("systype1Name").equals("null"))
                department[0] = water.getJSONObject(i).getString("systype1Name");
            else
                department[0] = "";
            if (!water.getJSONObject(i).getString("systype2Name").equals("null"))
                department[1] = water.getJSONObject(i).getString("systype2Name");
            else
                department[1] = "";
            if (!water.getJSONObject(i).getString("systype3Name").equals("null"))
                department[2] = water.getJSONObject(i).getString("systype3Name");
            else
                department[2] = "";
            if (!water.getJSONObject(i).getString("toSystype1Name").equals("null"))
                todepartment[0] = water.getJSONObject(i).getString("toSystype1Name");
            else
                todepartment[0] = "";
            if (!water.getJSONObject(i).getString("toSystype2Name").equals("null"))
                todepartment[1] = water.getJSONObject(i).getString("toSystype2Name");
            else
                todepartment[1] = "";
            if (!water.getJSONObject(i).getString("toSystype3Name").equals("null"))
                todepartment[2] = water.getJSONObject(i).getString("toSystype3Name");
            else
                todepartment[2] = "";
            if (department[0].contains("总部BSS"))
                tag = 1;
            if (!water.getJSONObject(i).getString("content").equals("null"))
                answer = water.getJSONObject(i).getString("content");
            else
                answer = "";
            num = Long.parseLong(water.getJSONObject(i).getString("handleDate"));
            stime = htime;
            htime = new Date(num);

            if (water.getJSONObject(i).getString("handleName").contains("回答") && !water.getJSONObject(i).getString("handleNick").contains("工号")) {
                statustag = 4;
            }
            if (statustag == 4 && water.getJSONObject(i).getString("handleName").contains("回退"))
                statustag = 1;
            String lname, lnick;
            lname = water.getJSONObject(i).getString("toUserName");
            lnick = water.getJSONObject(i).getString("toUserNick");
            if (lnick.equals("null")) {
                lnick = water.getJSONObject(i).getString("toCircleName");
                lname = "";
            }
            if (lnick.equals("null")) {
                lnick = "";
                lname = "";
            }

            Stream stream = new Stream(Integer.parseInt(water.getJSONObject(i).getString("id")), water.getJSONObject(i).getString("handleName"),
                    water.getJSONObject(i).getString("handleNick"), lname, lnick, water.getJSONObject(i).getInt("handleTypeName"),
                    answer, department, todepartment, stime, htime);
            streams.add(stream);
        }
        state[0] = 4;
        if (tag == 0 && start == 0)
            return null;
        return streams;
    }

    public static ArrayList<Stream> waterHold(JSONArray water, int[] state) {
        return waterHold(water, 0, water.size(), state);
    }
}

/*
 * Worry(int processId,String processKey,String processTitle, Date createTime,
 * String creatorRegion,Date handleTime, int processStatus,String
 * content,ArrayList<Stream> stream ){ this.processId=processId; //工单编码
 * this.processKey=processKey;//:OJS2020092401508
 * this.processTitle=processTitle;//:70订单正式提交或下发失败或完工失败
 * this.createTime=createTime;//:"2020-08-11 16:50"
 * this.creatorRegion=creatorRegion;//:江苏省-省本部
 * this.handleTime=handleTime;//:"2020-08-11 16:50"
 * this.processStatus=processStatus;//:已关单 this.content=content; //订单内容
 * this.stream=stream; }
 *
 *
 * processStatus:已关单 processTitle:70订单正式提交或下发失败或完工失败 processKey:OJS2020092401508
 * createTime:"2020-08-11 16:50" creatorRegion:江苏省-省本部
 * handleTime:"2020-08-11 16:50" processId:918569155
 *
 */