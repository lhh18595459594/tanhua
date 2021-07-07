package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtil;
import com.tanhua.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户业务， 调用服务提供者
 */
@Service
@Slf4j
public class UserService {


    @Reference
    private UserApi userApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private UserLikeApi userLikeApi;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private JwtUtils jwtUtils;


    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String redisValidateCodeKeyPrefix;


    /**
     * 通过手机号码查询
     *
     * @param phone
     * @return
     */
    public User findByMobile(String phone) {
        return userApi.findByMobile(phone);
    }

    /**
     * 发送验证码
     *
     * @param phone
     */
    public void sendValidateCode(String phone) {
        // 拼接key
        String key = redisValidateCodeKeyPrefix + phone;
        // 从redis中取出
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        log.info("发送验证码：{}, codeInRedis: {}", phone, codeInRedis);
        // 存在验证码 则报错，未失效
        if (StringUtils.isNotEmpty(codeInRedis)) {
            throw new TanHuaException(ErrorResult.duplicate());
        }
        // 不存在
        // 生成验证码
        String validateCode = "123456";//RandomStringUtils.randomNumeric(6);
        // 调用sms发送
        //huanXinTemplate.sendValidateCode(phone, validateCode);
        //Map<String, String> sendResult = smsTemplate.sendValidateCode(phone, validateCode);
        //if(null != sendResult){
        //    // 发送验证码出错了
        //    throw new TanHuaException(ErrorResult.fail());
        //}
        // 存入redis，设置有效期10分钟
        redisTemplate.opsForValue().set(key, validateCode, 10, TimeUnit.MINUTES);
        log.info("发送验证码成功{},{}", phone, validateCode);
    }

