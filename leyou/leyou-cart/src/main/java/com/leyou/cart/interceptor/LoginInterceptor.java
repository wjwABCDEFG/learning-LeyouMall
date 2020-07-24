/**
 * @author wjw
 * @date 2020/7/22 15:09
 */
package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * 实现HandlerInterceptor接口所有方法
 * 或者继承HandlerInterceptorAdapter复写其中一些方法，因为HandlerInterceptorAdapter是HandlerInterceptor接口的默认实现类
 * 这个类拦截token并解析，然后设置进ThreadLocal中，贯穿于整个controller,service,mapper用于获取已登录的账户
 */

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;

    //ThreadLocal是线程的一个局部变量，因为在controller,service,mapper中都是同一个线程在处理，所以可以用ThreadLocal，在该线程的任何地方都可以get值
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取cookie中的token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        //解析token，获取当前登录用户信息
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        if (userInfo == null) {
            return false;
        }
        //放入线程局部变量
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    //THREAD_LOCAL是private的，一个类最好只有一个，否则获取不到里面的东西，提供一个静态get方法
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空线程局部变量，因为使用的是tomcat的线程池，线程不会结束而是归还到线程池，也就不会自动释放变量
        THREAD_LOCAL.remove();
    }
}
