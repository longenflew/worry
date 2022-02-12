package com.unicom.betterworry.service.Impl;


import com.unicom.betterworry.dao.WorryMapper;
import com.unicom.betterworry.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	WorryMapper egg;
	public List<Map<String,Object>> getsupCategoryinfo(String type)  {
		return egg.getCategoryinfo(type);
	}


	public List<Map<String, Object>> getsupCategoryinfo(List<String> type)  {
		return egg.getCategoryinfo(type);
	}
	public List<Object[]> getCategory(String type)  {
		String sql="select categorytype,categoryname from s_category where categorytag=?";
		String[] t=new String[1];
		t[0]=type;
		List<Object[]> res=new ArrayList<Object[]>();

		List<Map<String, Object>> value= null;
		try {
			value = egg.find(sql, t);
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		for(int i=0;i<value.size();i++) {
			Object[] temp=new Object[2];
			temp[0]=value.get(i).get("categorytype");
			temp[1]=value.get(i).get("categoryname");
			res.add(temp);
		}
		return res;
	}
}
