package com.unicom.betterworry.nexus;


import com.unicom.betterworry.being.Word;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.*;
import java.util.stream.Collectors;

/*
切词工具，用的ansj切词，基于词典的中文分词
 */
public class WordKnife {
    static Set<String> formset;
    static {
        formset = new HashSet(Arrays.asList("z", "y", "yg", "x", "vd", "w", "u", "Tg","t"
                , "q", "p", "o", "r", "Rg", "ns", "m", "Mg", "k", "h", "f", "e", "d", "Dg", "c", "mq", "null"));
    }
    /*
     普通切词方法
             */
    public static List<Word> segWord(String str) {

        Map<String, Word> wordmap = new LinkedHashMap();
        ToAnalysis.parse(str).forEach(s -> {
            if (s.getName().length() > 1) {
                wordmap.putIfAbsent(s.getName(), new Word(s.getName(), s.getNatureStr()));
                Word w = wordmap.get(s.getName());
                w.setFrequency(w.getFrequency() + 1);
            }
        });
        return new ArrayList(wordmap.values());
    }

    /*
    纯净的切词方法，可获取纯粹的工单关键词
     */
    public static List<Word> segWordPurely(String str){
        return segWord(str).stream().filter(s -> !formset.contains(s.getWordform()) && s.getText().length() > 1).collect(Collectors.toList());
    }
    /*
        带用词在句中位置的切词方法
         */
    public static List<Word> segWordWithLocation(String str){
        Map<String, Word> wordmap = new LinkedHashMap();
        Result terms= ToAnalysis.parse(str);
        int i=0;
        for(Term t:terms.getTerms()){
            if(!t.getNatureStr().equals("null")){
            wordmap.putIfAbsent(t.getName(),new Word(t.getName(),t.getNatureStr()));
            Word w = wordmap.get(t.getName());
            w.setFrequency(w.getFrequency() + 1);
            w.addLocation(i);
            i++;
            }
        }
        return new ArrayList(wordmap.values());
    }
    /*
        带用词在句中位置的纯净切词方法
         */
    public static List<Word> segWordPurelyWithLocation(String str){
        return segWordWithLocation(str).stream().filter(s -> !formset.contains(s.getWordform()) && s.getText().length() > 1).collect(Collectors.toList());
    }


    public static void main(String args[]) throws Exception {
        List<Term> parse = DicAnalysis.parse("携号转网").getTerms();
        System.out.println(parse);
    }
}
