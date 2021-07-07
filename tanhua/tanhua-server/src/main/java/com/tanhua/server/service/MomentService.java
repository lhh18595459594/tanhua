package com.tanhua.server.service;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.api.mongo.VisitorsApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态的业务处理
 */
@Service
@Slf4j
public class MomentService {

    @Reference
    private PublishApi publishApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    @Reference
    private VisitorsApi visitorsApi;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发布动态
     *
     * @param publishVo
     * @param imageContent
     * @return
     */
    public void postMonment(PublishVo publishVo, MultipartFile[] imageContent) {
        //1.获取登录用户的Id
        Long loginUserId = UserHolder.getUserId();

        //2.上传的N张图片是一个数组类型，查看是否为空
        List<String> medias = new ArrayList<>();
        if (null != imageContent) {
            try {
                //2.1 不为空则遍历数组
                for (MultipartFile file : imageContent) {
                    //2.2 调用方法上传图片到阿里云，获得上传路径
                    String url = ossTemplate.upload(file.getOriginalFilename(), file.getInputStream());

                    //2.3 上传要添加图片的地址，保存到数据库
                    medias.add(url);
                }
            } catch (IOException e) {
                log.error("上传动态图片失败", e);
                throw new TanHuaException("上传动态图片失败");
            }
        }

        //3.构建Publish对象
        Publish publish = new Publish();

        //4.复制属性
        BeanUtils.copyProperties(publishVo, publish);

        //设置用户id
        publish.setUserId(loginUserId);

        //设置图片上传路径
        publish.setMedias(medias);

        //设置状态  0-未审核
        publish.setState(0);

        //设置谁可以看 1——公开
        publish.setSeeType(1);


        //发动动态，并得到动态id
        String publishId = publishApi.add(publish);

        // 发送消息，由消费者自动审核
        //参数1：消息主题  参数2：消息内容
        rocketMQTemplate.convertAndSend("tanhua-publish", publishId);

    }

    /**
     * 查询好友动态
     *
     * @param page     当前页码
     * @param pagesize 每页显示多少条
     * @return
     */
    public PageResult<MomentVo> queryFriendPublishList(Long page, Long pagesize) {
        //1.获取登陆用户id
        Long loginUserId = UserHolder.getUserId();

        //2.通过用户id分页查询好友动态
        PageResult pageResult = publishApi.findFriendPublishByTimeline(loginUserId, page, pagesize);

        //3.获取所有查询动态信息的结果集(动态的结果)
        List<Publish> publishList = pageResult.getItems();

        //4.判断结果集是否为空
        if (CollectionUtils.isEmpty(publishList)) {
            //5.通过stream流遍历动态信息结果集，取出所有的动态信息发布者的【userId,并存入list集合】.
            List<Long> userIds = publishList.stream().map(Publish::getUserId).collect(Collectors.toList());

            //6.根据userId批量查询作者用户信息,查出来的所有作者【用户信息，存入List集合】
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(userIds);

            /*
             7.【通过流的方式遍历并转换】
                遍历所有的用户信息，并将它们转换成map集合
               参数1：UserInfo::getId 取出每个元素的Id
               参数2：userInfo（每个元素的变量名） -> userInfo（相当于return userInfo）
               【每取出一个推荐用户的Id，就返回相关的信息,通过key-value形式存入map】!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
             */
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

            //8.通过stream流遍历动态信息结果集，转成MomentVO
            List<MomentVo> momentVoList = publishList.stream().map(publish -> {
                //9.构建vo
                MomentVo momentVo = new MomentVo();

                //10.设置图片, toArray转成数组
                momentVo.setImageContent(publish.getMedias().toArray(new String[0]));
                //11.设置距离
                momentVo.setDistance("500米");

                //12. 把动态id转成字符串
                momentVo.setId(publish.getId().toHexString());

                //13.把日期转成字符串
                momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

                //14.复制作者信息,从上面封装好的Map中通过userId取出对应的作者信息
                UserInfo userInfo = userInfoMap.get(publish.getUserId());
                BeanUtils.copyProperties(userInfo, momentVo);

                //15.设置标签
                momentVo.setTags(StringUtils.split(userInfo.getTags(), ","));

                //16.设置是否点赞过
                momentVo.setHasLiked(0);  // 0代表没点赞过 TODO
                momentVo.setHasLoved(0);  // 0代表没喜欢过 TODO

                return momentVo;
            }).collect(Collectors.toList());

            //17.把转换好的所以内容设置到pageResult到属性Items列表中，并返回前端
            pageResult.setItems(momentVoList);

        }

        return pageResult;
    }

