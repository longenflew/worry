package com.unicom.betterworry.dao.Impl;

import com.unicom.betterworry.being.Category;
import com.unicom.betterworry.being.Word;
import com.unicom.betterworry.being.WorryItem;
import com.unicom.betterworry.dao.CategoryClassfiyMongo;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Repository
public class CategoryClassfiyMongoImpl implements CategoryClassfiyMongo {
    private final MongoTemplate mongo;

    @Autowired
    public CategoryClassfiyMongoImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }


    public void updateWordClassProbability(List<Word> words, String collection) {
        words.forEach(w -> {
            Query query = new Query(Criteria.where("text").is(w.getText()));
            Update update = new Update();
            update.inc("frequency", w.getFrequency());
            mongo.upsert(query, update, collection);
        });
    }

    public void updateWorryClassProbability(Category category, String collection) {
        Query query = new Query();
        Criteria criteria = null;
        Update update = new Update();
        if (category.getSupercategory() != null) {
            criteria = Criteria.where("supercategory").is(category.getSupercategory());
            query.addCriteria(criteria);
            update.inc("sum");
            update.inc("wordsum", category.getWordsum());
            System.out.println(mongo.upsert(query, update, collection).getUpsertedId());
            category.getWord().forEach(w -> {
                Criteria cr = Criteria.where("supercategory").is(category.getSupercategory());
                cr.and("word.text").is(w.getText());
            });
        }
    }

    public long countNewWorryItem(Timestamp time) {
        Query query = new Query();
        Criteria criteria = Criteria.where("insertTime").gt(time);
        query.addCriteria(criteria);
        return mongo.count(query, "worryItem");
    }

    public List<WorryItem> findNewWorryItem(Timestamp time) {
        Query query = new Query();
        Criteria criteria = Criteria.where("insertTime").gt(time);
        query.addCriteria(criteria);
        return mongo.find(query, WorryItem.class, "worryItem");
    }

    public Integer sum(String param, String collection) {
        Aggregation aggregation = Aggregation.newAggregation(match(new Criteria()), group().sum(param).as("sum"));
        AggregationResults<Map> ar = mongo.aggregate(aggregation, collection, Map.class);
        List<Map> res = ar.getMappedResults();
        if (!res.isEmpty())
            return (Integer) res.get(0).get("sum");
        return null;
    }

    public List<Integer> findDistinctSupcate() {
        Query query = new Query();
        query.fields().include("supercategory");
        Criteria criteria = Criteria.where("supercategory").gt(100);
        query.addCriteria(criteria);
        return mongo.findDistinct(query, "supercategory", "worryItem", Integer.class);
    }

    public List<Word> findPossessionsWord() {
        Query query = new Query();
        query.addCriteria(Criteria.where("supercategory").gt(100));
        query.fields().include("contentSPword");
        List<Word> wl = new ArrayList<Word>();
        mongo.find(query, WorryItem.class, "worryItem").forEach(s ->
                wl.addAll(s.getContentSPword())
        );
        return wl;
    }

    public void saveData(final List in, String collection) {
        mongo.insert(in, collection);
    }

    public List<Map> findequal(Map<String, Object> args, String collection, List<String> fields) {
        return findequal(args, Map.class, collection, fields);
    }

    public <T> List<T> findequal(Map<String, Object> args, Class<T> entityClass, String collection, List<String> fields) {
        Query query = new Query();
        fields.forEach(m -> query.fields().include(m));
        if (args.isEmpty() && fields.isEmpty()) {
            return mongo.findAll(entityClass, collection);
        }
        Criteria criteria = new Criteria();
        args.forEach((key, value) -> criteria.and(key).is(value));
        query.addCriteria(criteria);
        return mongo.find(query, entityClass, collection);
    }

    public List findequal(Map<String, Object> args, String collection) {
        return findequal(args, collection, new ArrayList<>());
    }

    public List<WorryItem> findWorryItem(Integer processId) {
        Map in = new HashMap();
        in.put("processId", processId);
        return findequal(in, WorryItem.class, "worryItem", new ArrayList());
    }

    @Override
    public void clearWordCollection() {
        mongo.remove(new Query(), "wordClassProbability");
    }

    public void updateMap(Map<String, Object> in, Map<String, Object> updata, String collection) {
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

    public List<Category> findCategoryinWord(Collection words, String type) {
        if (type.equals("super"))
            return findIn("word.text", words, Category.class, "worryClassProbability", null);
        else if (type.equals("base"))
            return findIn("word.text", words, Category.class, "worryBaseClassProbability", null);
        return null;
    }

    public <T> List<T> findIn(String param, Collection values, Class<T> clz, String collection, List<String> includeArs) {
        Query query = new Query();
        Criteria criteria = Criteria.where(param).in(values);
        query.addCriteria(criteria);
        if (includeArs != null)
            includeArs.forEach(s -> query.fields().include(s));
        return mongo.find(query, clz, collection);
    }
}
