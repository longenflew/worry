/**
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.unicom.betterworry.being;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 词语实体类
 **/
public class Word implements Comparable {
    private String text; //词语的文本
    private Integer frequency=1; //词语出现的频率
    private Double percent; //词语出现的概率
    private Word nextWord; //词语的下一个词语
    private String wordform; //词语的词性
    private List<Integer> location = null; //词语在语句中的位置
    //权重，用于词向量分析
    private Double weight;
    public Word(){}
    public Word(String text) {
        this.text = text;
    }
    public Word(String text,Integer frequency) {
        this.text = text;
        this.frequency=frequency;
    }
    public Word(String text,String wordform) {
        this.text = text;
        this.wordform=wordform;
    }
    public Word(String text,Integer frequency,String wordform) {
        this.text = text;
        this.wordform = wordform;
    }
        public String getWordform() {
        return wordform;
    }

    public void setWordform(String wordform) {
        this.wordform = wordform;
    }

    public Word getNextWord() {
        return nextWord;
    }

    public void setNextWord(Word nextWord) {
        this.nextWord = nextWord;
    }

    public void setLocation(List<Integer> location) {
        this.location = location;
    }

    public void addLocation(Integer location) {
        this.location=this.location==null?new ArrayList<Integer>():this.location;
        this.location.add(location);
        frequency = this.location.size();
    }

    public List<Integer> getLocation() {
        return location;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.text);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        return Objects.equals(this.text, other.text);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (text != null) {
            str.append(text);
        }

        if (frequency > 0) {
            str.append("/").append(frequency);
        }
        if(wordform!=null){
            str.append("/").append(wordform);
        }
        return str.toString();
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (this.text == null) {
            return -1;
        }
        if (o == null) {
            return 1;
        }
        if (!(o instanceof Word)) {
            return 1;
        }
        String t = ((Word) o).getText();
        if (t == null) {
            return 1;
        }
        return this.text.compareTo(t);
    }
}
