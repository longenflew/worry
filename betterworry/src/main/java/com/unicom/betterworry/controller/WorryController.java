package com.unicom.betterworry.controller;


import com.unicom.betterworry.being.*;
import com.unicom.betterworry.service.Impl.WorryServiceImpl;
import com.unicom.betterworry.util.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工单接口
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 */
@Api(value = "工单细节处理接口")
@Controller
@RequestMapping("/worrysingle")
public class WorryController {
    @Autowired
    WorryServiceImpl worrysvc;
    static Map<String,String> paramNameMap=new HashMap();
    static {
        paramNameMap.put("category","category");
        paramNameMap.put("supercategory","supercategory");
        paramNameMap.put("worryremark","remark");
        paramNameMap.put("beautyTag","beautyTag");
        paramNameMap.put("beauty","beauty");
        paramNameMap.put("location","location");
        paramNameMap.put("menu","menu");
    }

    /*
    工单猎手接口，可以根据processId获取工单详细详细
     */
    @ApiOperation(value = "工单详细信息接口", notes = "通过工单processId获取工单详细信息")
    @RequestMapping("/worryhunter")
    @ResponseBody
    public JSONObject getmeat(@RequestParam(value = "processId") String id) {
        String inId, inKey;
        inId = id;
        inKey = id;
        Worry worry = null;
        JSONObject objout = new JSONObject();
        Map<Integer, String> person = new HashMap();
        try {
            worry = worrysvc.worryFind(Integer.parseInt(inId));
            JSONObject obj = JSONObject.fromObject(worry);
            obj.remove("maerts");
            obj.remove("streamMap");
            List<JSONObject> strstream = new ArrayList();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            obj.put("createTime", sdf.format(worry.getCreateTime()));
            for (int i = 0; i < worry.getStream().size(); i++) {
                Stream st = worry.getStream().get(i);
                JSONObject stmp = new JSONObject();
                stmp.put("department", st.getDepartments());
                stmp.put("usernick", st.getPersonNick());
                stmp.put("handle", st.getHandle());
                stmp.put("answer", st.getAnswer());
                strstream.add(stmp);
            }
            obj.put("stream", strstream);
            obj.put("log", worrysvc.findchangedPerson(Integer.parseInt(id)));
            objout.put("data", obj);
        } catch (NumberFormatException e) {
            try {
                e.printStackTrace();
                worry = worrysvc.worryFind(inKey);
                JSONObject obj = JSONObject.fromObject(worry);
                obj.remove("maerts");
                obj.remove("streamMap");
                objout.put("data", obj);

            } catch (NumberFormatException d) {
                objout.put("msg", d.getMessage());
            }
        }
        return objout;
    }

    /**
     * 猜想提供的工单
     * @param procesIdList
     * @return
     * @throws SQLException
     */
    @PostMapping("/guessworryitem")
    @ResponseBody
    public JSONObject guessWorrycategory(@RequestBody List<Integer> procesIdList) throws SQLException {
        JSONObject obj=new JSONObject();
        obj.put("data",worrysvc.guessWorryCategoryBayesian(procesIdList));
        return obj;
    }

    /**
     * 查询工单评论
     * @param id processId
     * @param type 评论类型
     * @return
     */
    @GetMapping("/watchremark")
    @ResponseBody
    public JSONObject watchRemark(@RequestParam(value = "processId") Integer id,@RequestParam(required = false)Integer type) {
        JSONObject objout = new JSONObject();
        List<Remark> remarks=worrysvc.getWorryRemark(id,type);
        objout.put("data",remarks);
        JSONArray arr=objout.getJSONArray("data");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(int i=0;i<arr.size();i++){
            arr.getJSONObject(i).put("lastTime",sdf.format(remarks.get(i).getLastTime()));
        }
        objout.put("code","0");
        return objout;
    }

    /**
     * 工单评论接口
     * @param remark 评论内容
     * @param processId
     * @param username
     * @param remarkType
     * @return
     */
    @PostMapping("/comment")
    @ResponseBody
    public JSONObject commentWorry (String remark,Integer processId,@RequestParam(defaultValue="sb")String username,Integer remarkType){
        if(remarkType<=30){
            JSONObject obj=new JSONObject();
            obj.put("msg","remark类型不可控");
            obj.put("code","1");
            return obj;
        }
        try {
            worrysvc.commentWorry(processId,remark,username,remarkType);
            JSONObject obj=new JSONObject();
            obj.put("msg","成功");
            obj.put("code","0");
            return obj;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            JSONObject obj=new JSONObject();
            obj.put("msg",throwables);
            obj.put("code","-1");
            return obj;
        }
    }

