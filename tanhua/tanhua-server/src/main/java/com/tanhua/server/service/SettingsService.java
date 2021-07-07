package com.tanhua.server.service;


import com.tanhua.commons.Const.RedisKeyConst;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.*;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.*;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SettingsService {

    @Reference
    private SettingsApi settingsApi;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private BlackListApi blackListApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private UserApi userApi;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 读取用户的通用设置,其中包括 【设置陌生人问题】 和【通知设置】
     *
     * @return
     */
    public SettingsVo querySettings() {
        //1.获取登录用户信息
        User loginUser = UserHolder.getUser();

        //2.调用questionApi通过用户id查询陌生人问题表,获取txt
        Question question = questionApi.findByUserId(loginUser.getId());

        //3.调用SesettingsApi通过用户id查询设置表tb_settings
        Settings settings = settingsApi.findByUserId(loginUser.getId());

        //4.创建SettingsVo,把settings值复制给settingsVo
        SettingsVo settingsVo = new SettingsVo();

        if (null != settings) {
            //5.用户设置了才复制，没设置则使用默认值
            BeanUtils.copyProperties(settings, settingsVo);
        }

        //6.判断用户是否设置了陌生人问题
        if (null != question) {
            //7.如果设置了，则把获取到的值，设置到settingsVo中
            settingsVo.setStrangerQuestion(question.getTxt());
        }

        //7.获取手机号
        settingsVo.setPhone(loginUser.getMobile());

        //8.获取id
        settingsVo.setId(loginUser.getId().intValue());
        return settingsVo;
    }


    /**
     * 更新和添加【通知设置】
     *
     * @param settingsVo
     */
    public void updateNotification(SettingsVo settingsVo) {
        //1.从线程池中获取用户对象的id
        Long userId = UserHolder.getUserId();

        System.out.println("登录用户id：=========================="+userId);
        //2.调用api方法查出该对象的旧信息
        Settings settingsInDB = settingsApi.findByUserId(userId);

        //3.创建与数据库交互的Settings对象
        Settings settings = new Settings();

        //4.将从前端带过来的settingsVo里的数据复制，到settings中
        BeanUtils.copyProperties(settingsVo, settings);

        /*
           因为前端传过来的settingsVo中只有 【likeNotification】、【pinglunNotification】、【gonggaoNotification】三个数据
           没有Id,所以需要把查出来的id，设置到settings中
         */
        //5.查出来的用户id设置到settings中
        settings.setUserId(userId);

        //6.对之前获取的旧信息判断是否存在于数据库，如果该对象已经存在，则更新
        if (null != settingsInDB) {
            settingsApi.update(settings);
        } else {
            //如果该对象不存在于数据库，则添加
            settingsApi.add(settings);
        }
    }


    /**
     * 分页查询黑名单列表
     * 请求参数：
     * Query参数：
     * page：当前页
     * pagesize：每页查询条数
     */
    public PageResult<UserInfoVoAge> blackList(Long page, Long pageSize) {
        //1.获取登录用户的Id
        Long loginUserId = UserHolder.getUserId();

        //2.分页查询登录用户的所有黑名单
        PageResult pageResult = blackListApi.fingPage(loginUserId, page, pageSize);

        //3.通过分页查询，查询出tb_black_list表的数据集合items
        List<BlackList> items = pageResult.getItems();

        //4.判断集合是否为空，不为空则继续
        if (!CollectionUtils.isEmpty(items)) {
              /*
              相当于：
            List<Long> ids=new ArrayList<>();
            for (BlackList item : items) {
                ids.add(item.getBlackUserId());
            }
            */
            //5.上面查出来的集合数据items,实际上是tb_black_list表的数据集合，还需要把黑名单black_user_id拿出来，组成一个list集合
            List<Long> blackUserIds = items.stream().map(BlackList::getBlackUserId).collect(Collectors.toList());

            //6.调用api的批量查询方法，把black_user_id的List集合传进去。
            //    返回的【userInfoList】是根据【black_user_id】查出来的黑名单里各个用户的全部信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(blackUserIds);

            //7.复制属性，把与数据库交互的实体类userInfo的数据复制到与前端交互的实体类userInfoVoAge中
            List<UserInfoVoAge> voList = userInfoList.stream().map(userInfo -> {

                UserInfoVoAge userInfoVoAge = new UserInfoVoAge();

                BeanUtils.copyProperties(userInfo, userInfoVoAge);

                return userInfoVoAge;

            }).collect(Collectors.toList());

            //8.第三步查询出来的iems的分页结果集是tb_black_list表的
            // 这次设置分页的结果集是根据黑名单各个用户的id查出来的所有信息.
            pageResult.setItems(voList);
        }
        //返回给controller
        return pageResult;
    }


    /**
     * 移除黑名单
     *
     * @param blackUserId
     */
    public void removeBlackList(Long blackUserId) {
        //1.获取登录用户的id
        Long loginUserId = UserHolder.getUserId();

        System.out.println("loginUserId================"+loginUserId);
        System.out.println("blackUserId================"+blackUserId);
        //2.调用api接口的方法移除
        blackListApi.delete(loginUserId, blackUserId);

    }


    /**
     * 保存陌生人问题
     *
     * @param paramMap
     */
    public void updateQuestion(Map<String, String> paramMap) {
        //1.获取用户的登录id
        Long loginUserId = UserHolder.getUserId();

        //2.获取前端传回来的问题内容
        String contentTxt = paramMap.get("content");

        //3.创建与数据库交互的Question实体类对象
        Question question = new Question();

        //4.将用户id和 用户设置的问题内容设置到Question实体类中
        question.setUserId(loginUserId);
        question.setTxt(contentTxt);

        //5.调用api方法保存
        questionApi.save(question);

    }


    /**
     * 修改手机号码：发送验证码
     *
     * @return
     */
    public void sendValidateCode() {
        //1.获取登录用户当前的手机号
        String mobile = UserHolder.getUser().getMobile();

        //2.在redis中存入验证码的key
        String key = RedisKeyConst.CHANGE_MOBILE_VALIDATE_CODE + mobile;

        /*
        3. 将组装好的key，存入redis缓存中，并给key设置一个value值验证码codeInRedis。
           为了第四步方便通过key值，查看value是否已经存在
         */
        String codeInRedis = redisTemplate.opsForValue().get(key);

        log.debug("========== redis中的验证码 修改手机号码:{},{}", codeInRedis, mobile);

        //4.根据value值,判断redis的key是否存在
        if (StringUtils.isNotEmpty(codeInRedis)) {
            //4.1 不是空的，则代表存在。已经发送过验证码
            //抛出异常，“上一次发送的验证码还未失效”
            throw new TanHuaException(ErrorResult.duplicate());
        }

        //5.【key = RedisKeyConst.CHANGE_MOBILE_VALIDATE_CODE + mobile】 不存在
        //5.1 随机生成手机的短信验证码,生成的随机数长度为6
        String validateCode = RandomStringUtils.randomNumeric(6);

        //5.2 调用阿里云smsTemplate的功能，发送手机的短信验证码validateCode
        log.debug("========== 发送验证码 修改手机号码:{},{}", validateCode, mobile);

        Map<String, String> smsResult = smsTemplate.sendValidateCode(mobile, validateCode);

        if (null == smsResult) {
            //5.3 发送失败
            throw new TanHuaException(ErrorResult.fail());
        }

        //6. 发送成功，存入redis中，有效期为5分钟
        log.info("========== 验证码存入redis");
        /*
          将上面第2步的组装好的 key：（RedisKeyConst.CHANGE_MOBILE_VALIDATE_CODE + mobile）存入redis中，
                              value：发送出去的手机短信验证码
         */
        redisTemplate.opsForValue().set(key, validateCode, 5, TimeUnit.MINUTES);
    }


    /**
     * 校验验证码
     *
     * @param verificationCode
     * @return
     */
    public boolean checkValidateCode(String verificationCode) {
        //1.先获取该登录用户的手机号
        String mobile = UserHolder.getUser().getMobile();

        //2.通过固定的实体类RedisKeyConst+手机号，获取组合好的key
        String key = RedisKeyConst.CHANGE_MOBILE_VALIDATE_CODE + mobile;

        //3.通过组合的key查询redis缓存
        String codeInRedis = redisTemplate.opsForValue().get(key);

        //4.防止重复提交
        redisTemplate.delete(key);
        log.debug("==========修改手机号 校验 验证码:{},{},{}", mobile, codeInRedis, verificationCode);

        //5.如果value验证码为空,则代表验证码已经失效。
        if (StringUtils.isEmpty(codeInRedis)) {
            throw new TanHuaException(ErrorResult.loginError());
        } else {
            //6.如果redis中的验证码不为空，则继续比对验证码是否一致
            if (!codeInRedis.equals(verificationCode)) {
                //不一致，返回false
                return false;
            }
            //一致，返回true
            return true;
        }

    }

    /**
     * 修改手机号码
     *
     * @param phone
     */
    public void changeMobile(String phone, String token) {
        //1.获取登录用户的id
        Long loginUserId = UserHolder.getUserId();


        //2.调用api进行更新，传入用户id跟前端修改过的手机号
        userApi.updateMobile(loginUserId, phone);

        //3.打下日志
        log.debug("修改手机号码成功(old:{})=>(new:{})", UserHolder.getUser().getMobile(), phone);


        //4.因为手机号码已被更改，所以需要删除token,让用户重新登录
        String key = RedisKeyConst.TOKEN + token;
        redisTemplate.delete(key);

    }
}
