package com.unicom.betterworry;

import com.unicom.betterworry.being.Category;
import com.unicom.betterworry.being.Word;
import com.unicom.betterworry.being.WorryItem;
import com.unicom.betterworry.dao.CategoryClassfiyMongo;
import com.unicom.betterworry.dao.WorryMapper;
import com.unicom.betterworry.dao.WorryMongo;
import com.unicom.betterworry.nexus.WordKnife;
import com.unicom.betterworry.nexus.method.CategoryClassifyBayesian;
import com.unicom.betterworry.service.WorryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@SpringBootTest
class BetterworryApplicationTests {

    @Autowired
    MongoTemplate mongo;
    @Autowired
    WorryMongo worryMongo;
    @Autowired
    WorryService worrysvc;
    @Autowired
    CategoryClassifyBayesian ccss;
    @Autowired
    WorryMapper workMapper;
    @Autowired
    private CategoryClassfiyMongo ccssmg;

    @Test
    void contextLoads() {
        String regex="^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$|^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$";
        System.out.print("32062119971030301x".matches(regex));
    }

    @Test
    void test(){
        ReadWriteLock rwl=new ReentrantReadWriteLock();
//        rwl.readLock().lock();
//        System.out.println("sb1");
//        rwl.readLock().lock();
//        System.out.println("sb2");
        rwl.writeLock().lock();
        System.out.println("sb3");
//        rwl.writeLock().unlock();
        rwl.readLock().lock();
        System.out.println("sb4");
    }

    @Test
    void testWorryMongo(){
//        String ticket="axqyuysfmebadnqskmagyuimqclumeyroedvnxgahcicgeyjxcfbkvydaewqoanj";
//        System.out.println(worryMongo.findWorryListFatherTicket(ticket).toString());
//        System.out.println(worryMongo.findWorryListSonTicket(ticket).toString());
        worryMongo.findWorryAllWord(923369356,"axqyuysfmebadnqskmagyuimqclumeyroedvnxgahcicgeyjxcfbkvydaewqoanj");
    }

    @Test
    void testGuess() throws SQLException {
        worrysvc.guessWorryCategory(Arrays.asList(844422903));
    }

    @Test
    void testSeg() {
        Scanner scanner = new Scanner(System.in);
        String str=scanner.nextLine();
        System.out.println(WordKnife.segWord(str));
    }

    @Test
    void testClassfiy() throws SQLException {
//        worrySegWord();
//        List<Word> words=new ArrayList<>();
//        ccss.caculateSample("worryBaseClassProbability");
//        ccss.caculateSample("worryClassProbability");
//        ccss.caculateWeight("worryBaseClassProbability");
//        ccss.caculateWeight("worryClassProbability");
//        ccss.caculateWordSample();
        Category c = ccss.classify("取消5G升级包 15618538238 用户通过手厅APP自行操作取消5G升级包49元20G（次月生效）+优惠购益选包（5G升级专享合约版），但一直无法操作成功。经CB查询是因15618538238号码有未竣工订单，我处无法取消，烦请帮用户取消此套餐");
        System.out.println(c.getCategorytype() + " " + c.getSupercategory());
    }

    @Test
    void testTrain() throws SQLException {
        String sql = "select content,processTitle,c_worry.processId,categorytype,supercategory from c_worry,d_worry_item where c_worry.processId=d_worry_item.processId and supercategory>100  ";
        List<Map<String, Object>> rl = workMapper.find(sql, null);
        int sum = 0, correctsum = 0;
        Long time = System.currentTimeMillis();
        List<String> tl=new ArrayList();
        rl.forEach(r->{
            ccss.trainSample(Arrays.asList(r.get("processTitle")+" "+r.get("content")), (Integer) r.get("supercategory"));
        });
    }

    @Test
    void testClassfiyCorrect() throws SQLException {
        String sql = "select content,processTitle,c_worry.processId,categorytype,supercategory from c_worry,d_worry_item where c_worry.processId=d_worry_item.processId and supercategory>100 limit 200";
        List<Map<String, Object>> rl = workMapper.find(sql, null);
        int sum = 0, correctsum = 0;
        Long time = System.currentTimeMillis();
        for (Map t : rl) {

            System.out.println(t.get("processTitle") + " " + t.get("content"));
            Category c = ccss.classify(t.get("processTitle") + " " + t.get("content"));
            if (c.getSupercategory().toString().equals(t.get("supercategory").toString()))
                correctsum++;
            else
                System.out.println("false:"+t.get("processId"));
            sum++;
            System.out.println(c.getSupercategory() + " " + t.get("supercategory"));
            System.out.println(sum + " " + correctsum + " " + (float) correctsum / sum);
        }
        System.out.println(System.currentTimeMillis() - time);

    }

