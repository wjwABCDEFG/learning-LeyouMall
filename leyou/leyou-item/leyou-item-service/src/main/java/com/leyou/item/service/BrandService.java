/**
 * @author wjw
 * @date 2020/6/28 1:44
 */
package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
@Transactional
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据查询条件分页查询品牌信息
     * @param key 用户在搜索框中输入的值
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        //key可以作为name模糊查询，或者作为首字母查询
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name", "%"+key+"%").orEqualTo("letter", key);  //第一个参数是数据库的列名
        }
        //添加分页条件
        PageHelper.startPage(page, rows);
        //添加排序条件
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));  //用这个setOrderByClause更方便些，参数是一个字符串例如"列名 desc"，注意中间的空格
        }

        List<Brand> brands = this.brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    public void saveBrand(Brand brand, List<Long> cids) {
        //brand插入brand表
        int brandId = this.brandMapper.insertSelective(brand);
//        System.out.println(brandId);
        //cids和插入后的brand_id插入中间表
        for (Long cid : cids) {
            this.brandMapper.insertCategoryAndBrand(cid, brand.getId());
        }
    }

    public List<Brand> queryBrandsByCid(Long cid){
        return this.brandMapper.queryBrandsByCid(cid);
    }

    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
