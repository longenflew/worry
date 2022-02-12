package com.unicom.betterworry.service.Impl;

import com.unicom.betterworry.being.Worry;
import com.unicom.betterworry.being.WorryItem;
import com.unicom.betterworry.being.WorryList;
import com.unicom.betterworry.dao.WorryMapper;
import com.unicom.betterworry.dao.WorryMongo;
import com.unicom.betterworry.service.WorryArrService;
import com.unicom.betterworry.util.AddrMapUtil;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 工单列表服务实现类
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 */
@Service
public class WorryArrServiceImpl implements WorryArrService {

    private WorryMapper worryMapper;
    @Autowired
    private WorryMongo worrymongo;
    private final int arrsize = 1000;
    private static final String WORRARRFUTURE="WorrArrfuture";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorryArrServiceImpl.class);
    /*线程池*/
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(15);
    private static final WorrArrfutureMapHeap worrThreadHeap = new WorrArrfutureMapHeap();

    @Autowired
    WorryArrServiceImpl(WorryMapper egg) {
        this.worryMapper = egg;
    }

    @Override
    public WorryList getWorryArr(String ticket, int start, int end) {

        if (start < 0 || end < 0 || start > end) {
            return null;
        }
        if (AddrMapUtil.containsKey(WORRARRFUTURE,ticket)) {     //如果存在为执行的线程就去执行
            WorrArrBuilder waber = (WorrArrBuilder) AddrMapUtil.get(WORRARRFUTURE,ticket);
            FutureTask<Integer> worrArrShit = new FutureTask<Integer>(waber);
            synchronized (waber) {
                if (!waber.isRuntag()) {   //如果执行了设置个标志,并锁住这个对象
                    waber.setRuntag(true);
                    THREAD_POOL.submit(worrArrShit);
                    try {
                        worrArrShit.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        //worrThreadHeap.delete(waber.getIndex());
                        AddrMapUtil.remove(WORRARRFUTURE,ticket);
                        return null;
                    }
                    waber.notifyAll();
                    //worrThreadHeap.delete(waber.getIndex());
                    AddrMapUtil.remove(WORRARRFUTURE,ticket);
                }
            }

        } else if (!checkWorryArr(ticket)) {
            return null;
        }
        List<Worry> worryArr = new ArrayList();
        WorryList worrList = null;
        if (start == end && start == 0)   //如果请求的数量大于实际数量或者end=start=0直接返回值
        {
            return worrymongo.findWorryList(ticket);
        }
        int index = start - start % arrsize;
        worrList = worrymongo.findWorryList(ticket, index);  //找出搜索的工单位于哪一个文档先查询缓存，看看有没有搜索的值
        int size = worrList.getSize();
        if (worrList!=null) {
            List<Worry> valist = worrList.getValue();
            int sum = ((end - end % arrsize) - index) / arrsize;
            for (int j = 0; j < sum; j++) {
                if (index > size) {
                    break;
                }
                index += (j + 1) * arrsize;
                worryArr.addAll(valist.subList(start % arrsize, valist.size()));
                start = index;
                WorryList val=worrymongo.findWorryList(ticket, index);
                if(val==null) {
                    break;
                }

                valist = val.getValue();
            }
            if (end > size) {
                end = valist.size();
            }
            worryArr.addAll(valist.subList(start % arrsize, end % arrsize));
            worrList.setValue(worryArr);
            return worrList;
        }
        return null;
    }

    @Override
    public boolean checkWorryArr(String ticket) {
        Object arg1[] = {"ticket", ticket};
        return worrymongo.checkWorryList((String) arg1[0],  arg1[1]);
    }

    public void createWorryList(WorryList worryList) {
        worryList.setIndex(0);
        worryList.setSize(worryList.getValue().size());
        worryList.setAccess(true);
        worrymongo.saveWorryList(worryList);
    }

    /*
    工单数据分类，返回的数据格式为  { anatype: 哲学问题 /昵称
                              *  VALUE: 100 /真实值
                              *  sum: 1 /总数
                              * }
     */
    @Override
    public List<Map<String, Object>> analysisArrwithType(List<Integer> woArr, String type) throws Exception {
        String sqltypefind = "select * from s_keytype where keytype=?";
        List<Map<String,Object>> valuetype = null;
        try {
            valuetype = worryMapper.find(sqltypefind, new String[]{type});
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        String table, relation;
        String[] col;
        if (valuetype == null || valuetype.isEmpty()) {
            return null;
        }
        table = (String) valuetype.get(0).get("table");
        col = valuetype.get(0).get("columnName").toString().split(";");
        relation = (String) valuetype.get(0).get("colrelation");
        if (col.length == 1) {

            String temp = col[0];
            col = new String[2];
            col[0] = temp + " VALUE";
            col[1] = temp + " as 'anatype'";
        } else if (col.length == 2) {
            for (int i = 0; i < col.length; i++) {
                if (!col[i].contains("VALUE")) {
                    col[i] = col[i] + " as anatype";
                }
            }
        } else {
            return null;
        }
        String sqlfind = "select " + col[0] + " , " + col[1] + " ,count(distinct processId) as 'sum' FROM  " + table + " where processId in (";
        StringBuffer sql = new StringBuffer(sqlfind);
        for (int i = 0; i < woArr.size() - 1; i++) {
            sql.append("?,");
        }
        if (woArr.size() > 0) {
            sql.append("?");
        }
        sql.append(")");
        if (relation != null && !relation.isEmpty()) {
            sql.append("and " + relation);
        }
        sql.append(" group by anatype");
        List<Map<String, Object>> anacate = new ArrayList();
        try {
            List<Map<String,Object>> value = worryMapper.find(sql.toString(), woArr.toArray());
            for (int i = 0; i < value.size(); i++) {
                Map<String, Object> anawor = value.get(i);
                for (Entry temp : anawor.entrySet()) {
                    String key = (String) temp.getKey();
                    Object val = temp.getValue() == null ? "llunnull" : temp.getValue();
                    anawor.put(key, val);
                }
                anacate.add(anawor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        return anacate;
    }  //对工单一些特殊属性分组求数量

    @Override
    public List<Map<String, Object>> guessedWorryanalysis(String ticket, String type) throws Exception {
        List<WorryItem> worryList = getGuessResult(ticket);
        if (worryList == null) {
            return new ArrayList();
        }
        Map<Integer, Set<Integer>> catemap = new HashMap(2);
        Map<Integer, String> cateinfomap;
        String catype;
        if ("super".equals(type)) {
            catype = "st";
            worryList.forEach(w -> {
                if (w.getSupercategory() == null) {
                    w.setSupercategory(100);
                }
                catemap.putIfAbsent(w.getSupercategory(), new HashSet());
                catemap.get(w.getSupercategory()).add(w.getProcessId());
            });
        } else if ("base".equals(type)) {
            catype = "t";
            worryList.forEach(w -> {
                if (w.getCategorytype()== null) {
                    w.setCategorytype(11);
                }
                catemap.putIfAbsent(w.getCategorytype(), new HashSet());
                catemap.get(w.getCategorytype()).add(w.getProcessId());
            });
        } else {
            throw new Exception("type is out of control!");
        }
        cateinfomap = worryMapper.getCategoryinfo(Collections.singletonList(catype)).stream().collect(Collectors.toMap(s -> Integer.parseInt(s.get("keyname").toString()), s -> s.get("keyvalue").toString()));
        List<Map<String, Object>> result = new ArrayList<>();
        catemap.forEach((k, v) -> {
            Map d = new HashMap();
            d.put("sum", v.size());
            String name = cateinfomap.get(k);
            d.put("anatype", name == null ? k : name);
            d.put("VALUE", k);
            d.put("list", v);
            d.put("guessed", true);
            result.add(d);
        });
        return result;
    }


    @Override
    public List<Worry> worrySpecialFilter(List<Map<String, Object>> args, List<Integer> worrylist) {
        String sqltypefind = "select * from s_keytype where keytype=?";
        final String sqlfindbegin = "select processId,processKey,processTitle from c_worry a where processId in ( ";
        String[] sqlarg = new String[1];
        List<Worry> savedata = new ArrayList();
        Map<String, List> retworlist = new LinkedHashMap<>();
        List sqlsargtemp = new ArrayList();
        for (Map<String, Object> arg : args) {
            if (!arg.containsKey("type")) {
                continue;
            }
            sqlarg[0] = (String) arg.get("type");
            StringBuffer sqlsb = null;
            try {
                List<Map<String, Object>> valuetype = worryMapper.find(sqltypefind, sqlarg);
                String table = (String) valuetype.get(0).get("table");
                String[] cols = new String[1];
                if (((String) valuetype.get(0).get("columnName")).contains("VALUE")) {
                    cols = ((String) valuetype.get(0).get("columnName")).split(",");
                    cols[0] = cols[0].substring(0, cols[0].indexOf(" VALUE"));
                } else {
                    cols[0] = (String) valuetype.get(0).get("columnName");
                }

                String relation = (String) valuetype.get(0).get("colrelation");
                String sqlfind = "select processId FROM " + table + " where processId in (";
                sqlsb = new StringBuffer(sqlfind);
                for (int j = 0; j < worrylist.size() - 1; j++) {
                    sqlsb.append("?,");
                }
                if (worrylist.size() > 0) {
                    sqlsb.append("?");
                }
                sqlsb.append(")");

                if (relation != null && !relation.isEmpty()) {
                    sqlsb.append("and ").append(relation);
                }
                sqlsb.append(" and ").append(cols[0]).append("=").append("? and a.processId=processId )");

                sqlsargtemp.addAll(worrylist);
                if (arg.containsKey("VALUE")) {
                    sqlsargtemp.add(arg.get("VALUE"));
                } else {
                    sqlsargtemp.add(arg.get("anatype"));
                }
                savedata.addAll(worryMapper.findWorryBySql(sqlfindbegin + sqlsb, sqlsargtemp.toArray()));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                System.out.println(sqlfindbegin + sqlsb.toString() + "\n" + sqlsargtemp);
            }
        }
        return savedata;
    }  //根据特殊条件搜索工单

    @Override
    public List<Worry> worryFilter(List<Map<String, Object>> args, List<Integer> worrylist, String viewType) throws ParseException, SQLException {
        String sqlworry = "select worry.processId from c_worry worry inner join d_worry_item worry_item on worry.processId=worry_item.processId  ";
        String sqlstream = "select stream.*,s_category.categoryname actionName from (c_worry_stream stream left join s_userorg userorg on stream.username=userorg.username ) left join s_category on handlename=categorytype and categorytag='ht'";
        final String sqlStream = "select processId from c_worry_stream stream left join s_userorg userorg on stream.username=userorg.username ";

        String sql = "select worry.processId,worry.processKey,worry.processTitle,worry.creatorRegion from c_worry worry inner join d_worry_item worry_item on worry.processId=worry_item.processId "
                + " ";
        Map<String,String> sqllist = new LinkedHashMap<>();
        StringBuffer sqlsb;
        if("worry".equals(viewType)) {
            sqlsb = new StringBuffer(sql);
        } else if("stream".equals(viewType)) {
            sqlsb = new StringBuffer(sqlstream);
        } else {
            return null;
        }
        Map<String,List> filerArg=new LinkedHashMap<>();
        List<Object> restrictions = new ArrayList();
        List<Object> restriction = new ArrayList();
        List<Worry> results = new ArrayList<>();
        boolean order = true;
        for (int i = 0; i < args.size(); i++) {
            Map<String, Object> arg = args.get(i);
            List<String> sqlContentList = new ArrayList();
            if (arg.containsKey("username") || arg.containsKey("org_id") || arg.containsKey("department") || arg.containsKey("luckyname") || arg.containsKey("handlename")) {
                String time = "";
                if (arg.containsKey("username")) {
                    String uesrname[] = arg.get("username").toString().split(" ");
                    StringBuffer sqlstr = new StringBuffer("(");
                    for (int ui = 0; ui < uesrname.length - 1; ui++) {
                        sqlstr.append("?,");
                    }
                    sqlstr.append("?)");
                    sqlContentList.add("( stream.username in " + sqlstr + "or stream.usernick in " + sqlstr+" )");
                    restriction.addAll(Arrays.asList(uesrname));
                    restriction.addAll(Arrays.asList(uesrname));

                }
                if (arg.containsKey("handletime")) {
                    sqlContentList.add(" stream.start_date between ? and ?");
                    time = arg.get("handletime").toString().trim();
                    restriction.add(time.substring(0, 10));
                    String end = time.substring(13, 23);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date date;
                    date = formatter.parse(end);
                    restriction.add(time.substring(13, 23));
                }
            }
            if (arg.containsKey("stream")) {
                sqlContentList.add(" answer like ?");
                String stream = (String) arg.get("stream");
                stream = stream.replace(" ", "%");
                stream = stream.replace("%", "\\%");
                restriction.add("%" + stream + "%");
            }
            if (arg.containsKey("org_id")) {
                sqlContentList.add(" orgid= ?");
                restriction.add(arg.get("org_id"));
            }
            if (arg.containsKey("luckyname")) {
                sqlContentList.add(" ( luckyname= ? or luckynick like ?)");
                restriction.add(arg.get("luckyname"));
                restriction.add(arg.get("luckyname") + "%");
            }
            if (arg.containsKey("handlename")) {
                sqlContentList.add(" handlename= ? ");
                restriction.add(arg.get("handlename"));
            }

            if (arg.containsKey("department")) {
                sqlContentList.add(" CONCAT(department1,department2,department3) like ?");
                String department = arg.get("department").toString();
                department = department.replace(" ", "%");
                restriction.add(department);
            }
            //如果需要查询stream表中的数据就进行下一步
            filerArg.put("stream",new ArrayList(restriction));

            if (!sqlContentList.isEmpty()) {

                StringBuilder sbtemp = new StringBuilder();
                for (int j = 0; j < sqlContentList.size()-1; j++) {
                    sbtemp.append(sqlContentList.get(j)).append(" and ");
                }
                sbtemp.append(sqlContentList.get(sqlContentList.size()-1));
                sqllist.put("stream",sbtemp.toString());
                sqlContentList.clear();
            }
            restriction.clear();
            String argtime = null;
            if (arg.containsKey("createTime")) {
                sqlContentList.add("worry.createTime between ? and ?");
                argtime = arg.get("createTime").toString().trim();
                restriction.add(argtime.substring(0, 10));
                String end = argtime.substring(13, 23);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date;
                date = formatter.parse(end);
                restriction.add(end);
            }
            if (arg.containsKey("processKey")) {
                order = false;
                String sqlpro = " worry.processKey in (";
                StringBuffer sqlprosb = new StringBuffer(sqlpro);
                Object a = arg.get("processKey");
                List<String> list = Arrays.asList(a.toString().replaceAll("\\[|]", "").split("[^0-9A-z]"));
                list = list.stream().map(String::trim).collect(Collectors.toList());
                sqlBulid(restriction, sqlContentList, sqlprosb, list);
            }
            if (arg.containsKey("processId")) {
                order = false;
                String sqlpro = " worry.processId in (";
                StringBuffer sqlprosb = new StringBuffer(sqlpro);
                Object a = arg.get("processId");
                List list = null;
                if (a instanceof Collection) {
                    list = new ArrayList((Collection) a);
                } else {
                    list = Arrays.asList(a.toString().replaceAll("\\[|]", "").split("[^0-9A-z]"));
                    list = ((List<String>) list).stream().map(String::trim).collect(Collectors.toList());
                }
                sqlBulid(restriction, sqlContentList, sqlprosb, list);
            }
            if (arg.containsKey("worryType")) {
                sqlContentList.add(" type= ? ");
                restriction.add(arg.get("worryType"));
            }
            if (arg.containsKey("processTitle")) {
                sqlContentList.add(" worry.processTitle like ?");
                String processTitle = arg.get("processTitle").toString();
                processTitle = processTitle.replace(" ", "%");
                processTitle = processTitle.replace("%", "\\%");
                restriction.add("%" + processTitle + "%");
            }
            if (arg.containsKey("content")) {
                sqlContentList.add(" worry.content like ?");
                String content = arg.get("content").toString();
                content = content.replace(" ", "%");
                content = content.replace("%", "\\%");
                restriction.add("%" + content + "%");
            }
            if (arg.containsKey("categoryName")) {
                sqlContentList.add("( worry_item.categorytype = ? or worry_item.truecategory=? )");
                restriction.add(arg.get("categoryName"));
                restriction.add(arg.get("categoryName"));
            }
            if (arg.containsKey("supercategory")) {
                sqlContentList.add("worry_item.supercategory = ?");
                restriction.add(arg.get("supercategory"));
            }
            if (arg.containsKey("creatorRegion")) {
                sqlContentList.add(" worry.creatorRegion like ?");
                restriction.add("%" + arg.get("creatorRegion") + "%");
            }
            if (arg.containsKey("status")) {
                sqlContentList.add("worry_item.processStatus = ?");
                restriction.add(arg.get("status"));
            }

            filerArg.put("worry",new ArrayList(restriction));
            restriction.clear();
            if (!sqlContentList.isEmpty()) {
                StringBuilder sbtemp = new StringBuilder();
                for (int j = 0; j < sqlContentList.size()-1 ; j++) {
                    sbtemp.append(sqlContentList.get(i)).append(" and ");
                }
                sbtemp.append(sqlContentList.get(sqlContentList.size()-1));
                sqllist.put("worry",sbtemp.toString());
            }
            if (arg.containsKey("sql")) {
                String upsql = arg.get("sql").toString();
                upsql = upsql.toLowerCase();
                int a = upsql.indexOf("from");
                if (a >= 0) {
                    upsql = "select processId " + upsql.substring(a);
                    sqllist.put("sql",upsql);
                }
            }
            String limit="";
            if (!sqllist.isEmpty()){
                if(sqllist.containsKey(viewType)){
                    sqlsb.append("where ").append(sqllist.get(viewType)).append(" ");
                    sqllist.remove(viewType);
                    restrictions.addAll(filerArg.get(viewType));
                    filerArg.remove(viewType);
                }
                sqllist.forEach((k,v)->{
                    restrictions.addAll(filerArg.get(k));
                    filerArg.remove(k);
                    if("worry".equals(k)) {
                        sqlsb.append(" and stream.processId in ( ").append(sqlworry).append("where ").append(v).append(")");
                    } else if("stream".equals(k)){
                        sqlsb.append(" and worry.processId in ( ").append(sqlStream).append(" where worry.processId=stream.processId and ").append(v).append(")");
                    }
                    else{
                        sqlsb.append(" and "+viewType+".processId in ( ").append(v).append(")");
                    }
                });
            }
            else {
                limit="limit 10000";
            }
            sqllist.clear();
            if (worrylist == null || worrylist.isEmpty()) {
                if (order) {
                    sqlsb.append(" order by ").append(viewType).append(".processId Desc ").append(limit);
                }
            } else {
                sqlsb.append(" and ").append(viewType).append(".processId in (");
                for (int j = 0; j < worrylist.size() - 1; j++) {
                    sqlsb.append("?,");
                }
                sqlsb.append("?) ");
                restrictions.addAll(worrylist);
            }
            System.out.println(sqlsb + "\n" + restrictions);
            if("worry".equals(viewType)) {
                results.addAll(worryMapper.findWorryBySql(sqlsb.toString(), restrictions.toArray()));
            } else {
                results.addAll(worryMapper.findStreamBySql(sqlsb.toString(), restrictions.toArray()));
            }
        }
        return results;
    } //根据提供的简单条件，工单过滤

    private void sqlBulid(List<Object> restrictions, List<String> sqllisttemp, StringBuffer sqlprosb, List list) {
        for (int j = 0; j < list.size() - 1; j++) {
            sqlprosb.append("?,");
        }
        if (list.size() > 0) {
            sqlprosb.append("? )");
        }
        sqllisttemp.add(sqlprosb.toString());
        restrictions.addAll(list);
    }

    @Override
    public void bulidWorryArr(WorryList readyWorrylist, List<Integer> limitProid, int searchtype) {
        if (checkWorryArr(readyWorrylist.getTicket())) {
            return;
        }
        WorrArrBuilder woArrBuilder = new WorrArrBuilder(this);
        woArrBuilder.setBase(readyWorrylist, limitProid, searchtype);
        Entry<String, Long> temp = woArrBuilder.getEntry();
        Entry<String, Integer> ininfo = WorrArrfutureMapHeap.insert(temp);
        if (ininfo.getKey() != null) {
            AddrMapUtil.remove(WORRARRFUTURE,ininfo.getKey());
        }
        AddrMapUtil.put(WORRARRFUTURE,readyWorrylist.getTicket(), woArrBuilder);
    }

    @Override
    public byte[] worryarroutfile(List<Integer> worrylist) {
        Map<Integer, String> keymap = new HashMap();
        worryMapper.getCategoryinfo(Arrays.asList("t", "st")).forEach(m -> keymap.put((Integer) m.get("keyname"), (String) m.get("keyvalue")));
        ByteArrayOutputStream outbyte = new ByteArrayOutputStream();
        WritableWorkbook excl;
        try {
            excl = Workbook.createWorkbook(outbyte);
            WritableSheet sheet = excl.createSheet("sheet1", 0);
            List<Worry> worryall = worryMapper.findWorryList(worrylist);
            //for (int i = 0; i < worryall.size(); i++)
            sheet.addCell(new Label(0, 0, "工单编码"));
            sheet.addCell(new Label(1, 0, "工单地域"));
            sheet.addCell(new Label(2, 0, "创建时间"));
            sheet.addCell(new Label(3, 0, "工单标题"));
            sheet.addCell(new Label(4, 0, "工单内容"));
            sheet.addCell(new Label(5, 0, "工单流水"));
            sheet.addCell(new Label(6, 0, "种类"));
            sheet.addCell(new Label(7, 0, "高级分类"));
            sheet.addCell(new Label(8, 0, "工单状态"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (int i = 0; i < worryall.size(); i++) {
                sheet.addCell(new Label(0, i + 1, worryall.get(i).getProcessKey()));
                sheet.addCell(new Label(1, i + 1, worryall.get(i).getCreatorRegion()));
                sheet.addCell(new Label(2, i + 1, sdf.format(worryall.get(i).getCreateTime())));
                sheet.addCell(new Label(3, i + 1, worryall.get(i).getProcessTitle()));
                sheet.addCell(new Label(4, i + 1, worryall.get(i).getContent()));
                sheet.addCell(new Label(5, i + 1, worryall.get(i).streamTostring()));
                sheet.addCell(new Label(6, i + 1, keymap.get(worryall.get(i).getCategorytype())));
                sheet.addCell(new Label(7, i + 1, keymap.get(worryall.get(i).getSupercategory())));
                sheet.addCell(new Label(8, i + 1, Integer.toString(worryall.get(i).getProcessStatus())));
            }
            excl.write();
            excl.close();
        } catch (IOException | WriteException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return outbyte.toByteArray();
    }

    @Override
    public void setWorryArrAccess(String ticket, boolean fixed) {
        Map in = new HashMap();
        in.put("ticket", ticket);
        Map data = new HashMap();
        data.put("access", fixed);
        worrymongo.updateMap(in, data, "worrylist");
        in.remove("ticket");
        worrymongo.findWorryListSonTicket(ticket).forEach(f -> {
            in.put("fatherTicket", f);
            worrymongo.updateMapAll(in, data, "worrylist");
        });
    }

    @Override
    public void recoverGuessedState(String ticket) {
        Map arg = new HashMap(4);
        arg.put("ticket", ticket);
        arg.put("access", true);
        Map data = new HashMap();
        data.put("guessed", 0);
        worrymongo.updateMap(arg, data, "worrylist");
    }

    @Override
    public Integer getWorryitemGuessState(String ticket) {
        return worrymongo.getWorryListGuessState(ticket);
    }

    @Override
    public Set<String> getWorryGuessedSonTicket(String ticket) {
        Set<String> fticket = worrymongo.findWorryListSonTicket(ticket);
        return fticket.stream().filter(s -> worrymongo.getWorryListGuessState(s)%10 == 1).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getWorryGuessedFatherTicket(String ticket) {
        Set<String> fticket = worrymongo.findWorryListFatherTicket(ticket);
        return fticket.stream().filter(s -> worrymongo.getWorryListGuessState(s)%10 == 1).collect(Collectors.toSet());
    }

    public List<WorryItem> getGuessResult(String ticket) throws Exception {
        if (!worrymongo.checkWorryList("ticket", ticket)) {
            throw new Exception("worrylist is not existed");
        }
        Integer guessed = getWorryitemGuessState(ticket);
        if (guessed < 2) {
            throw new Exception("worrylist state is not accessable!");
        }
        List<WorryItem> wtl = new ArrayList();
         worrymongo.findWorryList( ticket).getValue().forEach(d -> {
            WorryItem wt = new WorryItem();
             WorryItem wtt =  d.getWorryItem();
            if (wtt != null) {
                wt.setSupercategory(wtt.getSupercategory());
                wt.setCategorytype(wtt.getCategorytype());
                wt.setProcessId( d.getProcessId());
                wtl.add(wt);
            }
        });
        return wtl;
    }


}

class WorrArrfutureMapHeap {
    static private List<Entry<String, Long>> Heaparr = new CopyOnWriteArrayList();
    private static AtomicInteger size = new AtomicInteger(0);
    private final static int worrArrfutureMapsize = 1000;
    static Entry insert(Entry<String, Long> wrrinfo) {
        String tk = null;
        if (size.get() > worrArrfutureMapsize) {
            tk = deleteMin();
        }
        int index = size.get();
        Heaparr.add(size.get(), wrrinfo);
        while (Heaparr.get(index / 2).getValue() > wrrinfo.getValue()) {
            Entry temp = Heaparr.get(index / 2);
            Heaparr.set(index / 2, wrrinfo);
            Heaparr.set(index, temp);
            index /= 2;
        }
        final int lastindex = index;
        final String lasttk = tk;
        size.getAndIncrement();
        Entry entry = new Entry() {
            @Override
            public String getKey() {
                return lasttk;
            }

            @Override
            public Integer getValue() {
                return lastindex;
            }

            @Override
            public Object setValue(Object value) {
                return null;
            }
        };
        return entry;
    }

    static String deleteMin() {
        if (size.get() == 0) {
            return null;
        }
        String tk = null;
        tk = Heaparr.get(0).getKey();
        Heaparr.set(0, Heaparr.get(size.get() - 1));
        Heaparr.remove(size.decrementAndGet());
        int father = 0;
        for (int child = father * 2 + 1; child < size.get() + 1; child = 2 * father + 1) {
            if (child != size.get() - 1 && Heaparr.get(child).getValue() > Heaparr.get(child + 1).getValue()) {
                child++;
            }
            if (Heaparr.get(father).getValue() > Heaparr.get(child).getValue()) {
                Entry temp = Heaparr.get(father);
                Heaparr.set(father, Heaparr.get(child));
                Heaparr.set(child, temp);
                father = child;
            } else {
                break;
            }
        }
        size.getAndDecrement();
        return tk;
    }

    static String delete(int index) {
        if (index > size.get()) {
            return null;
        }
        String tk = null;
        tk = Heaparr.get(index).getKey();
        Heaparr.set(index, Heaparr.get(size.get() - 1));
        Heaparr.remove(size.decrementAndGet());
        int father = index;
        for (int child = father * 2 + 1; child < size.get(); child = 2 * father + 1) {
            if (child != size.get() - 1 && Heaparr.get(child).getValue() > Heaparr.get(child + 1).getValue()) {
                child++;
            }
            if (Heaparr.get(father).getValue() > Heaparr.get(child).getValue()) {
                Entry temp = Heaparr.get(father);
                Heaparr.set(father, Heaparr.get(child));
                Heaparr.set(child, temp);
                father = child;
            } else {
                break;
            }
        }
        return tk;
    }
}

/**
 * worrylist建筑者类,用于生成工单执行的程序
 */
class WorrArrBuilder implements Callable {
    /**
     * 工单列表的基础信息，已不使用
     */
    @Deprecated
    Map baseinfo;
    /**
     * 工单processId的列表
     */
    List<Integer> proid;
    /**
     * 工单过滤条件
     */
    List<Map<String, Object>> filterargs;
    /**
     * 线程需要依赖的服务 WorryArrServiceImpl
     */
    WorryArrServiceImpl worrarrsvc;
    /**
     * 搜索方式 0为正常搜索，1为特殊限制搜索
     */
    int searchtype = 0;
    /**
     * 实体类生成的时间
     */
    long time;
    /**
     * 执行标识，true为已经执行
     */
    Boolean runtag = false;
    /**
     * readyWorrylist为带生成的worrylist的一些基本参数
     */
    WorryList readyWorrylist;
    /**
     * 展示样式，worrylist.viewType
     */
    String viewType = "worry";

    class BuliderNode implements Entry<String, Long> {

        @Override
        public String getKey() {
            return readyWorrylist.getTicket();
        }

        @Override
        public Long getValue() {
            return time;
        }

        @Override
        public Long setValue(Long value) {
            return null;
        }
    }

    WorrArrBuilder(WorryArrServiceImpl svc) {
        worrarrsvc = svc;
        filterargs = new ArrayList<>();
        baseinfo = new HashMap();
        proid = new ArrayList<>();
        time = System.currentTimeMillis();
    }

    Entry getEntry() {
        return new BuliderNode();
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public long getTime() {
        return time;
    }

    public void setRuntag(Boolean runtag) {
        this.runtag = runtag;
    }

    public Boolean isRuntag() {
        return runtag;
    }

    @Deprecated
    void setBase(Map baseinfo, List<Integer> proid, List<Map<String, Object>> filterargs, int searchtype) {
        this.filterargs.clear();
        this.baseinfo.clear();
        if (proid == null) {
            this.proid = null;
        } else {
            this.proid = new ArrayList<>();
            this.proid.addAll(proid);
        }
        this.baseinfo.putAll(baseinfo);
        this.filterargs.addAll(filterargs);
        this.searchtype = searchtype;
    }
@Deprecated
    void setBase(Map baseinfo, List<Integer> proid, List<Map<String, Object>> filterargs) {
        setBase(baseinfo, proid, filterargs, 0);
    }

    void setBase(WorryList readyWorryList, List<Integer> limitProid, int searchtype) {
        readyWorrylist = readyWorryList;
        this.searchtype = searchtype;
        if (limitProid == null) {
            this.proid = null;
        } else {
            this.proid = new ArrayList<>();
            this.proid.addAll(limitProid);
        }
    }

    @Override
    public Integer call() throws ParseException, SQLException {
        if (worrarrsvc.checkWorryArr(readyWorrylist.getTicket())) {
            return 0;
        }
        if (searchtype == 0) {
            List<Worry> worryList = worrarrsvc.worryFilter(readyWorrylist.getFilter(), proid, readyWorrylist.getViewType());
            readyWorrylist.setValue(worryList);
            worrarrsvc.createWorryList(readyWorrylist);
        } else if (searchtype == 1) {
            List<Worry> worryList = worrarrsvc.worrySpecialFilter(readyWorrylist.getFilter(), proid);
            readyWorrylist.setValue(worryList);
            worrarrsvc.createWorryList(readyWorrylist);
        }
        return 1;
    }
}