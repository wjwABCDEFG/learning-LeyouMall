/**
 * @author wjw
 * @date 2020/7/16 19:37
 */
package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    public Map<String, Object> loadData(Long spuId){
        Map<String, Object> model = new HashMap<>();
        //根据spuId获取spu，渲染商品信息，标题，副标题等
        Spu spu = this.goodsClient.querySpuById(spuId);

        //查询spuDetail，渲染商品信息，宣传图，通用参数（cpu屏幕大小等），特殊参数（颜色内存储存等）用于用户购买选择
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spuId);

        //查询分类，渲染左上角面包屑
        //由于可以点击，点击时候应该是带id，因此不只要name，还要id，因此采用Map<String, Object>
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        //初始化一个分类Map
        List<Map<String, Object>> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", names.get(i));
            categories.add(map);
        }

        //查询品牌，渲染左上角面包屑
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //skus，渲染价格，sku展示图片等
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spuId);

        //查询规格参数组，渲染商品详情的参数组（比如芯片信息）和组内信息（cpu型号，cpu频率）
        List<SpecGroup> groups = this.specificationClient.queryGroupsWithParam(spu.getCid3());

        //查询特殊规格参数，上面的spuDetail只含有"4":["白色","金色","玫瑰金"],注意这个key，页面上应该显示的是"颜色":["白色","金色","玫瑰金"]
        //因此这个是要Map<Long, String> 就是到时候可以通过4这个key获取到"颜色"这个value
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, null);
        //初始化特殊规格参数map
        Map<Long, String> paramMap = new HashMap<>();
        Map<Long, String> genericMap = new HashMap<>();
        params.forEach(param -> {
            if (!param.getGeneric()){
                paramMap.put(param.getId(), param.getName());
            }else {
                genericMap.put(param.getId(), param.getName());
            }
        });

        //其实分析完前端需要什么格式后先写的是这里，确定好需要的数据之后再分别获取
        model.put("spu", spu);
        model.put("spuDetail", spuDetail);
        model.put("categories", categories);
        model.put("brand", brand);
        model.put("skus", skus);
        model.put("groups", groups);
        model.put("paramMap", paramMap);
        model.put("genericMap", genericMap);

        return model;
    }
}
