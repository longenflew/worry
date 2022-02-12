package com.unicom.betterworry.controller;

import com.unicom.betterworry.being.ResultBean;
import com.unicom.betterworry.being.Word;
import com.unicom.betterworry.nexus.WordKnife;
import com.unicom.betterworry.util.ResultUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/ccss")
public class CategoryClassifyController {
    public ResultBean<List<Word>> segContent(String text, String type) {
        if (type.equals("purely"))
            return ResultUtils.success(WordKnife.segWord(text));
        else if (type.equals(""))
            return ResultUtils.success(WordKnife.segWord(text));
        return ResultUtils.error("类型不可控");
    }
}
