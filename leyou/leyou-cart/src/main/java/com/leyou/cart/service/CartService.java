/**
 * @author wjw
 * @date 2020/7/22 16:46
 */
package com.leyou.cart.service;

import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.api.GoodsApi;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;  //StringRedisTemplate编解码比较统一

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "user:cart:";

    public void addCart(Cart cart) {

        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查询购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        //判断当前商品是否在购物车中，在修改数量，不在新增
        String key = cart.getSkuId().toString();
        if (hashOperations.hasKey(key)){
            Cart record = JsonUtils.parse(hashOperations.get(key).toString(), Cart.class);
            record.setNum(record.getNum() + cart.getNum());
            hashOperations.put(key, JsonUtils.serialize(record));
        }else{
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setUserId(userInfo.getId());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setPrice(sku.getPrice());
            hashOperations.put(key, JsonUtils.serialize(cart));
        }
    }

    public List<Cart> queryCarts() {
        //从拦截器中获得用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查找redis中该用户信息的购物车
        if (!this.redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())){
            return null;
        }
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        //redis里面的结构类似于Map<userId, Map<skuId, Cart(json字符串形式)>>，hashOperations就是Map<skuId, Cart(json字符串形式)>
        List<Object> cartsJson = hashOperations.values();
        if (CollectionUtils.isEmpty(cartsJson)){
            return null;
        }
        //将List<Object>转换为List<Cart>
        return cartsJson.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(), Cart.class)).collect(Collectors.toList());
    }

    public void updateNum(Cart cart) {
        //从拦截器中获得用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查找redis中该用户信息的购物车
        if (!this.redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())){
            return ;
        }
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        int num = cart.getNum();
        cart = JsonUtils.parse(hashOperations.get(cart.getSkuId().toString()).toString(), Cart.class);
        //更新
        cart.setNum(num);
        hashOperations.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }

    public void deleteCart(String id) {
        //从拦截器中获得用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查找redis中该用户信息的购物车
        if (!this.redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())){
            return ;
        }
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        hashOperations.delete(id);
    }
}