    /**
     * 评论点赞接口
     * @param remarkId
     */
    @PostMapping("/approveremark")
    @ResponseBody
    public void approveRemark (Integer remarkId){
        try {
            worrysvc.approveRemark(remarkId);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    /**
     * 内容分词接口，可以根据上传文本进行工单关键词分词
     * @param content json传说文本 {content:""}
     * @param pId 暂时未用到
     * @return 返回词语集合
     */
    @RequestMapping("/worryShit")
    @ResponseBody
    public ResultBean findWorryShit(@RequestBody JSONObject content, @RequestParam(value = "processId", required = false, defaultValue = "-1") int pId) {
        if (pId < 0 && content == null) {
            return null;
        }
        String text = (content == null || content.isEmpty()) ? worrysvc.worryFind(pId).
                getContent() : content.getString("content");
        return ResultUtils.success(worrysvc.findspWordinContent(text));
    }

    /**
     *
     * @param data
     * @return
     */
    @RequestMapping("/shitcook")
    @ResponseBody
    public String setWorryShit(@RequestBody Map data) {
        //判断是否所需要的字段，没有则直接返回
        if (!new HashSet<>(Arrays.asList("processId", "contentWord", "streamWord")).containsAll(data.keySet()))
        {
            return "shit!";
        }
        worrysvc.saveWorryCharacteristic((int) data.get("processId"), (List<Map>) data.get("contentWord"), (List<Map>) data.get("streamWord"),null);
        return "good!";
    }
    /**
     *
     * @param data 工单属性提交接口，提交的参数为 {@code List<WorryItem>}
     *           {
     *            data:[{}
     *            ]
     *           }
     *
     * @param person 操作账号
     * @param request
     * @return
     */
    @RequestMapping("/worrycook")
    @ResponseBody
    public String setWorryItem(@RequestBody JSONObject data, @RequestParam(defaultValue="sb")String person,HttpServletRequest request) {
        JSONArray itemlist = data.getJSONArray("data");
        String ip= getRemoteHost(request);
        List<WorryItem> worryList=new ArrayList();

        for (int i = 0; i < itemlist.size(); i++) {
            WorryItem worry;
            Map<String,Object> temp = itemlist.getJSONObject(i);
            if(temp.size() <=1) {
                continue;
            }
            worry= WorryFactory.GetWorryItemFromMap(temp);
            if(worry==null) {
                continue;
            }
            worryList.add(worry);
        }
        try {
            worrysvc.setworryItem(worryList,ip,person);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "hao";
    }


    /**
     * metamon 意思是百变怪 该接口可找出相似工单
     * 从Worrylist中找出与processId相识的工单
     * @param processId worry.processId
     * @param ticket worrylist.ticket
     * @param menthod 当method 的值为sync时变为同步，将获得相似工单以及相似度
     * @param response
     * @throws IOException
     */
    @RequestMapping("/metamon")
    @ResponseBody
    public void findSimilarWorry(@RequestParam String processId,@RequestParam(required = false) String ticket, @RequestParam(required = false, defaultValue = "web") String menthod, HttpServletResponse response) throws IOException {
        StringBuffer data = new StringBuffer();
        if (menthod.equals("sync")) {
            response.setContentType("application/json;charset=utf-8");
            JSONObject out = new JSONObject();
            out.put("data", worrysvc.findSimialrWorry(Integer.parseInt(processId),ticket));
            response.getWriter().write(out.toString());
            return;
        } else if (menthod.equals("web")) {
            worrysvc.findSimialrWorry(Integer.parseInt(processId),ticket).forEach(s ->s.keySet().stream().forEach(k->data.append(k + ",")));
            data.setCharAt(data.length() - 1, '\0');
            response.sendRedirect("../worryarr/aoegetworry?processId=" + data);
            return;
        }

    }

    /**
     * 获取工单处理人，orgId为组织Id
     * @param processId worry.processId
     * @param orgId 组织Id在userorg表可查询到
     * @return
     */
    @GetMapping("/getlucky")
    @ResponseBody
    public Map getWorryLucky(int processId,String orgId){
        Map data = new HashMap();
        data.put("streamlocation",worrysvc.getWorrySolvePerson(processId,orgId));
        data.put("lucky",((Map)data.get("streamlocation")).values().stream().collect(Collectors.toSet()));
        return data;
    }

    public String getRemoteHost(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
}