    /**
     * 登陆第二步： 验证码校验
     *
     * @param phone
     * @param verificationCode
     * @return
     */
    public Map<String, Object> loginVerification(String phone, String verificationCode) {
        //1. 拼接key
        String key = redisValidateCodeKeyPrefix + phone;
        //2. 获取redis中的验证码
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        log.info("验证码校验：phone:{}, codeInRedis:{}, verificationCode:{}", phone, codeInRedis, verificationCode);
        if (null == codeInRedis) {
            // 验证码失效了
            throw new TanHuaException(ErrorResult.loginError());
        }

        //3. 用户传过来验证比较
        if (!StringUtils.equals(verificationCode, codeInRedis)) {
            //4. 不相同, 报错
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        //5. 相同 删除redis中的验证码, 删除key，防止重复提交，减少数据库的访问压力
        redisTemplate.delete(key);
        //6. 通过手机号查询用户是否存在
        User user = userApi.findByMobile(phone);
        //7. 用户不存在，注册新用户
        log.info("user={}", user == null ? "null" : "已存在");
        boolean isNew = false;


        String type = "0102"; // 登陆

        if (null == user) {
            isNew = true;  //新用户
            user = new User();
            user.setMobile(phone);
            // 密码加密
            user.setPassword(DigestUtils.md2Hex(phone.substring(5)));
            Long userId = userApi.save(user);
            // 此时user对象有没有id? user对象中的id没有值
            log.debug("user中的id=" + user.getId());
            // 作用：方便下句代码的获取
            user.setId(userId);

            //注册环信帐号
            huanXinTemplate.register(userId);
            type="01";  //注册
        }
        //8. 生成token
        String token = jwtUtils.createJWT(phone, user.getId());
        //9. 把token 作为key, 用户信息为value 存入redis
        String tokenKey = "TOKEN_" + token;
        // 把用户信息转成json字符串
        String userString = JSON.toJSONString(user);
        // token有效期1天，通常7天
        redisTemplate.opsForValue().set(tokenKey, userString, 1, TimeUnit.DAYS);
        //10. 构建返回的数据map(token, isNew)
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", token);
        resultMap.put("isNew", isNew);


        // 添加注册用户的log日志
        // 构建日志数据
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("userId", user.getId());
        msgMap.put("type", type); // 0101 代表着注册
        msgMap.put("log_time", DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        msgMap.put("place", "深圳");
        msgMap.put("equipment", "iphone12");
        // 调用mq发送消息
        rocketMQTemplate.convertAndSend("tanhua_log", JSON.toJSONString(msgMap));
        return resultMap;

    }


    /**
     * 完善用户信息，vo的实体类主要用于与前端数据的交互
     *
     * @param userInfoVo
     */
    public void loginReginfo(UserInfoVo userInfoVo) {
        //1.现在从线程中就可以获取登录用户信息
        User loginUser = UserHolder.getUser();

        //2.通过查出来的user对象获取该id。
        Long loginUserId = loginUser.getId();

        //6.创建后端自己使用的实体类对象。
        UserInfo userInfo = new UserInfo();

        //7.复制属性值，把参数1复制到参数2，属性名及类型必须一致，如果不一致则复制失败
        //因为userInfoVo主要与前端数据交互，而查询完整的用户信息，则交给userInfo这个实体类去数据库查询
        BeanUtils.copyProperties(userInfoVo, userInfo);

        //8.再将第5步，通过redis中查出来的用户的信息，再获取Id，并设置到userInfo中
        userInfo.setId(loginUserId);

        //9.调用api方法添加用户信息
        userInfoApi.add(userInfo);
    }

    /**
     * 完善用户信息，上传头像
     *
     * @param headPhoto
     */
    public void updateUserAvatar(MultipartFile headPhoto) {
        //1.调用封装方法，来获取登录用户
        //User loginUser = getUserByToken(token);
        //1.现在从线程中就可以获取登录用户信息
        User loginUser = UserHolder.getUser();
        // 人脸检测
        try {
            if (!faceTemplate.detect(headPhoto.getBytes())) {
                throw new TanHuaException(ErrorResult.faceError());
            }
            // 上传到oss
            String url = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
            // 调用api来更新用户的头像
            UserInfo userInfo = new UserInfo();
            userInfo.setId(loginUser.getId());
            userInfo.setAvatar(url);
            userInfoApi.update(userInfo);
        } catch (IOException e) {
            throw new TanHuaException("上传头像失败");
        }
    }


    /**
     * 查看登陆用户信息
     *
     * @return
     */
    public UserInfoVo getLoginUserInfo() {
        //1.现在从线程中就可以获取登录用户信息
        User loginUser = UserHolder.getUser();

        // 调用api查询用户详情
        UserInfo userInfo = userInfoApi.findById(loginUser.getId());

        //转成vo
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);

        return userInfoVo;
    }

    /**
     * 更新用户信息
     *
     * @param userInfoVo
     */
    public void updateUserInfo(UserInfoVo userInfoVo) {
        //获取token中的用户信息，并且获取用户的id
        //Long loginUserId = getUserByToken(token).getId();

        //1.现在从线程中就可以获取登录用户信息
        User loginUser = UserHolder.getUser();

        // 转成pojo
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoVo, userInfo);

        //计算年龄
        userInfo.setAge(GetAgeUtil.getAge(userInfoVo.getBirthday()));

        //调用更新方法(复用)
        userInfoApi.update(userInfo);
    }

