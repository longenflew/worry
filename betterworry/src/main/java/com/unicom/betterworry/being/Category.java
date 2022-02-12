package com.unicom.betterworry.being;

import java.util.Objects;
import java.util.Set;

/**
 * 分类属性实体类
 **/
public class Category {
    private Integer categorytype; //基本类别
    private Double categoryProbability; //基本类别的概率
    private Integer supercategory; //高级类别
    private Double supercategoryProbability; //高级类别的概率
    private Integer sum=0; //该类别的数量
    private Integer wordsum=0; //类别中出现的词语总数
    private Set<Word> word;//类别下出现的词语
    private Set<String> department; //类别下出现的部门

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public Set<Word> getWord() {
        return word;
    }

    public void setWord(Set<Word> word) {
        this.word = word;
    }

    public Set<String> getDepartment() {
        return department;
    }

    public void setDepartment(Set<String> department) {
        this.department = department;
    }

    public Integer getCategorytype() {
        return categorytype;
    }

    public void setCategorytype(Integer categorytype) {
        this.categorytype = categorytype;
    }

    public Double getCategoryProbability() {
        return categoryProbability;
    }

    public void setCategoryProbability(Double categoryProbability) {
        this.categoryProbability = categoryProbability;
    }

    public Integer getSupercategory() {
        return supercategory;
    }

    public void setSupercategory(Integer supercategory) {
        this.supercategory = supercategory;
    }

    public Double getSupercategoryProbability() {
        return supercategoryProbability;
    }

    public void setSupercategoryProbability(Double supercategoryProbability) {
        this.supercategoryProbability = supercategoryProbability;
    }

    public Integer getWordsum() {
        return wordsum;
    }

    public void setWordsum(Integer wordsum) {
        this.wordsum = wordsum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categorytype, category.categorytype) && Objects.equals(categoryProbability, category.categoryProbability) && Objects.equals(supercategory, category.supercategory) && Objects.equals(supercategoryProbability, category.supercategoryProbability) && Objects.equals(sum, category.sum) && Objects.equals(word, category.word) && Objects.equals(department, category.department);
    }

    @Override
    public int hashCode() {
        if(supercategory == null&&categorytype==null)
            return Objects.hash(null);
        else if(supercategory == null){
            return Objects.hash(categorytype);
        }
        else if(categorytype == null){
            return Objects.hash(supercategory);
        }
        return Objects.hash(null);
    }
}