    /**
     * 查询推荐动态
     *
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<MomentVo> queryRecommendPublishList(Long page, Long pageSize) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();

        //2. 分页查询推荐动态信息
        PageResult pageResult = publishApi.findRecommendPublish(loginUserId, page, pageSize);

        //3. 获取推荐动态的信息的 作者的所有信息列表
        List<Publish> publishList = pageResult.getItems();

        List<MomentVo> voList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(publishList)) {
            //4. 查询作者信息
            List<Long> userIds = publishList.stream().map(Publish::getUserId).collect(Collectors.toList());
            //4. 批量查询作者用户信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(userIds);
            //5. 转成vo
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));
            //5. 转成MomentVO
            voList = publishList.stream().map(publish -> {

                MomentVo vo = new MomentVo();
                // 复制动态内容
                BeanUtils.copyProperties(publish, vo);

                // 图片, toArray转成数组
                vo.setImageContent(publish.getMedias().toArray(new String[0]));
                vo.setDistance("500米");

                // 把动态id转成字符串
                vo.setId(publish.getId().toHexString());

                // 把日期转成字符串
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

                // 复制作者信息
                UserInfo userInfo = userInfoMap.get(publish.getUserId());
                BeanUtils.copyProperties(userInfo, vo);
                vo.setTags(StringUtils.split(userInfo.getTags(), ","));

                // 是否点赞过
                vo.setHasLiked(0); // 0代表没点赞过 TODO
                vo.setHasLoved(0); // 0代表没喜欢过 TODO
                return vo;
            }).collect(Collectors.toList());
        }
        pageResult.setItems(voList);
        return pageResult;
    }

    /**
     * 查看“我的”动态
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<MomentVo> queryMyPublishList(Long userId, long page, long pageSize) {
        //获取登录用户id
        Long loginUserId = UserHolder.getUserId();
        if (null != userId) {
            loginUserId = userId;
        }

        final long loginUserId2 = loginUserId;

        //调用api查询登录用户自己的动态信息的结果集
        PageResult pageResult = publishApi.queryMyPublishList(loginUserId, page, pageSize);
        //3. 获取推荐动态的信息的 作者的所有信息列表
        List<Publish> publishList = pageResult.getItems();

        List<MomentVo> voList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(publishList)) {
            //4. 查询作者信息
            List<Long> userIds = publishList.stream().map(Publish::getUserId).collect(Collectors.toList());
            //4. 批量查询作者用户信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(userIds);
            //5. 转成vo
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));
            //5. 转成MomentVO
            voList = publishList.stream().map(publish -> {

                MomentVo vo = new MomentVo();
                // 复制动态内容
                BeanUtils.copyProperties(publish, vo);

                // 图片, toArray转成数组
                vo.setImageContent(publish.getMedias().toArray(new String[0]));
                vo.setDistance("500米");

                // 把动态id转成字符串
                vo.setId(publish.getId().toHexString());

                // 把日期转成字符串
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

                // 复制作者信息
                UserInfo userInfo = userInfoMap.get(publish.getUserId());
                BeanUtils.copyProperties(userInfo, vo);
                vo.setTags(StringUtils.split(userInfo.getTags(), ","));

                // 是否点赞过
                String key = "publish_like_" + loginUserId2 + "_" + vo.getId();
                vo.setHasLiked(0); // 0代表没点赞过 TODO
                if (redisTemplate.hasKey(key)) {
                    vo.setHasLiked(1); // 1代表点赞过
                }

                vo.setHasLoved(0); // 0代表没喜欢过 TODO
                key = "publish_love_" + loginUserId2 + "_" + vo.getId();
                if (redisTemplate.hasKey(key)) {
                    vo.setHasLoved(1); // 1代表点赞过
                }

                return vo;
            }).collect(Collectors.toList());

            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 查询单条动态
     *
     * @param publishId
     * @return
     */
    public MomentVo findById(String publishId) {
        //1.调用api查询该条动态的所有信息
        Publish publish = publishApi.findById(publishId);

        //2.根据拿到的动态的信息，获取该动态的发布者的id
        Long userId = publish.getUserId();

        //3.调用api，根据发布者id查询发布者的所有信息
        UserInfo userInfo = userInfoApi.findById(userId);

        //4.构建vo
        MomentVo momentVo = new MomentVo();

        //5.复制动态的内容
        BeanUtils.copyProperties(publish, momentVo);

        //6.设置图片, toArray转成数组
        momentVo.setImageContent(publish.getMedias().toArray(new String[0]));
        momentVo.setDistance("500米");

        //7.把动态id转成字符串
        momentVo.setId(publish.getId().toHexString());
        //8.设置日期，把日期转成字符串
        momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

        //9.复制作者信息
        BeanUtils.copyProperties(userInfo, momentVo);

        //10.设置标签
        momentVo.setTags(StringUtils.split(userInfo.getTags(), ","));

        //11.返回
        return momentVo;
    }


    /**
     * 谁看过我
     *
     * @return
     */
    public List<VisitorVo> queryVisitors() {

        //1.获取登录用户的id
        Long loginUserId = UserHolder.getUserId();

        //2.设置Key
        String key = "visitors_time_" + loginUserId;

        //3.redis是否记录上一次登陆时间
        Long lastTime = (Long) redisTemplate.opsForValue().get(key);

        //4. 有值：查询访客记录时，要带上这个时间条件
        //5. 没值：查询访客记录时，不需要时间条件

        //调用api查询所有访客记录
        List<Visitor> visitorList = visitorsApi.queryVisitors(loginUserId, lastTime);

        //6.遍历所有的访客信息，取出id
        List<Long> visitorUserIds = visitorList.stream().map(Visitor::getVisitorUserId).collect(Collectors.toList());

        //7.调用api进行批量查询访客信息
        List<UserInfo> visitorUserInfo = userInfoApi.findByBatchIds(visitorUserIds);

        //8.转成Map集合
        Map<Long, UserInfo> userInfoMap = visitorUserInfo.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

        //9.遍历所有的访客信息，并复制属性
        List<VisitorVo> voList = visitorList.stream().map(visitor -> {
            //10.构建vo
            VisitorVo visitorVo = new VisitorVo();

            //11.取出访客信息
            UserInfo userInfo = userInfoMap.get(visitor.getVisitorUserId());

            //12.复制访客信息
            BeanUtils.copyProperties(userInfo, visitorVo);

            //13.设置标签
            visitorVo.setTags(StringUtils.split(userInfo.getTags(), ","));

            //14.设置缘分值
            visitorVo.setFateValue(visitor.getScore().intValue());
            return visitorVo;
        }).collect(Collectors.toList());


        //15.查询完后，使用redis记录这一次的查询的时间
//        redisTemplate.opsForValue().set(key, System.currentTimeMillis() + "");

        return voList;

    }

}