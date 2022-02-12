package com.unicom.betterworry.service.Impl;

import com.unicom.betterworry.being.*;
import com.unicom.betterworry.dao.WorryMapper;
import com.unicom.betterworry.dao.WorryMongo;
import com.unicom.betterworry.nexus.CategoryClassify;
import com.unicom.betterworry.nexus.WordKnife;
import com.unicom.betterworry.service.WorryService;
import com.unicom.betterworry.util.AddrMapUtil;
import com.unicom.betterworry.util.Heap;
import com.unicom.betterworry.util.HeapFload;
import com.unicom.betterworry.util.HeapInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:longenflew@hotmail.com">何泳伟</a>
 * 工单服务实现类
 */
@Service
public class WorryServiceImpl implements WorryService {
    @Autowired
    WorryMongo worrymongo;
    WorryMapper egg;
    private final int spwordheapsize = 20;
    private final double likescore = 0.1;
    private static final String BAYESIAN = "BAYESIAN";
    CategoryClassify categoryClassify;

    @Autowired
    WorryServiceImpl(WorryMapper egg) {
        this.egg = egg;
    }

    @Autowired
    public void setCategoryClassfiy(CategoryClassify categoryClassify) {
        this.categoryClassify = categoryClassify;
    }

    @Override
    public Worry worryFind(int id) {
        return egg.findbyId(id);
    }

    @Override
    public Worry worryFind(String key) {
        return egg.findbyKey(key);
    }

    @Override
    public Map<Integer, String> getWorrySolvePerson(int id, Set<String> luck) {
        Worry worry = null;
        Map person = new HashMap();
        worry = egg.findbyId(id);
        for (int i = 0; i < worry.getStream().size(); i++) {
            Stream st = worry.getStream().get(i);
            if (luck.contains(st.getPersonName()) || luck.contains(st.getPersonNick())) {
                person.put(i, st.getPersonNick());
            }
        }
        return person;
    }

