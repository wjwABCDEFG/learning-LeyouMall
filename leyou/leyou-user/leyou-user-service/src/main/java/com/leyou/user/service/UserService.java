/**
 * @author wjw
 * @date 2020/7/18 15:58
 */
package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "user:verify:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 校验数据是否未被占用
     * @param data 要校验的数据
     * @param type 要校验的数据类型：1，用户名；2，手机；
     * @return bool可用不可用
     */
    public Boolean checkUser(String data, Integer type) {
        User user = new User();
        if (type == 1){
            user.setUsername(data);
        }else if (type == 2){
            user.setPhone(data);
        }else {
            return null;
        }
        return this.userMapper.selectCount(user) == 0;  //为0说明未被占用
    }

    public void sendVerifyCode(String phone) {
        if (StringUtils.isBlank(phone)){
            return;
        }
        //生成6位验证码
        String code = NumberUtils.generateCode(6);

        //发送消息到rabbitMQ
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE", "verifycode.sms", msg);
        //把验证码保存到redis
        this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
    }

    public boolean register(User user, String code) {
        //校验验证码
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code, redisCode)){
            return false;
        }
        //生成随机眼
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加盐加密用户密码
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        //新增用户
        user.setId(null);
        user.setCreated(new Date());
        this.userMapper.insertSelective(user);
        //删除redis中的验证码
        this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
        return true;
    }

    public User queryUser(String username, String password) {
        User user = new User();
        user.setUsername(username);

        //密码进行一次加盐加密后再和数据库对比
        User record = this.userMapper.selectOne(user);
        if (record == null) {
            return null;
        }
        String md5Password = CodecUtils.md5Hex(password, record.getSalt());
        if (StringUtils.equals(md5Password, record.getPassword())){
            return record;
        }
        return null;
    }
}
