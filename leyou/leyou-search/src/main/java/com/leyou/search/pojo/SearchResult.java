/**
 * @author wjw
 * @date 2020/7/16 0:20
 */
package com.leyou.search.pojo;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;
import java.util.Map;

//发现PageResult<Goods>无法获取搜索可选项的数据而进行的扩展
public class SearchResult extends PageResult<Goods> {

    //分类写成这样而不是List<Category>是因为Category中含有太多不必要的条件，而我们只需要id和name
    //至于为什么是map，是因为实体类被序列化成json后的样子其实就是map
    private List<Map<String, Object>> categories;

    private List<Brand>brands;

    private List<Map<String, Object>> specs;

    public SearchResult() {
    }

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Map<String, Object>> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public List<Map<String, Object>> getCategories() {
        return categories;
    }

    public void setCategories(List<Map<String, Object>> categories) {
        this.categories = categories;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

    public List<Map<String, Object>> getSpecs() {
        return specs;
    }

    public void setSpecs(List<Map<String, Object>> specs) {
        this.specs = specs;
    }
}
