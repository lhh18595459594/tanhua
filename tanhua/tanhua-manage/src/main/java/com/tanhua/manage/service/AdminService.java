package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AdminMapper;
import com.tanhua.manage.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private static final String CACHE_KEY_CAP_PREFIX = "MANAGE_CAP_";
    public static final String CACHE_KEY_TOKEN_PREFIX = "MANAGE_TOKEN_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 保存生成的验证码
     *
     * @param uuid
     * @param code
     */
    public void saveCode(String uuid, String code) {
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        // 缓存验证码，10分钟后失效
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));
    }

    /**
     * 获取登陆用户信息
     *
     * @return
     */
    public Admin getByToken(String authorization) {
        String token = authorization.replaceFirst("Bearer ", "");

        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;

        String adminString = (String) redisTemplate.opsForValue().get(tokenKey);

        Admin admin = null;

        if (StringUtils.isNotEmpty(adminString)) {
            admin = JSON.parseObject(adminString, Admin.class);
            // 延长有效期 30分钟
            redisTemplate.expire(tokenKey, 30, TimeUnit.MINUTES);
        }
        return admin;
    }

    /**
     * 登陆校验
     *
     * @return
     */
    public Map<String, String> login(Map<String, String> paramMap) {
        //1. 收集前端传过来的数据
        String username = paramMap.get("username");
        String password = paramMap.get("password");
        String verificationCode = paramMap.get("verificationCode");
        // 当用户访问登陆页面时，生成验证码时使用的唯一标识（由前端传过来的）,也随着验证码一起返回给了浏览器，验证码是存入了redis
        //     用的key中就包含了uuid, 这里我们需要从redis取出验证码
        String uuid = paramMap.get("uuid");

        //2. 验证码的校验
        //2.1 取出redis中的验证码
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        // 是否失效
        if (StringUtils.isEmpty(codeInRedis)) {
            throw new BusinessException("验证码已失效，请重新获取!");
        }
        if (!StringUtils.equals(codeInRedis, verificationCode)) {
            throw new BusinessException("验证码不正确，请重新输入!");
        }
        // 2.2如果验证码通过，清除redis中的验证码
        redisTemplate.delete(key);

        //3. 校验用户名与密码是否匹配
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException("用户名或密码不能为空!");
        }


        // 调用mapper通过用户名查询， mybatis-plus对service的封装, api使用过记录到小即可
        Admin adminInDb = query().eq("username", username).one();


        // 密码校验时要先把前端的密码加密再比较
        String encryptedPassword = SecureUtil.md5(password);
        if (null == adminInDb || !StringUtils.equals(encryptedPassword, adminInDb.getPassword())) {
            // null == adminInDb, 数据库不存在这个用户
            // !StringUtils.equals(encryptedPassword, adminInDb.getPassword()) 用户名是存在了，但是密码不一致
            throw new BusinessException("用户名或密码不正确!");
        }
        //4. 校验通过，签发token，设置有效期
        String token = jwtUtils.createJWT(adminInDb.getUsername(), adminInDb.getId());

        // 【注意】存入redis中的token的key必须与拦截器中校验token时取的key(getByToken)必须一致
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;

        String adminJsonString = JSON.toJSONString(adminInDb);
        // 有效期30分钟
        redisTemplate.opsForValue().set(tokenKey, adminJsonString, 30, TimeUnit.MINUTES);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        return tokenMap;
    }

    /**
     * 退出登录
     *
     * @param token
     */
    public void logout(String token) {
        token=token.replace("Bearer ", "");

        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;

        redisTemplate.delete(tokenKey);
    }
}
