/**
 * @author wjw
 * @date 2020/7/14 14:57
 */
package com.leyou.elasticsearch.test;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;    //用于创建索引和映射

    @Autowired
    private GoodsRepository goodsRepository;                //用于操作文档(每个条目都是Goods)

    @Autowired
    private SearchService searchService;                    //用于将Spu转换成Goods

    @Autowired
    private GoodsClient goodsClient;                        //用于从数据库获得Spu

    @Autowired
    private CategoryClient categoryClient;                        //用于从数据库获得Spu

    /**
     * 第一次执行才需要导入，所以只需写个测试用例
     */
    @Test
    public void test(){
        //创建索引库和映射
        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);

        //分批查询和插入数据
        Integer page = 1;
        Integer rows = 100;
        do {
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(null, null, page, rows);
            List<SpuBo> items = result.getItems();
            //将List<SpuBo>转变成List<Goods>
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            //插入索引库
            this.goodsRepository.saveAll(goodsList);
            rows = items.size();
            page++;
        }while (rows == 100);
    }
}
