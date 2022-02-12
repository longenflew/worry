package com.unicom.betterworry.dao.Impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.unicom.betterworry.being.*;
import com.unicom.betterworry.dao.WorryMongo;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Repository
public class WorryMongoImpl implements WorryMongo {
    private final MongoTemplate mongo;

    @Autowired
    public WorryMongoImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public boolean createCollection() {
        String collectionName = "worrylist";
        if (mongo.collectionExists(collectionName))
            return true;
        mongo.createCollection(collectionName);
        // 检测新的集合是否存在，返回创建结果
        return mongo.collectionExists(collectionName) ? true : false;
    }

    @Override
    public void updateMap(Map<String, Object> in, Map<String, Object> updata, String collection) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Update update = new Update();
        in.entrySet().stream().forEach(m -> {
            criteria.and(m.getKey()).is(m.getValue());
        });
        updata.forEach(update::set);
        query.addCriteria(criteria);
        mongo.upsert(query, update, collection);
    }

    @Override
    public void updateMapAll(Map<String, Object> in, Map<String, Object> updata, String collection) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Update update = new Update();
        in.entrySet().stream().forEach(m -> {
            criteria.and(m.getKey()).is(m.getValue());
        });
        updata.forEach(update::set);
        query.addCriteria(criteria);
        mongo.updateMulti(query, update, collection);
    }

    @Override
    public void upsertMap(Map<String, Object> in, Map<String, Object> updata, String collection) {
        Document indata = new Document();
        Document sql = new Document("$set", indata);
        Query query = new Query();
        Criteria criteria = new Criteria();
        Update update = new Update();
        in.entrySet().stream().forEach(m -> {
            criteria.and(m.getKey()).is(m.getValue());
            update.setOnInsert(m.getKey(), m.getValue());
        });
        updata.forEach(update::set);
        query.addCriteria(criteria);
        mongo.upsert(query, update, collection);
    }

    @Override
    public void setWorryListGuessState(String ticket, int guessed) {
        Map arg = new HashMap();
        arg.put("ticket", ticket);
        arg.put("access", true);
        Map updata = new HashMap();
        updata.put("guessed", guessed);
        updateMapAll(arg, updata, "worrylist");
    }

    @Override
    public void setWorryListGuessState(String ticket, int guessed, int index) {
        Map arg = new HashMap();
        arg.put("ticket", ticket);
        arg.put("access", true);
        arg.put("index", index);
        Map updata = new HashMap();
        updata.put("guessed", guessed);
        updateMapAll(arg, updata, "worrylist");
    }

    @Override
    public Integer getWorryListGuessState(String ticket) {
        Map arg = new HashMap();
        arg.put("ticket", ticket);
        arg.put("access", true);
        List<WorryList> rl = findequal(arg, WorryList.class, "worrylist", Collections.singletonList("guessed"));
        if (rl.isEmpty())
            return null;
        return rl.stream().mapToInt(r -> r.getGuessed()).min().getAsInt();
    }

    @Override
    public Set<String> findWorryListSonTicket(String ticket) {
        if (ticket == null)
            return null;
        Queue<String> hasSonqueue = new LinkedList();
        Set<String> sonTicketSet = new HashSet();
        hasSonqueue.add(ticket);
        do {
            String t = hasSonqueue.poll();
            Query query = new Query();
            Criteria criteria = Criteria.where("fatherTicket").is(t).and("access").is(true);
            query.addCriteria(criteria);
            query.fields().include("ticket");
            List<Map> r = mongo.find(query, Map.class, "worrylist");
            Set sset = r.stream().map(s -> s.get("ticket").toString()).collect(Collectors.toSet());
            hasSonqueue.addAll(sset);
            sonTicketSet.addAll(sset);
        }
        while (!hasSonqueue.isEmpty());
        return sonTicketSet;
    }

    @Override
    public Set<String> findWorryListFatherTicket(String ticket) {
        if (ticket == null)
            return null;
        WorryList wlist = findWorryList(ticket, 0);
        if (wlist.getFatherTicket() == null)
            return new HashSet();
        Queue<String> hasSonqueue = new LinkedList();
        Set<String> fatherTicketSet = new HashSet();
        hasSonqueue.add(wlist.getFatherTicket());
        fatherTicketSet.add(wlist.getFatherTicket());

        do {
            String t = hasSonqueue.poll();
            Query query = new Query();
            Criteria criteria = Criteria.where("ticket").is(t).and("access").is(true);
            query.addCriteria(criteria);
            query.fields().include("fatherTicket");
            List<Map> r = mongo.find(query, Map.class, "worrylist");
            Set<String> fset = r.stream().filter(s -> s.containsKey("fatherTicket")).map(s -> s.get("fatherTicket").toString()).collect(Collectors.toSet());
            hasSonqueue.addAll(fset);
            fatherTicketSet.addAll(fset);
        }
        while (!hasSonqueue.isEmpty());
        return fatherTicketSet;
    }


    @Override
    public Set<String> getWorryGuessedSonTicket(String ticket) {
        if (ticket == null)
            return null;
        Queue<String> hasSonqueue = new LinkedList();
        hasSonqueue.add(ticket);
        Set<String> sonTicketSet = new HashSet();
        do {
            String t = hasSonqueue.poll();
            Query query = new Query();
            Criteria criteria = Criteria.where("fatherTicket").is(t).and("access").is(true).orOperator(Criteria.where("guessed").is(11), Criteria.where("guessed").is(1));
            query.addCriteria(criteria);
            query.fields().include("ticket", "guessed");
            List<Map> r = mongo.find(query, Map.class, "worrylist");
            hasSonqueue.addAll(r.stream().filter(s -> (int) s.get("guessed") == 11).map(s -> s.get("ticket").toString()).collect(Collectors.toList()));
            sonTicketSet.addAll(r.stream().filter(s -> (int) s.get("guessed") == 1).map(s -> s.get("ticket").toString()).collect(Collectors.toSet()));
        }
        while (!hasSonqueue.isEmpty());
        return sonTicketSet;
    }

    @Override
    public void saveData(final List in, String collection) {
        mongo.insert(in, collection);
    }

    @Override
    public <T> void saveData(T enity, String collection) {
        mongo.insert(enity, collection);
    }

    @Override
    public void updateWorryItem(int processId, Integer category, Integer supercategory, List<Word> words) {
        Map in = new HashMap();
        in.put("processId", processId);
        Map data = new HashMap();
        if (category == null && supercategory == null) {
            return;
        }
        if (category != null) {
            data.put("categorytype", category);
        }
        if (supercategory != null && supercategory > 100) {
            data.put("supercategory", supercategory);
        }
        data.put("contentSPword", words);
        data.put("insertTime", new Timestamp(System.currentTimeMillis()));
        updateMap(in, data, "worryItem");
    }

    void saveWorryList(final Map in) {
        Map data = new HashMap(in);
        data.remove("_id");
        data.put("insertTime", new Timestamp(System.currentTimeMillis()));
        mongo.insert(data, "worrylist");
    }

    @Override
    public void saveWorryList(WorryList worrylist) {
        List<Worry> worryArr = new ArrayList(worrylist.getValue());
        if (worryArr.isEmpty())
            return;
        int i = 0;
        worrylist.setInsertTime(new Date());
        for (i = 0; i < worryArr.size() / 1000; i++) {
            List<Worry> temp = worryArr.subList(i * 1000, (i + 1) * 1000);
            worrylist.setValue(temp);
            worrylist.setIndex(i * 1000);
            mongo.save(worrylist, "worrylist");
        }
        worrylist.setValue(worryArr.subList(i * 1000, worryArr.size()));  //将搜索结果保存至mongo
        worrylist.setIndex(i * 1000);
        mongo.save(worrylist, "worrylist");
    }

    @Deprecated
    public void saveWorryList(Map baseinfo, List worryArr) {
        int i = 0;
        if (worryArr == null || worryArr.isEmpty()) {
            return;
        }
        for (i = 0; i < worryArr.size() / 1000; i++) {
            List temp = worryArr.subList(i * 1000, (i + 1) * 1000);
            baseinfo.put("value", temp);
            baseinfo.put("index", i * 1000);
            baseinfo.put("size", worryArr.size());
            saveWorryList(baseinfo);
        }
        baseinfo.put("value", worryArr.subList(i * 1000, worryArr.size()));  //将搜索结果保存至mongo
        baseinfo.put("index", i * 1000);
        baseinfo.put("size", worryArr.size());
        saveWorryList(baseinfo);
    }

    @Override
    public List<Map> findequal(Map<String, Object> args, String collection, List<String> fields) {
        return findequal(args, Map.class, collection, fields);
    }

    @Override
    public <T> List<T> findequal(Map<String, Object> args, Class<T> entityClass, String collection, List<String> fields) {
        return findequal(args, entityClass, collection, fields, 0);
    }

    @Override
    public <T> List<T> findequal(Map<String, Object> args, Class<T> entityClass, String collection, List<String> fields, int limit) {
        Query query = new Query();
        if (fields != null) {
            fields.forEach(m -> query.fields().include(m));
        }
        if (args.isEmpty() && fields != null && fields.isEmpty()) {
            return mongo.findAll(entityClass, collection);
        }
        Criteria criteria = new Criteria();
        args.forEach((k, v) -> criteria.and(k).is(v));
        query.addCriteria(criteria);
        if (limit > 0)
            query.limit(limit);
        return mongo.find(query, entityClass, collection);
    }

    @Override
    public List findequal(Map<String, Object> args, String collection) {
        return findequal(args, collection, new ArrayList<>());
    }

    @Override
    public List<WorryItem> findWorryItem(Integer processId) {
        Map in = new HashMap();
        in.put("processId", processId);
        return findequal(in, WorryItem.class, "worryItem", new ArrayList());
    }

    public List<WorryItem> findExistWorryItem() {
        Query query = new Query();
        Criteria criteria = Criteria.where("categorytype").exists(true);
        query.addCriteria(criteria);
        return mongo.findAll(WorryItem.class, "worryItem");
    }

    @Override
    public List<WorryList> findWorryListDebris(String ticket) {
        Map arg = new HashMap();
        arg.put("ticket", ticket);
        arg.put("access", true);
        return findequal(arg, WorryList.class, "worrylist", null);
    }

    @Override
    public WorryList findWorryList(String ticket) {
        List<WorryList> wld = findWorryListDebris(ticket);
        if (wld == null && wld.isEmpty()) {
            return new WorryList();
        }
        WorryList wlr = wld.get(0);
        List<Worry> worryList = new ArrayList();
        for (int i = 0; i < wld.size(); i++) {
            worryList.addAll(wld.get(i).getValue());
        }
        wlr.setValue(worryList);
        return wlr;
    }

    @Deprecated
    public Map findWorryList(String key, String val) {
        MongoCollection<Document> collection = mongo.getCollection("worrylist");
        BasicDBObject filter = new BasicDBObject();
        filter.append(key, val);
        filter.append("access", true);
        MongoCursor<Document> mongoCursor = collection.find(filter)
                .sort(new BasicDBObject("index", -1)).iterator();
        Map result = new HashMap();
        List worrylist = new ArrayList();
        Document doc = null;
        while (mongoCursor.hasNext()) {
            doc = mongoCursor.next();
            worrylist.addAll((List) doc.get("value"));
        }
        if (doc == null) {
            return null;
        }
        result.put("list", worrylist);
        result.put("size", doc.getInteger("size"));
        result.put("fixed", doc.getBoolean("fixed"));
        result.put("ticket", doc.get("ticket"));
        result.put("ip", doc.get("ip"));
        result.put("filter", doc.get("filter"));
        if (doc.containsKey("fatherTicket")) {
            result.put("fatherTicket", doc.get("fatherTicket"));
        }
        if (doc.containsKey("guessed")) {
            result.put("guessed", doc.get("guessed"));
        }
        return result;
    }

    public void setWorryListItem(List<WorryItem> worryItemList) {

    }

    @Override
    public void saveMap(final Map in, String collection) {
        Map data = new HashMap();
        data.putAll(in);
        Document doc = new Document();
        data.put("insertTime", new Timestamp(System.currentTimeMillis()));
        mongo.insert(data, collection);
    }

    @Override
    public void effectWorrySP(int proId, List<Map.Entry<String, Integer>> contentsp, List<Map.Entry<String, Integer>> streamsp, List<String> Department) {
        Query query = new Query();
        query.addCriteria(Criteria.where("processId").is(proId));
        Update update = new Update();
        if (contentsp != null)
            update.push("contentSPword", contentsp);
        if (streamsp != null)
            update.push("streamSPword", streamsp);
        if (Department != null)
            update.push("streamSPword", streamsp);
        mongo.upsert(query, update, "worryItem");
    }


    @Override
    public boolean checkWorryList(String key, Object val) {
        Query query = new Query();
        Criteria criteria = Criteria.where(key).is(val).and("access").is(true);
        query.addCriteria(criteria);
        return mongo.exists(query, "worrylist");
    }

    @Override
    public WorryList findWorryList(String ticket, int index) {
        index = index - index % 1000;
        Query query = new Query();
        Criteria criteria = Criteria.where("ticket").is(ticket).
                and("index").is(index).and("access").is(true);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Order.desc("insertTime")));
        query.limit(1);
        List<WorryList> reslist = mongo.find(query, WorryList.class, "worrylist");
        if (reslist.isEmpty()) {
            return new WorryList();
        }
        List<Worry> worrylist = new ArrayList();
        for (int i = 0; i < reslist.size(); i++) {
            worrylist.addAll(reslist.get(i).getValue());
        }
        reslist.get(0).setValue(worrylist);
        return reslist.get(0);
    }

    @Deprecated
    public Map findWorryList(List<Object[]> args) {
        Query query = new Query();
        Criteria criteria = null;
        if (args == null || args.isEmpty())
            criteria = new Criteria();
        else
            criteria = Criteria.where((String) args.get(0)[0]).is(args.get(0)[1]);
        for (int i = 1; i < args.size(); i++) {
            criteria.and((String) args.get(i)[0]).is(args.get(i)[1]);
        }
        criteria.and("access").is(true);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Order.desc("insertTime")));
        query.limit(1);
        List<Map> reslist = mongo.find(query, Map.class, "worrylist");
        if (reslist.isEmpty())
            return null;
        List worrylist = new ArrayList();
        for (int i = 0; i < reslist.size(); i++) {
            worrylist.addAll((List) reslist.get(i).get("value"));
        }
        Map result = new HashMap();
        result.put("list", worrylist);
        result.put("size", reslist.get(0).get("size"));
        result.put("fixed", reslist.get(0).get("fixed"));
        result.put("ticket", reslist.get(0).get("ticket"));
        result.put("ip", reslist.get(0).get("ip"));
        result.put("filter", reslist.get(0).get("filter"));
        if (reslist.get(0).containsKey("fatherTicket"))
            result.put("fatherTicket", reslist.get(0).get("fatherTicket"));
        if (reslist.get(0).containsKey("guessed"))
            result.put("guessed", reslist.get(0).get("guessed"));
        return result;
    }

    @Override
    public List<WorryItem> findTwinsUserful(int processId, List<Integer> pids) {
        Map arg = new HashMap();
        arg.put("processId", processId);
        List<WorryItem> worryItem = findWorryItem(processId);
        assert !worryItem.isEmpty();
        List<Word> words = worryItem.get(0).getContentSPword();
        Query query = new Query();
        Criteria criteria = Criteria.where("contentSPword.text").in(words.stream().map(Word::getText).collect(Collectors.toList()));
        if (pids != null)
            criteria.and("processId").in(pids);
        criteria.and("categorytype").exists(true);
        query.addCriteria(criteria);
        return mongo.find(query, WorryItem.class, "worryItem");
    }


    @Override
    public List<WorryItem> findWorryAllWord(int processId, String ticket) {
        Map arg = new HashMap();
        arg.put("processId", processId);
        List<WorryItem> worry = findequal(arg,WorryItem.class, "worryItem",null);
        List<Word> words = worry.get(0).getContentSPword();
        Query query = new Query();
        Criteria criteria = Criteria.where("contentSPword.text").in(words.stream().map(Word::getText).collect(Collectors.toList()));
        if (ticket != null && checkWorryList("ticket", ticket))
            criteria.and("processId").in((findWorryList( ticket).getValue()).stream().map(Worry::getProcessId).collect(Collectors.toList()));
        else if (!checkWorryList("ticket", ticket)) {
            return null;
        }
        query.addCriteria(criteria);
        return mongo.find(query, WorryItem.class, "worryItem");
    }

    @Override
    public List<WorryItem> findWorryAllWord(int processId, List<Integer> pids) {
        Map arg = new HashMap();
        arg.put("processId", processId);
        List<WorryItem> worry = findequal(arg,WorryItem.class, "worryItem",null);
        List<Word> words = worry.get(0).getContentSPword();
        Query query = new Query();
        Criteria criteria = Criteria.where("contentSPword.text").in(words.stream().map(Word::getText).collect(Collectors.toList()));
        criteria.and("processId").in(pids);
        query.addCriteria(criteria);
        return mongo.find(query, WorryItem.class, "worryItem");
    }

    @Override
    public List findNotIn(String param, List values, String collection, List<String> includeArs) {
        Query query = new Query();
        Criteria criteria = Criteria.where(param).in(values);
        query.addCriteria(criteria);
        includeArs.forEach(s -> query.fields().include(s));
        Set set = mongo.find(query, Map.class, collection).stream().map(s -> s.get(param)).collect(Collectors.toSet());
//        List<Map> outvalue=value.stream().filter(s -> !pid.contains(s.get(param))).collect(Collectors.toList());
        return (List) values.stream().filter(s -> !set.contains(s)).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> findIn(String param, List values, Class<T> clz, String collection, List<String> includeArs) {
        Query query = new Query();
        Criteria criteria = Criteria.where(param).in(values);
        query.addCriteria(criteria);
        if (includeArs != null)
            includeArs.forEach(s -> query.fields().include(s));
        return mongo.find(query, clz, collection);
    }

    @Override
    public Integer sum(String param, String collection) {
        Aggregation aggregation = Aggregation.newAggregation(match(new Criteria()), group().sum(param).as("sum"));
        AggregationResults<Map> ar = mongo.aggregate(aggregation, collection, Map.class);
        List<Map> res = ar.getMappedResults();
        if (!res.isEmpty())
            return (Integer) res.get(0).get("sum");
        return null;
    }

    //    public List<Map> findWorryNotIn(String param, String ticket, String collection, List<String> includeArs) {
////        Query query = new Query();
////        Criteria criteria = Criteria.where(param);
////        query.addCriteria(criteria);
////        includeArs.forEach(s -> query.fields().include(s));
////        Set<Integer> pid = (Set) values.stream().collect(Collectors.toSet());
////        List<Map> value=mongo.find(query, Map.class, collection);
////        return value.stream().filter(s -> !pid.contains((Integer) s.get("processId"))).collect(Collectors.toList());
//    }
    @Override
    public void ineffectWorryList(String ticket) {
        Map in = new HashMap();
        in.put("ticket", ticket);
        in.put("access", true);
        Map data = new HashMap();
        data.put("access", false);
        updateMapAll(in, data, "worrylist");
    }

    @Override
    public void ineffectWorryList(String ticket, int index) {
        Map in = new HashMap();
        in.put("ticket", ticket);
        in.put("index", index);
        in.put("access", true);
        Map data = new HashMap();
        data.put("access", false);
        updateMap(in, data, "worrylist");
    }

}