    @Override
    public Map<Integer, String> getWorrySolvePerson(int id, String orgId) {
        Worry worry = null;

        Set<String> luck = null;
        try {
            luck = egg.find("select username from s_userorg where orgid=?", new String[]{orgId}).stream().map(s -> (String) s.get("username")).collect(Collectors.toSet());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        Map person = new HashMap(8);
        worry = egg.findbyId(id);
        for (int i = 0; i < worry.getStream().size(); i++) {
            Stream st = worry.getStream().get(i);
            if (luck.contains(st.getPersonName()) || luck.contains(st.getPersonNick())) {
                person.put(i, st.getPersonNick());
            }
        }
        return person;
    }

    @Override
    public List<Map> findspWordinContent(final String content) {
        if (content == null) {
            return null;
        }
        List<Map> wordlist = new ArrayList<>();
        Map<Integer, String> wordindex = new HashMap<>();
        List words = WordKnife.segWordWithLocation(content);


        return words;
    }

    @Override
    public void saveWorryCharacteristic(List<Integer> pids) throws SQLException {
        if (pids.isEmpty()) {
            return;
        }
        StringBuffer sqlfindlist = new StringBuffer("select processId,processTitle,content from c_worry where processId in(");
        pids.forEach(p -> sqlfindlist.append("?,"));
        List<Map<String, Object>> worryContent = egg.find(sqlfindlist.deleteCharAt(sqlfindlist.length() - 1).append(")").toString(), pids.toArray());
        if (worryContent.isEmpty()) {
            return;
        }
        worryContent.forEach(s -> {
            String str = s.get("processTitle") + " " + s.get("content");
            List<Map> lw = WordKnife.segWordPurely(str).stream().map(word -> {
                Map map = new HashMap();
                map.put("text", word.getText());
                map.put("frequency", word.getFrequency());
                return map;
            }).collect(Collectors.toList());
            saveWorryCharacteristic((int) s.get("processId"), lw, null, null);
        });
    }

    @Override
    public List<Map> findSimialrWorry(int id, String ticket) {
        Map arg = new HashMap();
        arg.put("processId", id);
        List<WorryItem> twins;
        if (ticket != null) {
            List<Worry> temp = worrymongo.findWorryList(ticket).getValue();
            List<Integer> pids = temp.stream().map(Worry::getProcessId).collect(Collectors.toList());
            if (pids.isEmpty()) {
                return new ArrayList();
            } else if (pids.size() < 10000) {
                List outdworry = worrymongo.findNotIn("processId", pids, "worryWord", Arrays.asList("processId"));
                try {
                    saveWorryCharacteristic((List<Integer>) outdworry);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            twins = worrymongo.findWorryAllWord(id, pids);
        } else {
            twins = worrymongo.findWorryAllWord(id, (String) null);
        }
        if (twins == null) {
            return new ArrayList();
        }
        List<WorryItem> t = worrymongo.findWorryItem(id);
        if (t.isEmpty()) {
            return new ArrayList();
        }
        WorryItem brother = t.get(0);
        List<Word> contentSPword = brother.getContentSPword();
        int sumf = contentSPword.stream().mapToInt(s -> {
            Integer a = s.getFrequency();
            a = a == null || a == 0 ? 1 : a;
            return a * a;
        }).sum();
        if (sumf == 0) {
            return new ArrayList();
        }
        Map<String, Integer> wordandFrequence = new HashMap<>();
        contentSPword.forEach(s -> wordandFrequence.put(s.getText(), s.getFrequency() == 0 ? 1 : (int) s.getFrequency()));
        Heap<Double, Integer> maxheap = new HeapFload(20, true);
        for (WorryItem baby : twins) {
            double score = 0;
            int sum = 0;
            int sumu = 0;
            if (baby.getContentSPword() != null) {
                for (Word temp : baby.getContentSPword()) {
                    int a = temp.getFrequency();
                    a = a == 0 ? 1 : a;
                    Integer f = wordandFrequence.get(temp.getText());
                    f = f == null ? 0 : f;
                    sum += f * a;
                    sumu += a * a;
                }
            }
            if (sumu + sumf == 0) {
                return new ArrayList();
            }
            score = (double) sum / (Math.sqrt(sumu) * Math.sqrt(sumf));
            maxheap.insert(score, baby.getProcessId());
        }
        List<Map> res = new ArrayList();
        maxheap.getList();
        while (!maxheap.isEmpty()) {
            if (maxheap.findHighest().entrySet().stream().filter(s -> s.getValue() > likescore).count() > 0) {
                res.add(maxheap.deleteMinOrMax());
            } else {
                maxheap.deleteMinOrMax();
            }
        }
        res.forEach(System.out::println);
        return res;
    }

    List<Map> checkOutDoorWorry(List<Integer> pIds) {
        return worrymongo.findNotIn("processId", pIds, "worryWord", Arrays.asList("processId"));
    }

    public void saveWorryCharacteristic(int id, List<Map> contentWord, List<Map> streamWord, List<String> department) {
        Map data = new HashMap();
        Map index = new HashMap();
        index.put("processId", id);
        //Map wdata=new HashMap();
        if (contentWord != null) {
            data.put("contentSPword", contentWord);
        }
        if (streamWord != null) {
            data.put("streamSPword", streamWord);
        }
        if (department != null) {
            data.put("streamDepartments", department);
        }
//        data.put("streamDepartment", WorryFind(id).getStream().stream().
//                map(s -> {
//                    Map<List, Integer> map = new HashMap();
//                    List list = Arrays.asList(s.getDepartment());
//                    map.putIfAbsent(list, 0);
//                    map.put(list, map.get(list) + 1);
//                    return map;
//                }).collect(Collectors.toList()));
        worrymongo.upsertMap(index, data, "worryWord");

    }

    /**
     * 计算相似度 从wrrItList中找出与brother相似的工单
     *
     * @param brother
     * @param wrrItList
     * @return 按相似度从高到底的list
     */
    private List<Map<WorryItem, Double>> calculateSimilarity(WorryItem brother, List<WorryItem> wrrItList) {
        List<Word> contentSPword = brother.getContentSPword();
        int sumf = contentSPword.stream().mapToInt(s -> {
            Integer a = s.getFrequency();
            a = a == null || a == 0 ? 1 : a;
            return a * a;
        }).sum();
        if (sumf == 0) {
            return null;
        }
        Map<String, Integer> wordandFrequence = new HashMap<>();
        contentSPword.forEach(s -> wordandFrequence.put(s.getText(), s.getFrequency() == 0 ? 1 : (int) s.getFrequency()));
        Heap<Double, WorryItem> maxheap = new HeapFload(20, true);
        for (WorryItem baby : wrrItList) {
            double score = 0;
            int sum = 0;
            int sumu = 0;
            if (baby.getContentSPword() != null) {
                for (Word temp : baby.getContentSPword()) {
                    int a = temp.getFrequency();
                    a = a == 0 ? 1 : a;
                    Integer f = wordandFrequence.get(temp.getText());
                    f = f == null ? 0 : f;
                    sum += f * a;
                    sumu += a * a;
                }
            }
            if (sumu + sumf == 0) {
                continue;
            }
            score = (double) sum / (Math.sqrt(sumu) * Math.sqrt(sumf));
            maxheap.insert(score, baby);
        }
        List<Map<WorryItem, Double>> res = new ArrayList();
        maxheap.getList();
        while (!maxheap.isEmpty()) {
            if (maxheap.findHighest().entrySet().stream().filter(s -> s.getValue() > likescore).count() > 0) {
                res.add(maxheap.deleteMinOrMax());
            } else {
                maxheap.deleteMinOrMax();
            }
        }
        res.forEach(System.out::println);
        return res;
    }

    private List<Map<WorryItem, Integer>> calculateDepartment(WorryItem brother, List<WorryItem> wrrItList) {
        if (brother.getStreamDepartments() == null || brother.getStreamDepartments().isEmpty()) {
            return null;
        }
        Heap<Integer, WorryItem> maxheap = new HeapInteger(20, true);
        Set<String> bdSet = new HashSet(brother.getStreamDepartments());
        wrrItList.forEach(w -> {
            if (w.getStreamDepartments() != null) {
                int num = (int) w.getStreamDepartments().stream().filter(bdSet::contains).count();
                maxheap.insert(num, w);
            }
        });

        List<Map<WorryItem, Integer>> res = new ArrayList();
        while (!maxheap.isEmpty()) {
            res.add(maxheap.deleteMinOrMax());
        }
        return res;
    }


    @Override
    public List<Map<String, Object>> findchangedPerson(int id) {
        String sql = "SELECT '分类',substr(changedValue,1,locate(',',changedValue)-1) categoryValue,'高级分类',substr(changedValue,locate(',',changedValue)+1) supCategoryValue,ip,lastTime FROM \n" +
                "h_datachangedlog where tableName='d_worry_item' and keyValue=? order by " +
                "" +
                " desc;";
        try {
            return egg.find(sql, new Integer[]{id});
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void worryinsert() {

    }

    public void getWorryImage(int proId) {

    }

    @Override
    public void saveGuessedWorryCategory(String ticket) throws Exception {
        if (!worrymongo.checkWorryList("ticket", ticket)) {
            throw new Exception("worrylist is not existed!");
        }
        Map arg = new HashMap();
        arg.put("ticket", ticket);
        arg.put("access", true);
        List<Map> rl = worrymongo.findequal(arg, Map.class, "worrylist", Arrays.asList("guessed"), 1);
        Integer guessed = (Integer) rl.get(0).get("guessed");
        if (guessed != 2) {
            throw new Exception("worrylist state is not access!");
        }
        List<WorryItem> wtl = new ArrayList<>();
        List<Map> worrylist = ((List<Map>) worrymongo.findequal(arg, "worrylist"));
        for (Map temp1 : worrylist) {
            List<Map> list1 = (List<Map>) temp1.get("value");
            list1.stream().filter(s -> s.containsKey("worryItem")).forEach(s -> {
                WorryItem wt = (WorryItem) s.get("worryItem");
                wt.setProcessId((Integer) s.get("processId"));
                wtl.add(wt);
            });
            Map in1 = new HashMap();
            in1.put("ticket", ticket);
            in1.put("index", temp1.get("index"));
            in1.put("access", true);
            Map data = new HashMap();
            data.put("guessed", 3);
            worrymongo.updateMap(in1, data, "worrylist");
            setworryItem(wtl, "127.0.0.1", "guessWorryCategoryBayesian");
            wtl.clear();
        }
    }

    void setWorryListGuessState(String ticket, int guessed) {
        Integer guess = worrymongo.getWorryListGuessState(ticket);
        if (guess == null) {
            worrylistrecombination(ticket);
        }
        worrymongo.setWorryListGuessState(ticket, guessed);
    }

    void worrylistrecombination(String ticket) {
        List<WorryList> worrylist = worrymongo.findWorryListDebris(ticket);
        worrymongo.ineffectWorryList(ticket);
        for (WorryList temp : worrylist) {
            temp.setGuessed(0);
            worrymongo.saveData(temp, "worrylist");
        }
    }

    ReadWriteLock findFatherLock(Set<String> fticketSet) {
        ReadWriteLock fatherlock = null;
        for (String t : fticketSet) {
            Map<String, Object> fatherstate = (Map) AddrMapUtil.get(BAYESIAN, t);
            if (fatherstate != null && !fatherstate.isEmpty()) {
                fatherlock = (ReadWriteLock) fatherstate.get("lock");
            }
        }
        return fatherlock;
    }

    @Override
    @Async
    public void guessWorryCategoryBayesian(String ticket) throws Exception {
        AddrMapUtil.putIfAbsent(BAYESIAN, ticket, new HashMap<String, Integer>());
        Map<String, Integer> state = (Map) AddrMapUtil.get(BAYESIAN, ticket);
        state.putIfAbsent("sum", 0);
        state.putIfAbsent("now", 0);
        if (!worrymongo.checkWorryList("ticket", ticket)) {
            AddrMapUtil.remove(BAYESIAN, ticket);
            throw new Exception("worrylist is not existed!");
        }
        ReadWriteLock fatherlock = null;
        Set<String> fticketSet = worrymongo.findWorryListFatherTicket(ticket);
        Set<String> sticketSet = worrymongo.findWorryListSonTicket(ticket);
        try {
            Integer guessed = worrymongo.getWorryListGuessState(ticket);
            if (guessed != null && guessed % 10 == 1) {
                if (guessed == 21) {
                    fatherlock = findFatherLock(fticketSet);
                    fatherlock = fatherlock == null ? new ReentrantReadWriteLock() : fatherlock;
                    for (String s : fticketSet) {
                        Map<String, Object> fatherstate = (Map<String, Object>) AddrMapUtil.get(BAYESIAN, s);
                        if (fatherstate != null && !fatherstate.isEmpty()) {
                            fatherstate.putIfAbsent("lock", fatherlock);
                            fatherstate.putIfAbsent("threadNum", new AtomicInteger());
                            ((AtomicInteger) fatherstate.get("threadNum")).getAndIncrement();
                        }
                    }
                    fatherlock.readLock().lock();
                } else {
                    return;
                }
            }
            AddrMapUtil.put(BAYESIAN, ticket, state);
            List<WorryList> worrylist = worrymongo.findWorryListDebris(ticket);
            state.put("sum", worrylist.get(0).getSize());
            setWorryListGuessState(ticket, 1);
            //设置父列表和子列表的状态
            if (fticketSet != null) {
                fticketSet.forEach(s ->
                        setWorryListGuessState(s, 11)
                );
            }
            if (sticketSet != null) {
                sticketSet.forEach(s -> setWorryListGuessState(s, 21));
            }
            List<WorryItem> worryItemList = new ArrayList();
            //开始分析处理
            for (WorryList temp : worrylist) {
                List<WorryItem> wt = guessWorryCategoryBayesian(temp.getValue().stream().map(Worry::getProcessId).collect(Collectors.toList()), state, ticket);
                worryItemList.addAll(wt);
                List<Worry> worries = temp.getValue();
                for (int i = 0; i < worries.size(); i++) {
                    WorryItem wit = new WorryItem();
                    wit.setCategorytype(wt.get(i).getCategorytype());
                    wit.setSupercategory(wt.get(i).getSupercategory());
                    worries.get(i).setWorryItem(wit);
                }
                temp.setGuessed(1);
                worrymongo.ineffectWorryList(ticket, temp.getIndex());
                worrymongo.saveData(temp, "worrylist");
            }
            Map<Integer, WorryItem> wmitp = worryItemList.stream().collect(Collectors.toMap(WorryItem::getProcessId, s -> s));
            if (fticketSet != null) {
                fticketSet.forEach(s -> {
                    if (AddrMapUtil.containsKey(BAYESIAN, s)) {
                        AtomicInteger tag = (AtomicInteger) ((Map) AddrMapUtil.get(BAYESIAN, s)).get("threadNum");
                        if (tag != null && tag.get() == 0) {
                            setWorryListGuessState(s, 1);
                        }
                    } else {
                        setWorryListGuessState(s, 11);
                    }
                    updateFatherListWorryItem(s, wmitp);
                });
            }
            if (sticketSet != null) {
                sticketSet.forEach(s -> updateSonListWorryItem(s, wmitp));
            }
            worrymongo.setWorryListGuessState(ticket, 2);
        } catch (Exception e) {
            worrymongo.setWorryListGuessState(ticket, 0);
            throw e;
        } finally {
            AddrMapUtil.remove(BAYESIAN, ticket);
            if (fatherlock != null) {
                fatherlock.readLock().unlock();
            }
            for (String s : fticketSet) {
                Map<String, Object> fatherstate = (Map<String, Object>) AddrMapUtil.get(BAYESIAN, s);
                if (fatherstate != null && !fatherstate.isEmpty()) {
                    ((AtomicInteger) fatherstate.get("threadNum")).getAndDecrement();
                }
            }
        }
    }

    WorryList updateWorryListItem(String ticket, Map<Integer, WorryItem> wmitp) {
        WorryList wmp = worrymongo.findWorryList(ticket);
        List<Worry> worrylist = wmp.getValue();
        for (Worry temp : worrylist) {
            int pid = temp.getProcessId();
            if (wmitp.containsKey(pid)) {
                WorryItem wit = new WorryItem();
                wit.setSupercategory(wmitp.get(pid).getSupercategory());
                wit.setCategorytype(wmitp.get(pid).getCategorytype());
                temp.setWorryItem(wit);
            }
        }
        return wmp;
    }

    void updateFatherListWorryItem(String ticket, Map<Integer, WorryItem> wmitp) {
        WorryList wmp = updateWorryListItem(ticket, wmitp);
        worrymongo.ineffectWorryList(ticket);
        worrymongo.saveWorryList(wmp);
    }

    void updateSonListWorryItem(String ticket, Map<Integer, WorryItem> wmitp) {
        WorryList wmp = updateWorryListItem(ticket, wmitp);
        wmp.setGuessed(2);
        worrymongo.ineffectWorryList(ticket);
        worrymongo.saveWorryList(wmp);
    }

    List<WorryItem> guessWorryCategoryBayesian(List<Integer> proIds, Map state, String ticket) {
        state.putIfAbsent("now", 0);
        List<WorryItem> worryItemList = new ArrayList();
        if (proIds == null || proIds.isEmpty()) {
            return null;
        }
        Map<Integer, WorryItem> uneedGuessed = new HashMap();
        Set<String> sonticket = new HashSet();
        if (ticket != null) {
            worrymongo.getWorryGuessedSonTicket(ticket).forEach(s -> {
                WorryList wl = worrymongo.findWorryList(s);
                wl.getValue().forEach(l -> uneedGuessed.put(l.getProcessId(), l.getWorryItem()));
            });
        }
        ReadWriteLock lock = null;
        List<Worry> worrys = egg.findWorryList(proIds);
        for (int i = 0; i < worrys.size(); i++) {
            System.out.println(worrys.get(i).getProcessId());
            Category ct;
            if (!uneedGuessed.containsKey(worrys.get(i).getProcessId())) {
                ct = categoryClassify.classify(worrys.get(i).getProcessTitle() + " " + worrys.get(i).getContent());
            } else {
                ct = new Category();
                WorryItem it = uneedGuessed.get(worrys.get(i).getProcessId());
                ct.setSupercategory(it.getSupercategory());
                ct.setCategorytype(it.getCategorytype());
            }
            WorryItem wt = new WorryItem(proIds.get(i));
            if (state.containsKey("lock")) {
                lock = (ReadWriteLock) state.get("lock");
                lock.writeLock().lock();
                lock.writeLock().unlock();
                if (((AtomicInteger) state.get("threadNum")).get() == 0) {
                    state.remove("lock");
                }
                if (ticket != null) {
                    worrymongo.getWorryGuessedSonTicket(ticket).forEach(s -> {
                        if (!sonticket.contains(s)) {
                            sonticket.add(s);
                            WorryList wl = worrymongo.findWorryList(s);
                            wl.getValue().forEach(l -> uneedGuessed.put(l.getProcessId(), l.getWorryItem()));
                        }
                    });
                }
            }
            if (ct.getSupercategory() == null) {
                continue;
            }
            wt.setSupercategory(ct.getSupercategory());
            wt.setCategorytype(ct.getCategorytype());
            worryItemList.add(wt);
            state.put("now", (int) state.get("now") + 1);
        }
        return worryItemList;
    }

    public List<WorryItem> guessWorryCategoryBayesian(List<Integer> proIds) {
        return guessWorryCategoryBayesian(proIds, new HashMap(), null);
    }

    @Override
    public List<WorryItem> guessWorryCategory(List<Integer> proIds) throws SQLException {
        List<WorryItem> worryItemList = new ArrayList();
        for (int pid : proIds) {
            WorryItem worryItem = new WorryItem(pid);
            List<WorryItem> t = worrymongo.findWorryItem(pid);
            if (t.isEmpty() || (t.get(0).getSupercategory() == null && t.get(0).getCategorytype() == null && t.get(0).getCategorytype() == 11 && t.get(0).getSupercategory() == 100)) {
                saveWorryCharacteristic(Arrays.asList(pid));
                t = worrymongo.findWorryItem(pid);
            }
            if (t.get(0).getContentSPword() == null) {
                saveWorryCharacteristic(Arrays.asList(t.get(0).getProcessId()));
                t.set(0, worrymongo.findWorryItem(pid).get(0));
            }
            List<WorryItem> twins = worrymongo.findTwinsUserful(pid, null);
            List<Map<WorryItem, Double>> res = calculateSimilarity(t.get(0), twins);
            List wt = res.stream().map(s -> s.keySet().toArray()[0]).collect(Collectors.toList());
            List<Map<WorryItem, Integer>> res2 = calculateDepartment(t.get(0), (List<WorryItem>) wt);
            if (res2 == null) {
                WorryItem wts = (WorryItem) res.get(0).keySet().toArray()[0];
                worryItem.setCategorytype(wts.getCategorytype());
                worryItem.setSupercategory(wts.getSupercategory());
            }
            Map<Integer, Integer> cateFre = new HashMap<>();
            res2.forEach(s -> {
                WorryItem wtm = (WorryItem) s.keySet().toArray()[0];
                if (cateFre.containsKey(wtm.getCategorytype())) {
                    cateFre.put(wtm.getCategorytype(), cateFre.get(wtm.getCategorytype()) + 1);
                } else if (wtm.getCategorytype() != 11) {
                    cateFre.put(wtm.getCategorytype(), 1);
                }
            });
            int max = 0;
            Set<Integer> catSet = new HashSet();
            for (Map.Entry<Integer, Integer> temp : cateFre.entrySet()) {
                if (temp.getValue() > max) {
                    max = temp.getValue();
                }
            }
            for (Map.Entry<Integer, Integer> temp : cateFre.entrySet()) {
                if (temp.getValue() == max) {
                    catSet.add(temp.getKey());
                }
            }
            for (Map<WorryItem, Integer> tmp : res2) {
                WorryItem wrtm = (WorryItem) tmp.keySet().toArray()[0];
                if (catSet.contains(wrtm.getCategorytype())) {
                    worryItem.setCategorytype(wrtm.getCategorytype());
                    break;
                }
            }
            max = 0;
            cateFre.clear();
            res2.forEach(s -> {
                WorryItem wtm = (WorryItem) s.keySet().toArray()[0];
                if (cateFre.containsKey(wtm.getCategorytype())) {
                    cateFre.put(wtm.getSupercategory(), cateFre.get(wtm.getSupercategory()) + 1);
                } else if (wtm.getSupercategory() != 100) {
                    cateFre.put(wtm.getSupercategory(), 1);
                }
            });
            Set<Integer> supCatSet = new HashSet();
            for (Map.Entry<Integer, Integer> temp : cateFre.entrySet()) {
                if (temp.getValue() > max) {
                    max = temp.getValue();
                }
            }
            for (Map.Entry<Integer, Integer> temp : cateFre.entrySet()) {
                if (temp.getValue() == max) {
                    supCatSet.add(temp.getKey());
                }
            }
            for (Map<WorryItem, Integer> tmp : res2) {
                WorryItem wrtm = (WorryItem) tmp.keySet().toArray()[0];
                if (supCatSet.contains(wrtm.getSupercategory())) {
                    worryItem.setSupercategory(wrtm.getSupercategory());
                }
            }
            worryItemList.add(worryItem);
            System.out.println(worryItem.getCategorytype() + " " + worryItem.getSupercategory());
        }
        return worryItemList;
    }


    @Override
    public void setworryItem(List<WorryItem> worry, String ip, String personName) {
        try {
            egg.setworryItem(worry);
            egg.insertItemLog(worry, ip, personName);
            updateWorryItem(worry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void updateWorryItem(List<WorryItem> worry) throws Exception {
        worry.forEach(w -> {
            try {
                Map a = egg.find("select content,processTitle from c_worry where processId=?", new Object[]{w.getProcessId()}).get(0);
                worrymongo.updateWorryItem(w.getProcessId(), w.getCategorytype(), w.getSupercategory(),
                        WordKnife.segWordPurely(a.get("processTitle") + " " + a.get("content")));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        categoryClassify.updateWordSample();
    }

    @Override
    public void worryContentSPwordEffect(int processId, List<Map.Entry<String, Integer>> spwords) throws Exception {
        Map<String, Object> inData = new HashMap();
        worrymongo.effectWorrySP(processId, spwords, null, null);

    }

    @Override
    public List<Remark> getWorryRemark(int processId, Integer type) {
        try {
            return egg.findRemarks(processId, type);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public void commentWorry(int processId, String remark, String username, int remarktype) throws SQLException {
        egg.commentWorry(processId, remark, username, remarktype);
    }

    @Override
    public void approveRemark(int remarksId) throws SQLException {
        egg.approveRemark(remarksId);
    }
}