    /**
     * 更新用户头像
     *
     * @param headPhoto
     */
    public void updateHeadPhoto(MultipartFile headPhoto) {
        //1.根据token查找存在的用户的id
        //Long loginUserId = getUserByToken(token).getId();

        //1.现在从线程中就可以获取登录用户信息
        Long loginUserId = UserHolder.getUser().getId();

        // 2.调用方法查询用户详情
        UserInfo userInfo = userInfoApi.findById(loginUserId);

        /*
        这里必须先查询出旧头像的地址，不能放在更新头像完成之后。不然更新头像以后获取的是新地址
         */
        //获取旧的头像地址
        String oldAvatar = userInfo.getAvatar();

        //3.上传头像
        // 人脸检测
        try {
            if (!faceTemplate.detect(headPhoto.getBytes())) {
                throw new TanHuaException(ErrorResult.faceError());
            }
            log.info("人脸识别成功");
            // 上传头像到oss
            String url = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
            log.info("图片上传成功了....");

            // 调用api来更新用户的头像
            userInfo = new UserInfo();
            userInfo.setId(loginUserId);
            userInfo.setAvatar(url);
            userInfoApi.update(userInfo);
            log.info("更新用户头像成功了....");

            //4.删除旧的头像
            ossTemplate.deleteFile(oldAvatar);
            log.info("删除阿里云头像成功了....");
        } catch (IOException e) {
            throw new TanHuaException("上传头像失败");
        }


    }

    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     * 我的页面中的统计
     *
     * @return
     */
    public CountsVo counts() {
        //1.获取登录用户Id
        Long loginUserId = UserHolder.getUserId();

        //2.调用api，查询跟登录用户相互喜欢的好友个数，统计好友数
        Long eachLoveCoun = userLikeApi.countLikeEachOther(loginUserId);

        //3.调用api，统计我喜欢的用户，统计个数
        Long loveCount = userLikeApi.countOneSideLike(loginUserId);

        //4.调用api，统计我的粉丝
        Long fansCount = userLikeApi.countFens(loginUserId);

        //5.构建vo
        CountsVo countsVo = new CountsVo();

        //6.赋值
        countsVo.setEachLoveCount(eachLoveCoun);
        countsVo.setLoveCount(loveCount);
        countsVo.setFanCount(fansCount);

        return countsVo;
    }

    /**
     * 相互喜欢、我喜欢、粉丝列表分页查询
     *
     * @param type     1 互相关注
     *                 2 我关注
     *                 3 粉丝
     *                 4 谁看过我
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<FriendVo> queryUserLikeList(int type,Long page, Long pageSize,String nickname) {
        //1.获取登录用户的id
        Long loginUserId = UserHolder.getUserId();

        PageResult pageResult = null;
        boolean alreadyLove = false;
        switch (type) {
            case 1:
                //分页查询相互喜欢的
                pageResult = userLikeApi.findPageLikeEachOther(loginUserId, page, pageSize);
                alreadyLove = true;
                break;

            case 2:
                //分页查询我喜欢的
                pageResult = userLikeApi.findPageOneSideLike(loginUserId, page, pageSize);
                alreadyLove = true;
                break;
            case 3:
                //分页查询粉丝列表
                pageResult = userLikeApi.findPageFens(loginUserId, page, pageSize);
                alreadyLove = false;
                break;
            case 4:
                //分页查询访客列表
                pageResult = userLikeApi.findPageMyVisitors(loginUserId, page, pageSize);
                break;
            default:
                break;

        }
        // 补全用户信息 RecommendUser
        // 获取分页结果集
        List<RecommendUser> recommendUserList = pageResult.getItems();

        if (!CollectionUtils.isEmpty(recommendUserList)) {
            //取出所有人的id
            List<Long> userIds = recommendUserList.stream().map(RecommendUser::getUserId).collect(Collectors.toList());

            //批量查询，查出所有人的信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(userIds);

            //转成map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

            //遍历所有人信息,转成vo
            List<FriendVo> voList = recommendUserList.stream().map(recommendUser -> {
                //构建vo
                FriendVo friendVo = new FriendVo();

                // 复制用户信息
                Long userId = recommendUser.getUserId();
                UserInfo userInfo = userInfoMap.get(userId);

                BeanUtils.copyProperties(userInfo, friendVo);

                //补全信息
                //设置缘分值
                friendVo.setMatchRate(recommendUser.getScore().intValue());

                //是否喜欢过
                friendVo.setAlreadyLove(true);

                return friendVo;
            }).collect(Collectors.toList());

            pageResult.setItems(voList);

        }
        return pageResult;
    }

    /**
     * 粉丝中的喜欢
     *
     * @param fansId
     */
    public void fansLike(Long fansId) {

        //1.获取登录用户的Id
        Long loginUserId = UserHolder.getUserId();

        // 登陆用户来喜欢粉丝
        Boolean flag = userLikeApi.fansLike(loginUserId, fansId);

        if (flag) {
            //喜欢成功，在环信上注册为好友
            huanXinTemplate.makeFriends(loginUserId, fansId);
        }
    }
}
