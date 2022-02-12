package com.unicom.betterworry.dao.Impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import com.unicom.betterworry.being.*;
import com.unicom.betterworry.dao.WorryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * worry关系型数据库DAO类
 * 目前里面的操作是使用的DatabasePool，可以继承DatabasePool这个类进行改进。
 */
@Repository
@Scope("prototype")
@Primary
public class WorryMapperImpl implements WorryMapper {

    @Autowired
    DatabasePool database;//数据库连接池的类
    String dbname;
    private static final Map<String, String> colMap = new HashMap();

    static {
        colMap.put("category", "truecategory");
    }

    /**
     * mysqlunicom 默认使用配置mysqlunicom 可在c3p0-config.xml 查看配置文件
     */
    public WorryMapperImpl() {
        dbname = "mysqlunicom";
    }

    public WorryMapperImpl(String db) {
        dbname = db;
        System.out.println(db);
    }

    public void setdb(String db) {
        dbname = db;
    }

    /**
     * 数据库连接，调用
     *
     * @return
     */
    public Connection getConnect() {
        Connection connect = null;
        try {
            connect = getConnect(dbname);
        } catch (Exception e) {
            System.out.println("数据库连接异常");
            e.printStackTrace();
        }
        return connect;
    }

    public Connection getConnect(String name) {
        Connection connect = null;
        try {
            connect = database.getConnect(name);
        } catch (Exception e) {
            System.out.println("数据库连接异常");
            e.printStackTrace();
        }
        return connect;
    }

