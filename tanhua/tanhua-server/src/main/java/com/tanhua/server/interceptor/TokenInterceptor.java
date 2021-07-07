package com.tanhua.server.interceptor;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.domain.db.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;


/**
 * 自定义token拦截器，统一的token认证处理，打印日志
 */

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("进入了TokenInterceptor拦截器......");

        //1.记录用户访问的url地址
        log.info("用户访问了：" + request.getRequestURL());

        //2.判断用户是否登陆过，token是否存在
        String token = request.getHeader("Authorization");
        System.out.println(token);
        //3.如果token不为空，获取登录用户信息，且存入Threadlocal里,放行，返回true
        if (StringUtils.isNotEmpty(token)) {
            User loginUser = getUserByToken(token);
            if (null != loginUser) {
               //不为空，说明redis中有值，在有效期内登陆过
                //存入Threadlocal
                UserHolder.setUser(loginUser);
                return true;
            }

        }


        //4.如果不存在，报401错误,拦截，返回false
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }


    /**
     * 封装获取token的方法
     *
     * @param token
     * @return
     */
    public User getUserByToken(String token) {
        String tokenKey = "TOKEN_" + token;
        String userJsonString = ((String) redisTemplate.opsForValue().get(tokenKey));
        if (null == userJsonString) {
            return null;
        }
        // 延长有效期，续期
        redisTemplate.expire(tokenKey, 7, TimeUnit.DAYS);
        // 获取登陆用户
        User loginUser = JSON.parseObject(userJsonString, User.class);
        return loginUser;
    }
}