    @Test
    public void caculateWordsum() {
        List<Map> words = worryMongo.findequal(new HashMap(), "worryClassProbability", Arrays.asList("word"));
        Map<String, AtomicInteger> wordmap = new HashMap();
        words.forEach(lw -> ((List<Word>) lw.get("word")).forEach(w -> {
            wordmap.putIfAbsent(w.getText(), new AtomicInteger());
            wordmap.get(w.getText()).addAndGet(w.getFrequency());
        }));
        words.forEach(lw -> {
            Map in = new HashMap();
            in.put("_id", lw.get("_id"));
            Map data = new HashMap();
            data.put("wordsum", ((List<Word>) lw.get("word")).stream().mapToInt(Word::getFrequency).sum());
            worryMongo.updateMap(in, data, "worryClassProbability");
        });
        //System.out.println(words);
    }

    @Test
    void testBatchClassfiyCorrect() throws SQLException {
        String sql = "select content,processTitle,c_worry.processId,categorytype,supercategory from c_worry,d_worry_item where c_worry.processId=d_worry_item.processId limit 100";
        List<Map<String, Object>> rl = workMapper.find(sql, null);
        List<String> contents = rl.stream().map(s -> s.get("processTitle") + " " + s.get("content")).collect(Collectors.toList());
        int sum = rl.size(), correctsum = 0;
        Long time = System.currentTimeMillis();

        List<Category> categoryList = ccss.classifyBatch(contents);
        System.out.println(System.currentTimeMillis() - time);
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getSupercategory().toString().equals(rl.get(i).get("supercategory").toString()))
                correctsum++;
            System.out.println(sum + " " + correctsum + " " + (float) correctsum / sum);
        }

    }


    @Test
    void testSegword() throws IOException {
        String fileContent = "";
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tempdic.txt");
        InputStreamReader insread = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bfreader = new BufferedReader(insread);
        String line;
        Set<String> newword = new HashSet();
        while ((line = bfreader.readLine()) != null) {
            WordKnife.segWord(line).forEach(w -> {
                if (w.getWordform().equals("nw")) {
                    newword.add(w.getText());
                }
            });
        }
        File file = new File("worryLibrary.dic");
        FileWriter fw = new FileWriter(file);
        newword.forEach(w -> {
            try {
                fw.write(w + " " + "wrw 1000\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fw.close();
    }

    @Test
    public void findDistinctSupcate() {
        Query query = new Query();
        query.fields().include("supercategory");
        Criteria criteria = Criteria.where("supercategory").gt(100);
        query.addCriteria(criteria);
        List t = mongo.findDistinct(query, "supercategory", "worryItem", Integer.class);
        System.out.println(t.toString());
    }


    @Test
    void testFindworry() {
        List<WorryItem> woit = findWorryAllWord(1615549962, Arrays.asList(1615549962));
        System.out.print(woit);
    }

    public List<WorryItem> findWorryAllWord(int processId, List<Integer> pids) {
        Map arg = new HashMap();
        arg.put("processId", processId);
        List<Map<String, Object>> worry = worryMongo.findequal(arg, "worryItem");
        List<Map<String, Object>> words = (List<Map<String, Object>>) worry.get(0).get("contentSPword");
        Query query = new Query();
        Criteria criteria = Criteria.where("contentSPword.text").in(words.stream().map(w -> w.get("text")).collect(Collectors.toList()));
        criteria.and("processId").in(pids);
        query.addCriteria(criteria);
        return mongo.find(query, WorryItem.class, "worryItem");
    }

    @Test
    public void updateWordSample() throws SQLException {
        ccss.updateWordSample();
    }

    @Test
    public void worrySegWord() throws SQLException {
        String sql = "select content,processTitle,c_worry.processId,supercategory,categorytype from c_worry inner join d_worry_item on d_worry_item.processId=c_worry.processId where supercategory>100";
        List<Map<String, Object>> res = workMapper.find(sql, null);
        res.forEach(r -> {
            Map data = new HashMap();
            data.put("contentSPword", WordKnife.segWordPurely(r.get("content").toString() + r.get("processTitle")));
            Map in = new HashMap();
            in.put("processId", r.get("processId"));
            data.put("supercategory", r.get("supercategory"));
            data.put("categorytype", r.get("categorytype"));
            worryMongo.updateMap(in, data, "worryItem");
        });
    }

    @Test
    public void updateWorryClassProbability(){
        Category category=new Category();
        category.setSupercategory(365);
        category.setWord(new HashSet<Word>(Arrays.asList(new Word("处理"),new Word("报错"))));
        String collection="worryTestClassProbability";
        Query query = new Query();
        Criteria criteria = null;
        Update update = new Update();
        if(category.getSupercategory()!=null){
            criteria=Criteria.where("supercategory").is(category.getSupercategory());
            query.addCriteria(criteria);
            update.inc("sum");
            update.inc("wordsum",category.getWordsum());
            System.out.println(mongo.upsert(query,update,collection));
            category.getWord().forEach(w->{
                Criteria cr=Criteria.where("supercategory").is(category.getSupercategory());
                cr.and("word.text").is(w.getText());
            });
        }
    }


}