    /**
     * 每一次数据库操作完成后，都要使用该方法关闭连接
     * @param connect
     * @param presql
     * @param value
     */
    public void closeConnect(Connection connect, PreparedStatement presql, ResultSet value) {
        try {

            database.close(connect, presql, value);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 工单插入方法，目前本工程中不会使用到
     *
     * @param egg
     */
    public void insertWorry(Worry egg) { // 插入数据
        Timestamp tempc = new Timestamp(egg.getCreateTime().getTime());
        insertWorry(egg.getStream(), egg.getProcessId(), egg.getProcessKey());
        String produce = "call insertworry(?,?,?,?,?,?,?)";
        PreparedStatement presql = null;
        Connection connect = getConnect();
        try {
            presql = connect.prepareStatement(produce);
            presql.setInt(1, egg.getProcessId());
            presql.setString(2, egg.getProcessKey());
            presql.setString(3, egg.getCreatorRegion());
            presql.setString(4, egg.getProcessTitle());
            presql.setString(5, egg.getContent());
            presql.setInt(6, egg.getCategorytype());
            presql.setInt(7, egg.getProcessStatus());
            presql.execute();
            presql.close();
        } catch (SQLException e) {
            System.out.println(
                    "pid:" + egg.getProcessId() + " cat:" + egg.getCategorytype() + " pst:" + egg.getProcessStatus());
            e.printStackTrace();
        }
        closeConnect(connect, presql, null);
    }

    public void insertWorry(List<Stream> egg, int pid, String pk) { // 插入数据
        Timestamp tempc, temph;
        Stream instemp;

        if (egg == null)
            return;
        PreparedStatement presql = null;
        Connection connect = getConnect();
        try {
            connect.setAutoCommit(false);
            presql = connect.prepareStatement("insert into c_worry_stream values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate()) "
                    + " ON DUPLICATE KEY UPDATE luckyname=values(luckyname),luckynick=values(luckynick),answer=values(answer)");
            for (int i = 0; i < egg.size(); i++) {
                instemp = egg.get(i);
                tempc = new Timestamp(instemp.getStartdate().getTime());
                temph = new Timestamp(instemp.getHandledate().getTime());
                presql.setInt(1, instemp.getStreamId());
                presql.setInt(2, pid);
                presql.setString(3, pk);
                presql.setInt(4, instemp.getAction());
                presql.setString(5, instemp.getDepartment()[0]);
                presql.setString(6, instemp.getDepartment()[1]);
                presql.setString(7, instemp.getDepartment()[2]);
                presql.setString(8, instemp.getPersonName());
                presql.setString(9, instemp.getPersonNick());
                presql.setString(10, instemp.getLuckyName());
                presql.setString(11, instemp.getLuckyNick());
                presql.setTimestamp(12, tempc);
                presql.setString(13, instemp.getAnswer());
                presql.setTimestamp(14, temph);
                presql.addBatch();
            }
            // System.out.print(presql);
            presql.executeBatch();
            connect.commit();
            presql.clearBatch();
        } catch (SQLException e) {
            // e.printStackTrace();
        }
        closeConnect(connect, presql, null);
    }

    @Override
    public Worry findbyKey(String k) { // 用超键查询工单
        Map jsonObj = new HashMap();
        ArrayList<Stream> st = new ArrayList<Stream>();
        PreparedStatement presql = null;
        ResultSet value = null;
        Connection connect = getConnect();
        try {
            presql = connect.prepareStatement(
                    "select case  when truecategory is not null or truecategory not in (11) then truecategory else categorytype end as categorytype,b.*,a.* from c_worry a,d_worry_item b where a.processKey=? and a.processId=b.processId");
            presql.setString(1, k);
            value = presql.executeQuery();
            if (value == null) {
                return null;
            }
            if (!value.next()) {
                closeConnect(connect, presql, value);
                return null;
            }
            ResultSetMetaData metaData = value.getMetaData();
            String key;
            Object val;//
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                key = metaData.getColumnLabel(i);
                val = value.getObject(key);
                jsonObj.put(key, val);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        closeConnect(connect, presql, value);
        Worry work = null;
        try {
            work = WorryFactory.GetWorryFromMap(jsonObj);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        work.setStream(findStream(work.getProcessId()));
        return work;
    }

    @Override
    public Worry findbyId(int Id) { // 主键查询数据
        Map<String, Object> mapObj = new HashMap();
        PreparedStatement presql = null;
        ResultSet value = null;
        Connection connect = getConnect();
        try {
            presql = connect.prepareStatement(
                    "select case when truecategory is not null and truecategory not in (11) then truecategory else categorytype end as categorytype,b.*,a.* from c_worry a,d_worry_item b where a.processId=? and a.processId=b.processId order by a.processId");
            presql.setInt(1, Id);
            value = presql.executeQuery();
            if (!value.next()) {
                closeConnect(connect, presql, value);
                return null;
            }
            ResultSetMetaData metaData = value.getMetaData();
            String key;
            Object val; //
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                key = metaData.getColumnLabel(i);
                val = value.getObject(key);
                if (val == null)
                    val = "";
                mapObj.put(key, val);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnect(connect, presql, value);
        }
        Worry work = null;
        try {
            work = WorryFactory.GetWorryFromMap(mapObj);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        work.setStream(findStream(Id));
        return work;
    }

    @Override
    public List<Remark> findRemarks(int processid, Integer type) throws SQLException {
        final StringBuilder sql = new StringBuilder("select * from d_worry_remarks where processid=?");
        if (type != null) {
            sql.append(" and remarkType=?");
            return find(sql.toString(), new Integer[]{processid, type}).stream().map(WorryFactory::createRemark).collect(Collectors.toList());
        }
        return find(sql.toString(), new Integer[]{processid}).stream().map(WorryFactory::createRemark).collect(Collectors.toList());
    }


    public List<Stream> findStream(Integer processId) {
        ArrayList<Stream> st = new ArrayList<Stream>();
        PreparedStatement presql = null;
        Connection connect = getConnect();
        ResultSet value = null;
        try {
            presql = connect.prepareStatement("select c_worry_stream.*,s_category.categoryname handlenick from c_worry_stream,s_category where processId=? and categorytype=handlename and categorytag='ht'");
            presql.setInt(1, processId);
            Stream sb;
            value = presql.executeQuery();
            while (value.next()) {
                String[] dep = new String[3];
                dep[0] = value.getString("department1");
                dep[1] = value.getString("department2");
                dep[2] = value.getString("department3");
                sb = new Stream(value.getInt("streamId"), value.getString("username"), value.getString("usernick"),
                        value.getString("luckyname"), value.getString("luckynick"), value.getInt("handlename"),
                        value.getString("answer"), dep, new Date(value.getTimestamp("start_date").getTime()),
                        new Date(value.getTimestamp("end_date").getTime()));
                sb.setActionName(value.getString("handlenick"));
                st.add(sb);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnect(connect, presql, value);
        }

        return st;
    }

    @Override
    public List<Worry> findWorryList(List<Integer> Ids) { // 批量查询数据
        Map jsonObj = new HashMap();
        Map<Integer, Worry> worryMap = new HashMap<>();
        PreparedStatement presql = null;
        ResultSet value = null;
        if (Ids == null || Ids.isEmpty())
            return null;
        String sql = "select case when truecategory is not null and truecategory not in (15,11) then truecategory else b.categorytype end categorytype,b.superCategory,b.processStatus,a.* from c_worry a,d_worry_item b ";
        String pids = "where a.processId in (?";
        StringBuffer sbsql = new StringBuffer(pids);
        Connection connect = getConnect();
        List<Worry> worryall = new ArrayList<Worry>();
        Ids.stream().limit(Ids.size() - 1).forEach(x -> sbsql.append(",?"));
        sbsql.append(")");
        sql = sql + sbsql + "and a.processId=b.processId order by a.processId";
        try {
            presql = connect.prepareStatement(sql); //注意，不要在循环中使用查询操作
            for (int j = 0; j < Ids.size(); j++) {
                presql.setInt(j + 1, (int) Ids.get(j));
            }
            presql.executeQuery();
            value = presql.executeQuery();
            if (value == null) {
                return null;
            }
            while (value.next()) {
                ResultSetMetaData metaData = value.getMetaData();
                String key;
                Object val; //
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    key = metaData.getColumnLabel(i);
                    val = value.getObject(key);
                    if (val == null)
                        jsonObj.put(key, "");
                    else
                        jsonObj.put(key, val);
                }
                Worry work = WorryFactory.GetWorryFromMap(jsonObj);
                worryMap.put(work.getProcessId(), work);  //使用map来拼接数据

            }
            presql = connect.prepareStatement("select a.*,s_category.categorytype handlenick from c_worry_stream a,s_category " + sbsql + " and categorytype=handlename and categorytag='ht'");
            for (int j = 0; j < Ids.size(); j++) {
                presql.setInt(j + 1, (int) Ids.get(j));
            }
            Stream sb;
            value = presql.executeQuery();
            String[] dep = new String[3];
            int processId = 0;
            ArrayList<Stream> st = new ArrayList<Stream>();
            while (value.next()) {
                dep[0] = value.getString("department1");
                dep[1] = value.getString("department2");
                dep[2] = value.getString("department3");
                sb = new Stream(value.getInt("streamId"), value.getString("username"), value.getString("usernick"),
                        value.getString("luckyname"), value.getString("luckynick"), value.getInt("handlename"),
                        value.getString("answer"), dep, new Date(value.getTimestamp("start_date").getTime()),
                        new Date(value.getTimestamp("start_date").getTime()));
                sb.setActionName(value.getString("handlenick"));
                if (processId != value.getInt("processId")) {
                    Worry w = worryMap.get(processId);
                    if (w == null) {
                        processId = value.getInt("processId");
                        st = new ArrayList<>();
                        ;
                        continue;
                    }
                    w.setStream(st);
                    worryMap.put(processId, w);
                    processId = value.getInt("processId");
                    st = new ArrayList<>();
                }

                st.add(sb);

            }
            Ids.forEach(p -> {
                worryall.add(worryMap.get(p));
            });
//            worryall.addAll(worryMap.values());
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }
        closeConnect(connect, presql, value);
        return worryall;
    }

    @Override
    public void insertItemLog(List<WorryItem> worrys, String ip, String person) throws SQLException {
        String issertsql = "insert into h_datachangedlog (tableName,columnName,keyValue,ip,personName,changedValue,lastTime) values(?,?,?,?,?,?,sysdate());";

        List<Object[]> args = new ArrayList(2);
        worrys.forEach(w -> {
            List<List> colsval = w.findItemValue();
            List arg = new ArrayList();
            arg.add("d_worry_item");
            arg.add(colsval.get(0).toString());
            arg.add(w.getProcessId());
            arg.add(ip);
            arg.add(person);
            arg.add(colsval.get(1).toString());
            args.add(arg.toArray());
        });
        excuteSql(issertsql, args);
    }

    @Override
    public void commentWorry(int processId, String remark, String username, int remarktype) throws SQLException {
        final String sql = "insert into d_worry_remarks (processId,processKey,username,remarkType,remark,approveNum,lastTime)" +
                "values(?,(select processKey from c_worry where processid=?),?,?,?,0,now())";
        excuteSqlone(sql, Arrays.asList(processId, processId, username, remarktype, remark));
    }

    @Override
    public void approveRemark(int remarkId) throws SQLException {
        final String sql = "update set approveNum=approveNum+1 where remarkId = ?";
        excuteSqlone(sql, Arrays.asList(remarkId));
    }

    @Override
    public void setworryItem(List<WorryItem> worrys) throws SQLException {
        final String sql = "update d_worry_item set ";
        StringBuffer sqlsb = new StringBuffer(sql);
        PreparedStatement presql = null;
        Connection connect = getConnect();
        connect.setAutoCommit(false);
        for (WorryItem w : worrys) {
            List<List> al = w.findItemValue();
            for (int i = 0; i < al.get(0).size() - 1; i++) {
                sqlsb.append(al.get(0).get(i) + "=?,");
            }
            if (al.get(0).size() > 0)
                sqlsb.append(al.get(0).get(al.get(0).size() - 1)).append("=? where processId=?");
            al.get(1).add(w.getProcessId());
            presql = connect.prepareStatement(sqlsb.toString());
            sqlsb = new StringBuffer(sql);
            for (int i = 0; i < al.get(1).size(); i++) {
                presql.setObject(i + 1, al.get(1).get(i));
            }
            presql.executeUpdate();
        }
        connect.commit();
        closeConnect(connect, presql, null);
    }

    @Override
    public List<Worry> findWorryBySql(String sql, Object val[]) throws SQLException {
        List<Map<String, Object>> list = find(sql, val);
        List<Worry> worrylist = new ArrayList();
        if (list != null) {
            list.forEach(m -> {
                try {
                    worrylist.add(WorryFactory.GetWorryFromMap(m));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return worrylist;
    }

    @Override
    public List<Worry> findStreamBySql(String sql, Object val[]) throws SQLException {
        List<Map<String, Object>> list = find(sql, val);
        List<Worry> worrylist = new ArrayList();
        int processid = 0;
        List<Stream> streams = new ArrayList<>();
        Worry worry = new Worry();
        if (list != null) {
            for (Map m : list) {
                try {
                    if (!m.get("processId").equals(processid)) {
                        processid = (int) m.get("processId");
                        if (!streams.isEmpty()) {
                            worry.setStream(streams.stream().sorted(Comparator.comparing(Stream::getStreamId)).collect(Collectors.toList()));
                            worrylist.add(worry);
                        }
                        worry = WorryFactory.GetWorryFromMap(m);
                        streams = new ArrayList<>();
                    }
                    String[] dep = new String[3];
                    dep[0] = m.get("department1").toString();
                    dep[1] = m.get("department2").toString();
                    dep[2] = m.get("department3").toString();
                    Stream sb = new Stream(Integer.parseInt(m.get("streamId").toString()), (String) m.get("username"), (String) m.get("usernick"),
                            (String) m.get("luckyname"), (String) m.get("luckynick"), (int) m.get("handlename"),
                            (String) m.get("answer"), dep, (Date) m.get("start_date"),
                            (Date) m.get("end_date"));
                    sb.setProcessId(Integer.parseInt(m.get("processId").toString()));
                    sb.setActionName((String) m.get("actionName"));
                    streams.add(sb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return worrylist;
    }

    /**
     * 原子数据库查询操作，尽量在此基础上开发新的查询
     * @param sql
     * @param val
     * @param clz
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> find(String sql, Object val[], Class<T> clz) throws SQLException {
        List<Map<String, Object>> list = find(sql, val);
        List<T> datalist = new ArrayList();
        if (list != null) {
            list.forEach(m -> {
                try {
                    datalist.add(WorryFactory.univerCreateFromMap(clz, m));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return datalist;
    }

    /**
     * 原子数据库查询操作，尽量在此基础上开发新的查询
     * @param sql 可以执行的sql语句
     * @param val sql语句中的变量
     * @return
     * @throws SQLException
     */
    @Override
    public List<Map<String, Object>> find(String sql, Object[] val) throws SQLException { // 通用查询方法
        return database.find(sql, val, dbname);
    }

    @Override
    public List<Map<String, Object>> getCategoryinfo(String type) {
        String sql = "select categorytype cat,categoryname catname,categorytag tag from s_category ";
        List<String> val = new ArrayList();
        try {
            if (type != null && !"".equals(type)) {
                sql += "where categorytag=?";
                val.add(type);
            }
            return find(sql, val.toArray());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getCategoryinfo(List<String> type) {
        String sql = "select categorytype as cat,categoryname as catname,categorytag as keytag from s_category where categorytag in (";
        StringBuffer sqlsb = new StringBuffer(sql);
        type.forEach(s -> sqlsb.append("?,"));
        sqlsb.setCharAt(sqlsb.length() - 1, ')');
        List<Map<String, Object>> value = null;
        try {
            value = find(sqlsb.toString(), type.stream().toArray());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return value;
    }

    @Override
    public void excuteSql(String sql, List<Object[]> val) throws SQLException { // 通用执行原子方法，尽量在该方法的基础上写sql
        PreparedStatement presql = null;
        Connection connect = getConnect();
        connect.setAutoCommit(false);
        presql = connect.prepareStatement(sql);
        if (val != null) {
            for (int i = 0; i < val.size(); i++) {
                Object[] temp = val.get(i);
                for (int j = 0; j < temp.length; j++)
                    presql.setObject(1 + j, temp[j]);
                presql.addBatch();
            }
        }
        presql.executeBatch();
        connect.commit();
        closeConnect(connect, presql, null);
    }

    @Override
    public void excuteSqlone(String sql, List<Object> val) throws SQLException { // 通用执行方法
        PreparedStatement presql = null;
        Connection connect = getConnect();
        presql = connect.prepareStatement(sql);
        // System.out.print(presql.toString());
        for (int j = 0; j < val.size(); j++)
            presql.setObject(1 + j, val.get(j));

        presql.execute();
        closeConnect(connect, presql, null);
    }

    /**
     * 原子插入方法 tag==1时为单条插入
     * @param data
     * @param sql
     * @param tag
     * @throws SQLException
     */
    void insert(List data, String sql, int tag) throws SQLException { // 通用插入方法
        PreparedStatement presql = null;
        Connection connect = getConnect();
        presql = connect.prepareStatement(sql);
        if (tag == 1) {
            for (int i = 0; i < data.size(); i++)
                presql.setObject(i + 1, data.get(i));
            presql.execute();
        } else {

            connect.setAutoCommit(false);
            for (int i = 0; i < data.size(); i++) {
                presql.setObject(1, data.get(i));
                presql.addBatch();
                if (i % 100 == 0) {
                    presql.executeBatch();
                    connect.commit();
                    presql.clearBatch();
                }
            }
            presql.executeBatch();
            connect.commit();
        }

        closeConnect(connect, presql, null);
    }

}
