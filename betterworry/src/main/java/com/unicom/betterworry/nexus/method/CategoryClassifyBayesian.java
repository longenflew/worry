package com.unicom.betterworry.nexus.method;

import com.unicom.betterworry.being.Category;
import com.unicom.betterworry.being.Word;
import com.unicom.betterworry.being.WorryItem;
import com.unicom.betterworry.dao.CategoryClassfiyMongo;
import com.unicom.betterworry.nexus.CategoryClassify;
import com.unicom.betterworry.nexus.WordKnife;
import com.unicom.betterworry.service.WorryService;
import com.unicom.betterworry.util.Heap;
import com.unicom.betterworry.util.HeapFload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class CategoryClassifyBayesian implements CategoryClassify {

    private WorryService wsvc;
    @Autowired
    private CategoryClassfiyMongo ccssmg;
    private static Logger logger = LoggerFactory.getLogger(CategoryClassifyBayesian.class);


    @Autowired
    public void setWsvc(@Lazy WorryService wsvc) {
        this.wsvc = wsvc;
    }

    public void caculateSample(String collection) throws SQLException {
        List<Integer> sups ;
        String catetype;
        if(collection.contains("Base")){
            sups=Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            catetype="categorytype";
        }
        else{
            sups=ccssmg.findDistinctSupcate();
            catetype="supercategory";
        }
        for (Integer supcate : sups) {
            Map arg = new HashMap();
            arg.put(catetype, supcate);
            List<WorryItem> worryItemList = ccssmg.findequal(arg, WorryItem.class, "worryItem", new ArrayList());
            Map<String, AtomicInteger> word = new HashMap();
            Map<String, AtomicInteger> department = new HashMap();
            int listSize = worryItemList.size();
            for (WorryItem worryItem : worryItemList) {
                if (worryItem.getContentSPword() == null || worryItem.getStreamDepartments() == null) {
                    wsvc.saveWorryCharacteristic(Collections.singletonList(worryItem.getProcessId()));
                    worryItem = ccssmg.findWorryItem(worryItem.getProcessId()).get(0);
                }
                worryItem.getContentSPword().forEach(s -> {
                    word.putIfAbsent(s.getText(), new AtomicInteger());
                    word.get(s.getText()).getAndIncrement();
                });
                if (worryItem.getStreamDepartments() != null) {
                    worryItem.getStreamDepartments().forEach(s -> {
                        department.putIfAbsent(s, new AtomicInteger());
                        department.get(s).getAndIncrement();
                    });
                }
            }
            List<Word> words = new ArrayList();
            word.forEach((key, value) -> {
                Word w = new Word(key);
                w.setFrequency(value.get());
                w.setPercent((double) w.getFrequency() / listSize);
                words.add(w);
            });
//            List<Map<String, Object>> departments = new ArrayList();
//            department.forEach((key, value) -> {
//                Map<String, Object> d = new HashMap();
//                d.put("department", key);
//                d.put("frequency", value.get());
//                d.put("percent", (float) value.get() / listSize);
//                departments.add(d);
//            });
            Map<String, Object> data = new HashMap();
            data.put("word", words);
//            data.put("department", departments);
            data.put("sum", listSize);
            Map in = new HashMap();
            in.put(catetype, supcate);
            ccssmg.updateMap(in, data, collection);
        }
    }


    public void caculateWordSample() {
        List<Word> collectionWord = ccssmg.findPossessionsWord();
        Map<String, Word> wordStatisticsMap = new HashMap();
        int sum = 0;
        for (Word word : collectionWord) {
            wordStatisticsMap.putIfAbsent(word.getText(), new Word(word.getText(), 0,word.getWordform()));
            Word wt = wordStatisticsMap.get(word.getText());
            wt.setFrequency(wt.getFrequency() + word.getFrequency());
            sum += wt.getFrequency();
        }
        ccssmg.clearWordCollection();
        ccssmg.saveData(new ArrayList(wordStatisticsMap.values()), "wordClassProbability");
    }

    @Async
    public void updateWordSample() throws SQLException {
        Map arg=new HashMap();
        arg.put("name","Sample");
        List<Map> res=ccssmg.findequal(arg,"staticVariable",Arrays.asList("lastTime"));
        if(!res.isEmpty()){
            Timestamp st=new Timestamp(((Date)res.get(0).get("lastTime")).getTime());
            Long sum=ccssmg.countNewWorryItem(st);
            if(sum>100){
                caculateWordSample();
                caculateSample("worryBaseClassProbability");
                caculateSample("worryClassProbability");
                caculateWeight("worryBaseClassProbability");
                caculateWeight("worryClassProbability");
                Map in=new HashMap<>();
                in.put("name","Sample");
                Map data=new HashMap<>();
                data.put("lastTime",new Timestamp(System.currentTimeMillis()));
                ccssmg.updateMap(in,data,"staticVariable");
            }
            else{
                Map in=new HashMap<>();
                in.put("name","Sample");
                Map data=new HashMap<>();
                data.put("newItemSum",sum);
                ccssmg.updateMap(in,data,"staticVariable");
            }
        }
    }

    List<Word> segContent(String content){
       return WordKnife.segWordPurely(content);
    }

    public Category classify(String content) {
        if (content == null)
            return new Category();
        Set<String> inwordset = WordKnife.segWordPurely(content).stream().map(Word::getText).collect(Collectors.toSet());
        if(inwordset.isEmpty())
            return new Category();
        Integer cateSum = ccssmg.sum("sum", "worryBaseClassProbability");
        Integer supSum = ccssmg.sum("sum", "worryClassProbability");
        Integer wordSum = ccssmg.sum("frequency", "wordClassProbability");
        logger.info(content+"\ncatesum:"+cateSum+" supSum:"+supSum+" word:"+wordSum);
        Map<String, Word> wordFrequency = new HashMap();
        List<Word> wol=ccssmg.findIn("text",inwordset,Word.class,"wordClassProbability",new ArrayList());
        wol.forEach(s->wordFrequency.put(s.getText(),s));
        long tb=System.currentTimeMillis();
        Category c = classifySupcate(inwordset,wordFrequency, supSum, wordSum);
        logger.info("sup耗时："+(System.currentTimeMillis()-tb));
        tb=System.currentTimeMillis();
        logger.info("inwordset:"+inwordset);
        Category b = classifyBasecate(inwordset,wordFrequency, cateSum, wordSum);
        logger.info("base耗时："+(System.currentTimeMillis()-tb));
        if (c == null && b == null) {
            return new Category();
        } else if (c == null) {
            return b;
        } else if (b == null) {
            return c;
        }
        c.setCategorytype(b.getCategorytype());
        c.setCategoryProbability(b.getCategoryProbability());
        return c;
    }

    public List<Category> classifyBatch(List<String> contents) {
        if (contents == null || contents.isEmpty())
            return new ArrayList<Category>();
        Integer supSum = ccssmg.sum("sum", "worryClassProbability");
        Integer wordSum = ccssmg.sum("frequency", "wordClassProbability");
        Map<String, Word> wordFrequency = new HashMap();
        List<Set<String>> contentsWordSetList = new ArrayList();
        contents.forEach(content -> contentsWordSetList.add(WordKnife.segWordPurely(content).stream().map(Word::getText).collect(Collectors.toSet())));
        contentsWordSetList.forEach(cl -> {
                    cl.forEach(r -> {
                        Map arg = new HashMap();
                        arg.put("text", r);
                        List<Word> l = ccssmg.findequal(arg, Word.class, "wordClassProbability", new ArrayList());
                        if (!l.isEmpty()) {
                            wordFrequency.putIfAbsent(r, l.get(0));
                        }
                    });
                }
        );
        List<Category> categoryList=new ArrayList();
        for(int i=0;i<contentsWordSetList.size();i++){
            Category c = classifySupcate(contentsWordSetList.get(i),wordFrequency,supSum,wordSum);
            Category b = classifyBasecate(contentsWordSetList.get(i),wordFrequency,supSum,wordSum);
            if (c == null && b == null) {
                categoryList.add(new Category());
            } else if (c == null) {
                categoryList.add(b);
            } else if (b == null) {
                categoryList.add(c);
            }else{
                c.setCategorytype(b.getCategorytype());
                c.setCategoryProbability(b.getCategoryProbability());
                categoryList.add(c);
            }
        }
        return categoryList;
    }

    public List<Category> classifySupcateSortList(Set<String> wordset, Map<String, Word> wordFrequency, Integer supSum, Integer wordSum,String type){
        Long tb=System.currentTimeMillis();
        List<Category> rl = ccssmg.findCategoryinWord(wordset,type);
        Heap<Double, Integer> maxHeap = new HeapFload();
        for (Category s : rl) {
            int supcate = s.getSupercategory()==null?0:s.getSupercategory();
            if(supcate==0)
                continue;
            int supNum = s.getSum();
            double probabilitySupcate = 1f;
            Set<Word> wordList = s.getWord();
//            System.out.println(s.getSupercategory()+" ");
            for (Word ww : wordList) {
                if (wordFrequency.containsKey(ww.getText())) {
                    double probabilityWordcate = 1d;
                    double probabilityWord = 1d;
                    probabilityWordcate *= ww.getPercent();
                    probabilityWord *= (float) wordFrequency.get(ww.getText()).getFrequency() / wordSum;
                    probabilitySupcate *= probabilityWordcate / probabilityWord * (1-supNum/supSum);
//                    System.out.println(ww.getText()+" : "+probabilityWordcate / probabilityWord);
                }

            }
            maxHeap.insert(probabilitySupcate, supcate);
        }
        List<Category> suplist = new ArrayList();
        while (!maxHeap.isEmpty() ) {
            Map<Integer, Double> d = maxHeap.deleteMinOrMax();
            if(d==null)
                continue;
            d.forEach((k, v) -> {
                Category c = new Category();
                c.setSupercategory(k);
                c.setSupercategoryProbability(v);
                suplist.add(c);
            });
        }
        return suplist;
    }

    public Category classifySupcate(Set<String> wordset, Map<String, Word> wordFrequency, Integer supSum, Integer wordSum) {
        List<Category> suplist =classifySupcateSortList(wordset,wordFrequency,supSum,wordSum,"super");
        if(suplist.isEmpty())
            return new Category();
        if (suplist.size() > 1 && suplist.get(0).getSupercategory() == 109) {
            if (suplist.get(0).getSupercategoryProbability() / (double) suplist.get(1).getSupercategoryProbability()/1000 < 1000000)
                return suplist.get(1);
        }
        return suplist.get(0);
    }

    Category classifyBasecate(Set<String> wordset, Map<String, Word> wordFrequency, Integer supSum, Integer wordSum) {
        List<Category> rl = ccssmg.findCategoryinWord(wordset,"base");
        Heap<Double, Integer> maxHeap = new HeapFload();
        for (Category s : rl) {
            int supcate = 0;
            if(s.getCategorytype()!=null)
                supcate= s.getCategorytype();
            else
                continue;
            int supNum = s.getSum();
            double probabilitySupcate = 1f;
            Set<Word> wordList = s.getWord();
            for (Word ww : wordList) {
                if (wordFrequency.containsKey(ww.getText())) {
                    float probabilityWordcate = 1f;
                    float probabilityWord = 1f;
                    if(ww.getWeight()==null)
                        continue;
                    probabilityWordcate *= (double) ww.getWeight();
                    probabilityWord *= (float) wordFrequency.get(ww.getText()).getFrequency() / wordSum;
                    probabilitySupcate *= probabilityWordcate / probabilityWord* (1-supNum/supSum);
                }
            }
            probabilitySupcate *= ((float) supNum / supSum);
            maxHeap.insert(probabilitySupcate, supcate);
        }
        List<Category> suplist = new ArrayList();
        while (!maxHeap.isEmpty() && suplist.size() <= 2) {
            Map<Integer, Double> d = maxHeap.deleteMinOrMax();
            d.forEach((k, v) -> {
                Category c = new Category();
                c.setCategorytype(k);
                c.setCategoryProbability(v);
                suplist.add(c);
            });
        }
        return suplist.get(0);
    }

     public void caculateWeight(String collection) {
        List<Map> words = ccssmg.findequal(new HashMap(), collection, Arrays.asList("word"));
        Map<String, AtomicInteger> wordmap = new HashMap();
        words.forEach(lw -> ((List<Word>) lw.get("word")).forEach(w -> {
            wordmap.putIfAbsent(w.getText(), new AtomicInteger());
            wordmap.get(w.getText()).getAndIncrement();
        }));
        words.forEach(lw -> {
            ((List<Word>) lw.get("word")).forEach(w -> {
                double weight = (float) w.getFrequency() / wordmap.get(w.getText()).get();
                w.setWeight(weight);
            });
            Map in = new HashMap();
            in.put("_id", lw.get("_id"));
            Map data = new HashMap();
            data.put("word", lw.get("word"));
            data.put("wordsum",((List<Word>) lw.get("word")).stream().mapToInt(Word::getFrequency).sum());
            ccssmg.updateMap(in, data, collection);
        });
        //System.out.println(words);
    }

    public void trainSample(List<String> texts,int supercate){
        Integer cateSum = ccssmg.sum("sum", "worryBaseClassProbability");
        Integer supSum = ccssmg.sum("sum", "worryClassProbability");
        Integer wordSum = ccssmg.sum("frequency", "wordClassProbability");
        Map arg = new HashMap();
        arg.put("supercategory", supercate);
        List<Category> cList = ccssmg.findequal(arg, Category.class, "worryClassProbability", new ArrayList());
        Category category=cList.get(0);
        Map<String,Word> WordMap=new HashMap();
        category.getWord().forEach(w->WordMap.put(w.getText(),w));
        int tag=0,times=0;
        Map<String, Word> wordFrequency = new HashMap();
        for(int i=0;i<texts.size();i++){
            tag=0;
            String str=texts.get(i);
            double truerate=calculateP(category,WordKnife.segWordPurely(str));
            Set<String> inwordset = WordKnife.segWordPurely(str).stream().map(Word::getText).collect(Collectors.toSet());
            inwordset.forEach(r -> {
                Map arg1 = new HashMap();
                arg1.put("text", r);
                List<Word> l = ccssmg.findequal(arg1, Word.class, "wordClassProbability", new ArrayList());
                if (!l.isEmpty()) {
                    wordFrequency.putIfAbsent(r, l.get(0));
                }
            });
            List<Category> cateSort=classifySupcateSortList(inwordset,wordFrequency,supSum,wordSum,"super");
            System.out.println("识别类别为："+cateSort.get(0).getSupercategory()+" ");
            while(cateSort.get(0).getSupercategory()!=supercate&&!cateSort.isEmpty()){
                tag=1;
                double falserate=cateSort.get(0).getSupercategoryProbability();
                double rate=truerate/falserate;
                Map arg1 = new HashMap();
                arg1.put("supercategory", cateSort.get(0).getSupercategory());
                List<Category> clist = ccssmg.findequal(arg1, Category.class, "worryClassProbability", new ArrayList());
                rate=Math.pow(rate,1f/clist.get(0).getWordsum());
                for(Word w:clist.get(0).getWord()){
                    if(WordMap.containsKey(w.getText())){
                        Word sw=WordMap.get(w.getText());
                        double probabilityWordcate = w.getPercent()* w.getWeight();
                        double probabilityWord = (float) WordMap.get(w.getText()).getFrequency() / wordSum;
                        double dp1=probabilityWordcate / probabilityWord;
                        probabilityWordcate = sw.getPercent()* sw.getWeight();
                        probabilityWord = (float) WordMap.get(w.getText()).getFrequency() / wordSum;
                        double dps=probabilityWordcate / probabilityWord;
                        if(dp1>dps){
                            w.setWeight((1d-dps/dp1)*rate*w.getWeight());
                        }

                    }
                    Map in=new HashMap();
                    in.put("supercategory",cateSort.get(0).getSupercategory());
                    Map data=new HashMap();
                    data.put("word",cateSort.get(0).getWord());
                    ccssmg.updateMap(in,data,"worryClassProbability");
                }
                cateSort.remove(0);
            }
            if(tag>0){
                System.out.println("数据纠正:"+(++times));
            }else
                times=0;
            i=i-tag;
        }
    }
    Category findCateProbility(int supcate,List<Category> cateSort){
        for(Category c:cateSort){
            if(supcate==c.getSupercategory())
                return c;
        }
        return null;
    }
    Double calculateP(Category s,List<Word> inwordset){

        Integer cateSum = ccssmg.sum("sum", "worryBaseClassProbability");
        Integer supSum = ccssmg.sum("sum", "worryClassProbability");
        Integer wordSum = ccssmg.sum("frequency", "wordClassProbability");
        int supcate = s.getSupercategory()==null?0:s.getSupercategory();
        int supNum = s.getSum();
        Map<String, Word> wordFrequency = new HashMap();
        double probabilitySupcate = 1f;
        Set<Word> wordList = s.getWord();
        inwordset.forEach(r -> {
            Map arg = new HashMap();
            arg.put("text", r.getText());
            List<Word> l = ccssmg.findequal(arg, Word.class, "wordClassProbability", new ArrayList());
            if (!l.isEmpty()) {
                wordFrequency.putIfAbsent(r.getText(), l.get(0));
            }
        });
        
        for (Word ww : wordList) {
            if (wordFrequency.containsKey(ww.getText())) {
                double probabilityWordcate = 1d;
                double probabilityWord = 1d;
                probabilityWordcate *= ww.getPercent()* ww.getWeight();
                probabilityWord *= (float) wordFrequency.get(ww.getText()).getFrequency() / wordSum;
                probabilitySupcate *= probabilityWordcate / probabilityWord * (1-supNum/supSum);
//                System.out.println(ww.getText()+" : "+probabilityWordcate / probabilityWord);
            }

        }
//        System.out.println("result"+" : "+probabilitySupcate);
        return probabilitySupcate;
    }
}
