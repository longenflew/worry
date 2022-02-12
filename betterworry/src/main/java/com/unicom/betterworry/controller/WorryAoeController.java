package com.unicom.betterworry.controller;

import com.unicom.betterworry.being.ResultBean;
import com.unicom.betterworry.being.Worry;
import com.unicom.betterworry.being.WorryList;
import com.unicom.betterworry.service.WorryArrService;
import com.unicom.betterworry.service.WorryService;
import com.unicom.betterworry.util.AddrMapUtil;
import com.unicom.betterworry.util.ResultUtils;
import io.swagger.annotations.Api;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.stream.Collectors;
/**
 * 工单批量处理control类
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 * 
 */

@Api(value = "多量工单处理接口")
@Controller
@RequestMapping("/worryarr")
public class WorryAoeController {
    private WorryArrService worrarrsvc;
    private WorryService worrsvc;
    private static final String BAYESIAN = "BAYESIAN";

    @Autowired
    public void setAorryAoesvc(WorryArrService svc) {
        worrarrsvc = svc;
    }

    @Autowired
    public void setWorrsvc(WorryService worrsvc) {
        this.worrsvc = worrsvc;
    }

    /**
     *
     * @param ticket 根据ticket来限制查询范围
     * @param request 传参含有menthod=api时为同步获取直接返回全量数据不会分页，传参含有fixed=on时将生成永久数据
     * @param response
     * @throws Exception
     */
    @Api(value="工单搜索接口")
    @RequestMapping("/aoegetworry")
    public void huntAoeWorry(@RequestParam(required = false) String ticket, HttpServletRequest request, HttpServletResponse response) throws Exception {
        BufferedReader reader = request.getReader();
        StringBuilder builder = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            builder.append(line);
            line = reader.readLine();
        }
        reader.close();
        String reqBody = builder.toString();
        JSONArray restrict;
        if (reqBody.length() == 0) {
            restrict = null;
        } else {
            restrict = JSONArray.fromObject(reqBody);
        }
        List<Map<String, Object>> args = new ArrayList<>();
        String sql = request.getParameter("sql");
        Map<String,Object[]> params = new HashMap(request.getParameterMap());
        String viewType = (String) params.getOrDefault("viewType",new String[]{"worry"})[0];
        params.remove("viewType");
        String menthod= (String) params.getOrDefault("menthod",new String[]{""})[0];
        params.remove("menthod");
        WorryList readyWorrylist = new WorryList();
        StringBuffer ticketsb = new StringBuffer();
        HttpSession session = request.getSession();
        String ip = getRemoteHost(request);
        Boolean fixed = false;
        List<Worry> worrylist;
        if (restrict == null) {
            Map<String,Object> argstemp = new HashMap();
            for (Entry<String, Object[]> temp : params.entrySet()) {
                if ("".equals(temp.getValue()[0])) {
                    continue;
                }
                argstemp.put(temp.getKey(), temp.getValue()[0]);
                ticketsb.append(temp.getKey()).append(temp.getValue()[0]);
            }
            args.add(argstemp);
        } else {
            for (int i = 0; i < restrict.size(); i++) {
                JSONObject temp = restrict.getJSONObject(i);
                Map<String, Object> maptemp = new HashMap<>();
                Iterator<String> it = temp.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    Object val = temp.get(key);
                    if (key.toLowerCase().contains("time")) {
                        String end = ((String) val).substring(12, 23);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        Date date;
                        try {
                            date = formatter.parse(end);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    maptemp.put(key, val);
                    ticketsb.append(key).append(val);
                }
                args.add(maptemp);
            }
        }
        fixed = isNeedFix(args);
        readyWorrylist.setIp(ip);
        readyWorrylist.setFilter(args);
        readyWorrylist.setFixed(fixed);
        readyWorrylist.setViewType(viewType);
        readyWorrylist.setSessionId(session.getId());
        List<Integer> pidlist = null;
        if (ticket == null) {
            if (fixed) {
                ticket = createTicketFixed(ticketsb.toString())+viewType.toLowerCase();
            } else {
                ticket = createTicket(ticketsb.toString());
            }

        } else {   //如果已经存在ticket，就使用ticket的工单
            worrylist = worrarrsvc.getWorryArr(ticket, 0, 0).getValue();
            readyWorrylist.setFatherTicket(ticket);
            ticket = createTicket(ticketsb.toString(), ticket);
            pidlist = new ArrayList();
            for (int i = 0; i < worrylist.size(); i++) {
                Worry temp = worrylist.get(i);
                pidlist.add(temp.getProcessId());
            }
        }
        readyWorrylist.setTicket(ticket);
        session.setAttribute("searchinfo", readyWorrylist);
        session.setAttribute("ticket", ticket);
        if ("".equals(menthod)) {
            response.getWriter().println("{ticket:" + ticket + "}");
            response.sendRedirect("../worrylist.html?ticket=" + ticket+"&limit=0&page=0");
            worrarrsvc.bulidWorryArr(readyWorrylist, pidlist, 0);
        } else if ("api".equals(menthod)) {
            worrarrsvc.bulidWorryArr(readyWorrylist, pidlist, 0);
            response.getWriter().println("{ticket:" + ticket + "}");
            response.sendRedirect("hunterprey?ticket=" + ticket);
        } else if ("syn".equals(menthod)) {
            try {
                response.setContentType("application/json;charset=utf-8");
                OutputStream out = response.getOutputStream();
                //worrarrsvc.bulidWorryArr(in, pidlist, (List<Map<String,Object>>) in.get("filter"), 0); //同步获取
                worrarrsvc.bulidWorryArr(readyWorrylist, pidlist, 0);
                JSONObject output = new JSONObject();
                WorryList d = worrarrsvc.getWorryArr(ticket, 0, 0);
                output.put("data", d.getValue());
                output.put("count", d.getSize());
                output.put("code", 0);
                out.write(output.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if("asyn".equals(menthod)){
            worrarrsvc.bulidWorryArr(readyWorrylist, pidlist, 0);
            response.getWriter().println("{ticket:" + ticket + "}");
            return;
        }
    }

    /**
     *
     * @param data 入参json数据，参数:1.type,类型:list,描述:分析的类型见s_category;2.processlist,类型:list,描述:分析的工单。请求格式{
     *             type:[],
     *             processlist:[],
     *             ticket:""
     * }
     * @param request
     * @return
     */

    @RequestMapping("/conclusion")
    @ResponseBody
    public JSONObject worryarrConclusion(@RequestBody JSONObject data, HttpServletRequest request) {
        String ip = getRemoteHost(request);
        List pidlist = new ArrayList();
        String ticket;
        List type = data.getJSONArray("type");
        if (type == null) {
            return JSONObject.fromObject("{'msg':'type can't be empty','data':[]}");
        }

        List prolist = (List) data.get("processlist");
        if (prolist == null && (request.getParameter("ticket") != null || request.getSession().getAttribute("ticket") != null)) {
            ticket = request.getParameter("ticket");
            WorryList worryval;
            ticket = ticket == null ? (String) request.getSession().getAttribute("ticket") : ticket;
            worryval = worrarrsvc.getWorryArr(ticket, 0, 0);
            if (worryval == null) {
                return JSONObject.fromObject("{'msg':'worrylist is empty','data':[]}");
            }
            worryval.getValue().forEach(s -> pidlist.add(s.getProcessId()));
        } else {
            return JSONObject.fromObject("{'msg':'who are you?'}");
        }
        Integer guessed = worrarrsvc.getWorryitemGuessState(ticket);
        JSONObject output = new JSONObject();
        List<List<Map<String, Object>>> retlist = new ArrayList();
        for (int i = 0; i < type.size(); i++) {
            List<Map<String, Object>> temp;
            try {
                if (type.get(i).toString().matches("[0-9]*"))
                    temp = worrarrsvc.analysisArrwithType(pidlist, (String) type.get(i));
                else {
                    temp = worrarrsvc.guessedWorryanalysis(ticket, type.get(i).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (temp == null)
                continue;
            retlist.add(temp);
        }
        output.put("msg", "success");
        if (!retlist.isEmpty()) {
            if (guessed % 10 == 1) {
                guessed = 21;
            } else if (guessed % 10 > 1) {
                guessed = guessed % 10;
            } else {
                guessed = 0;
            }
            for (int i = 0; i < retlist.size(); i++) {
                for (int j = 0; j < retlist.get(i).size(); j++) {
                    WorryList readyWorryList = new WorryList();
                    readyWorryList.setGuessed(guessed);
                    readyWorryList.setFixed(false);
                    readyWorryList.setIp(ip);
                    readyWorryList.setSessionId(request.getSession().getId());
                    readyWorryList.setGuessed(guessed);
                    Map<String, Object> filter = new HashMap();
                    if (retlist.get(i).get(j).containsKey("guessed")) {
                        filter.put("processId", retlist.get(i).get(j).get("list"));
                    } else {
                        filter.put("VALUE", retlist.get(i).get(j).get("VALUE"));
                        filter.put("type", type.get(i));
                    }
                    retlist.get(i).get(j).remove("list");
                    readyWorryList.setFilter(Arrays.asList(filter));
                    readyWorryList.setFatherTicket(ticket);
                    readyWorryList.setTicket(createTicket(retlist.get(i).get(j).toString(), ticket));
                    retlist.get(i).get(j).put("ticket", readyWorryList.getTicket());
                    if (retlist.get(i).get(j).containsKey("guessed"))
                        worrarrsvc.bulidWorryArr(readyWorryList, pidlist, 0);
                    else
                        worrarrsvc.bulidWorryArr(readyWorryList, pidlist, 1);
                }
            }
        }
        Map respData = new HashMap();
        for (int i = 0; i < retlist.size(); i++) {
            respData.put(type.get(i), retlist.get(i));
        }
        output.put("data", respData);
        output.put("code", 0);
        return output;
    }

    /**
     * 工单导出方法，将{@code WorryList.value}输出为xls的二进制流并返回给前端
     * @param ticket {@code WorryList.ticket}
     * @param all 是否全量导出的表示，但是好像没
     * @param response
     */
    @RequestMapping(value = "worryoutput")
    public void worryoutput(String ticket, boolean all, HttpServletResponse response) {
        List<Worry> worrylist;
        List<Integer> worryidlist = new ArrayList();
        if (ticket != null && all) {
            WorryList temp = worrarrsvc.getWorryArr(ticket, 0, 0);
            if (temp == null) {
                response.setCharacterEncoding("UTF-8");
                try {
                    response.getWriter().println("worrylist is empty!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            worrylist = temp.getValue();
            for (int i = 0; i < worrylist.size(); i++) {
                worryidlist.add(worrylist.get(i).getProcessId());
            }
        }
        byte[] data = null;
        data = worrarrsvc.worryarroutfile(worryidlist);
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=worrylist" + System.currentTimeMillis() + ".xls");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream;charset=UTF-8");
        OutputStream outputStream;
        try {
            outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param ticket {@code Worrylist.ticket} 
     * @param request 参数{@code page}:页数;{@code limit}:每页的数量
     * @param response
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping("/hunterprey")
    public void getWorryArr(@RequestParam String ticket, HttpServletRequest request, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {
        JSONObject msg = new JSONObject();
        int page = 1, pagenum = 10;
        if (request.getSession().getAttribute("ticket") == null && request.getParameter("ticket") == null) {
            msg.put("code", 0);
            msg.put("data", "");
            OutputStream out = response.getOutputStream();
            out.write(msg.toString().getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }

        if (request.getParameter("limit") != null) {
            pagenum = Integer.parseInt(request.getParameter("limit"));
        }
        request.getSession().setAttribute("ticket", ticket);
        WorryList data = worrarrsvc.getWorryArr(ticket, (page - 1) * pagenum, page * pagenum);
        if (data == null) {
            msg.put("code", 1);
            msg.put("msg", "list is not exist!");
            msg.put("data",new ArrayList());
            OutputStream out = response.getOutputStream();
            out.write(msg.toString().getBytes(StandardCharsets.UTF_8));
            return;
        }
        List<Map<String, Object>> retMap = null;
        if ("worry".equals(data.getViewType())) {
            retMap = data.getValue().stream().map(m -> {
                Map<String, Object> d = new HashMap();
                d.put("processId", m.getProcessId());
                d.put("processKey", m.getProcessKey());
                d.put("processTitle", m.getProcessTitle());
                d.put("worryItem", m.getWorryItem());
                return d;
            }).collect(Collectors.toList());
        } else if ("stream".equals(data.getViewType())) {
            retMap = data.getValue().stream().map(m -> {
                Map<String, Object> d = new HashMap();
                d.put("processId", m.getProcessId());
                d.put("processKey", m.getProcessKey());
                d.put("processTitle", m.getProcessTitle());
                d.put("stream", m.getStream());
                d.put("worryItem", m.getWorryItem());
                return d;
            }).collect(Collectors.toList());

        }
        msg.put("data", retMap);
        msg.put("count", data.getSize());
        msg.put("page", page);
        msg.put("limit", pagenum);
        msg.put("fixed", data.isFixed());
        msg.put("filter", data.getFilter());
        msg.put("guessed", data.getGuessed());
        msg.put("viewType",data.getViewType());
        msg.put("fatherTicket",data.getFatherTicket());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        OutputStream out = response.getOutputStream();
        out.write(msg.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 设置某条工单是否可以访问
     * @param ticket
     * @param fixed
     */
    @GetMapping("/setaccess")
    @ResponseBody
    public void setWorryArrAccess(@RequestParam String ticket, @RequestParam boolean fixed) {
        worrarrsvc.setWorryArrAccess(ticket, fixed);
    }

    /**
     * 猜想一个工单列表接口
     * @param ticket
     * @return 返回是否能被处理的信息
     */
    @RequestMapping("/aoeguessworryitem")
    @ResponseBody
    public ResultBean guessWorryitemAoe(@RequestParam  String ticket) {

        if (AddrMapUtil.containsKey(BAYESIAN , ticket))
            return getWorryitemGuessState(ticket);
        try {
            Map<String, Object> state = new HashMap();
            state.put("sum", 0);
            state.put("now", 0);
            state.put("ticket", ticket);
            AddrMapUtil.put(BAYESIAN ,ticket, state);
            worrsvc.guessWorryCategoryBayesian(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(e.getMessage());
        }
        return getWorryitemGuessState(ticket);
    }

    /**
     * 获取某个worryList的猜想状态
     * @param ticket
     * @return
     */
    @RequestMapping("/getworryitemguess_state")
    @ResponseBody
    public ResultBean getWorryitemGuessState(String ticket) {
        Map in = new HashMap();
        in.put("ticket", ticket);
        if (AddrMapUtil.containsKey(BAYESIAN ,ticket)) {
            Map state = new HashMap((Map) AddrMapUtil.get(BAYESIAN , ticket));
            in.put("finished", false);
            in.putAll(state);
            if (state.containsKey("lock")) {
                Set<String> sonticket = worrarrsvc.getWorryGuessedSonTicket(ticket);
                in.put("guessingSonTicket", sonticket);
                in.put("lock",true);
            }
            return ResultUtils.success(in);
        } else {
            Integer guessed = worrarrsvc.getWorryitemGuessState(ticket);
            if (guessed == null) {
                return ResultUtils.success(in);
            }
            if (guessed == 11) {
                Set<String> sonticket = worrarrsvc.getWorryGuessedSonTicket(ticket);
                if (sonticket != null && !sonticket.isEmpty()) {
                    in.put("guessingSonTicket", sonticket);
                    in.put("finished", false);
                    return ResultUtils.success(in);
                }
                return ResultUtils.error("worrylist guessed state is invalid!");
            } else if (guessed >= 2 && guessed < 10) {
                in.put("finished", true);
                return ResultUtils.success(in);
            } else if (guessed == 1)
                return ResultUtils.error("guess process is terminated!");
            else if (guessed == 21) {
                in.put("finished", false);
                in.put("guessingFatherTicket", worrarrsvc.getWorryGuessedFatherTicket(ticket));
                return ResultUtils.success(in);
            }
            in.put("finished", false);
            return ResultUtils.success(in);
        }
    }

    /**
     * 保存猜想完成后的工单worrylist
     * @param ticket
     * @return
     */
    @RequestMapping("/saveguessworryitem")
    @ResponseBody
    public ResultBean saveGuessedWorryCategory(String ticket) {
        try {
            worrsvc.saveGuessedWorryCategory(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(e.getMessage());
        }
        return ResultUtils.success();
    }

    String createTicket(String in) {
        in += Long.toString(System.currentTimeMillis());
        int[] key = new int[32];
        int j = 0;
        if (in.length() == 0)
            in = "a";
        while (in.length() < 32) {
            in = in + in;
        }

        for (int k = 0; k < in.length(); k++) { // 设置ticket
            key[j % 32] = key[j % 32] + in.charAt(k) + 1;
            j++;
        }
        StringBuffer ticket = new StringBuffer();
        for (int i = 0; i < key.length; i++) {
            ticket.append((char) (key[i] % (122 - 97) + 97));
        }
        return ticket.toString();
    }

    String createTicketFixed(String in) {
        int[] key = new int[32];
        int j = 0;
        if (in.length() == 0)
            in = "a";
        while (in.length() < 32) {
            in = in + in;
        }

        for (int k = 0; k < in.length(); k++) { // 设置ticket
            key[j % 32] = key[j % 32] + in.charAt(k) + 1;
            j++;
        }
        StringBuffer ticket = new StringBuffer();
        for (int i = 0; i < key.length; i++) {
            ticket.append((char) (key[i] % (122 - 97) + 97));
        }
        return ticket.toString();
    }

    String createTicket(String in, String in2) {
        return createTicket(in) + createTicket(in2);
    }

    public boolean isNeedFix(List<Map<String, Object>> args) throws Exception {
        if (args == null || args.isEmpty())
            return false;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        int[] num = {0}, err = {0};
        if (args.stream().filter(s -> {
            if (s.isEmpty())
                return false;
            List<Entry<String, Object>> temp = new ArrayList(s.entrySet());
            if (temp.get(0).getKey().toLowerCase().contains("time")) {
                num[0]++;
                try {
                    String end=temp.get(0).getValue().toString();
                    if (formatter.parse(end.substring(end.indexOf("20",10), 23)).getTime() < System.currentTimeMillis()) {
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    err[0]++;
                }
            }
            return false;
        }).count() == num[0] && num[0] > 0 && err[0] == 0)
            return true;
        else if (err[0] > 0)
            throw (new Exception("日期格式不对"));
        return false;
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
