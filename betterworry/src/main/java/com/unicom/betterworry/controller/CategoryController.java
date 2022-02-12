package com.unicom.betterworry.controller;

import com.unicom.betterworry.being.ResultBean;
import com.unicom.betterworry.service.CategoryService;
import com.unicom.betterworry.util.ResultUtils;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "基本参数查询")
@Controller
public class CategoryController {
	
	@Autowired
	CategoryService catsvc;
    public CategoryController() {
    	
    }
    @RequestMapping("/catchsupcat")
	@ResponseBody
    public ResultBean getCatInfo(String type) {
    	return ResultUtils.success(catsvc.getsupCategoryinfo(type));
    }
}
