package com.unicom.betterworry.dao.Impl;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库连接池 类
 */
@Component
public class DatabasePool {
    public static Map<String,ComboPooledDataSource> datapool=new HashMap<String,ComboPooledDataSource>();
    //static ComboPooledDataSource dataSource = new ComboPooledDataSource("mysql");
    static {
        datapool.put("mysql",new ComboPooledDataSource("mysql"));
        datapool.put("mysqlview",new ComboPooledDataSource("mysqlview"));
        datapool.put("mysqlunicom",new ComboPooledDataSource("mysqlunicom"));
        datapool.put("mysqlinnerunicom",new ComboPooledDataSource("mysqlinnerunicom"));
    }

    public void setNameAndPaw(String name,String paw){

    }
    public void setCofig(String coname){
    }

    /**
     * 数据库连接操作
     * @return
     * @throws Exception
     */
	public Connection getConnect() throws Exception{
	            return datapool.get("root").getConnection();
	    }
    public Connection getConnect(String name) throws Exception{
        return datapool.get(name).getConnection();
    }

    /**
     * 封装好的数据库原子查询
     * @param sql 可执行的sql语句
     * @param val 语句中的变量
     * @param dbname 数据名称
     * @return 数据集合
     * @throws SQLException
     */
    public List<Map<String, Object>> find(String sql, Object[] val,String dbname) throws SQLException { // 通用查询方法
        PreparedStatement presql = null;
        Connection connect = null;
        try {
            connect = getConnect(dbname);
        } catch (Exception e) {
            throw new SQLException(e);
        }
        ResultSet value = null;
        presql = connect.prepareStatement(sql);
        if (val != null) {
            for (int i = 0; i < val.length; i++)
                presql.setObject(1 + i, val[i]);
        }
        value = presql.executeQuery();
        if (value == null)
            return null;
        ResultSetMetaData metaData = value.getMetaData();
        List<Map<String, Object>> res = new ArrayList();
        Map row = null;
        while (value.next()) {
            row = new HashMap();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnLabel(i), value.getObject(i));
            }
            res.add(row);
        }
        try {
            close(connect, presql, value);
        } catch (Exception e) {
            throw new SQLException(e);
        }
        return res;
    }

    /**
     * 数据库连接关闭
     * @param conn
     * @param pst
     * @param rs
     * @throws Exception
     */
	public void close(Connection conn,PreparedStatement pst,ResultSet rs) throws Exception{  
        if(rs!=null){  
            try {  
                rs.close();  
            } catch (SQLException e) {  
                throw new Exception("数据库连接关闭出错!", e);            
            }  
        }  
        if(pst!=null){  
            try {  
                pst.close();  
            } catch (SQLException e) {  
                throw new Exception("数据库连接关闭出错!", e);    
            }  
        }    
        if(conn!=null){  
            try {  
                conn.close();
                //System.out.println("数据库已关闭");
            } catch (SQLException e) {  
                throw new Exception("数据库连接关闭出错!", e);    
            }  
        }
	}
	}