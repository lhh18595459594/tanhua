package com.tanhua.manage.service;

import cn.hutool.core.date.DateUtil;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.naming.spi.ObjectFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private VideoApi videoApi;

    @Reference
    private PublishApi publishApi;

    @Reference
    private CommentApi commentApi;

    /**
     * 用户管理页面分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<UserInfoVo> findPage(Long page, Long pageSize) {

        PageResult pageResult = userInfoApi.findPage(page, pageSize);

        //获得所有人的信息集合
        List<UserInfo> userInfoList = pageResult.getItems();

        //遍历
        List<UserInfoVo> voList = userInfoList.stream().map(userInfo -> {

            UserInfoVo vo = new UserInfoVo();

            //复制
            BeanUtils.copyProperties(userInfo, vo);
            return vo;
        }).collect(Collectors.toList());

        pageResult.setItems(voList);

        return pageResult;
    }


    /**
     * 用户详情
     *
     * @param userId
     * @return
     */
    public UserInfo findUserDetail(Long userId) {
        return userInfoApi.findById(userId);
    }

    /**
     * 获取当前用户的所有视频分页列表
     *
     * @param page
     * @param pageSize
     * @param uid
     * @return
     */
    public PageResult<VideoVo> findAllVideos(Long page, Long pageSize, Long uid) {

        PageResult pageResult = videoApi.findPageAll(page, pageSize, uid);

        List<Video> videoList = pageResult.getItems();

        //查询昵称
        UserInfo userInfo = userInfoApi.findById(uid);

        if (CollectionUtils.isNotEmpty(videoList)) {

            List<VideoVo> voList = videoList.stream().map(video -> {
                //构建vo
                VideoVo vo = new VideoVo();

                BeanUtils.copyProperties(video, vo);
                BeanUtils.copyProperties(userInfo, vo);
                vo.setCover(video.getPicUrl());
                vo.setCreateDate(video.getCreated().intValue());
                return vo;

            }).collect(Collectors.toList());

            pageResult.setItems(voList);
        }

        return pageResult;
    }


    /**
     * 获取当前用户的所有动态分页列表
     *
     * @param page
     * @param pageSize
     * @param uid
     * @param state
     * @return
     */
    public PageResult<MomentVo> findAllMovements(Long page, Long pageSize, Long uid, Integer state) {

        PageResult pageResult = publishApi.findAll(page, pageSize, uid, state);

        //1.获取所以的动态信息
        List<Publish> publishList = pageResult.getItems();

        //2.获取作者信息
        UserInfo userInfo = userInfoApi.findById(uid);

        if (CollectionUtils.isNotEmpty(publishList)) {
            //2.遍历集合
            List<MomentVo> voList = publishList.stream().map(publish -> {
                //3.构建vo
                MomentVo momentVo = new MomentVo();

                //4复制作者信息
                BeanUtils.copyProperties(userInfo, momentVo);

                //5.复制其他属性
                BeanUtils.copyProperties(publish, momentVo);

                momentVo.setUserId(uid);
                // 把动态id转成字符串
                momentVo.setId(publish.getId().toHexString());

                momentVo.setImageContent(publish.getMedias().toArray(new String[0]));
                momentVo.setDistance("500米");
                // 把日期转成字符串
                momentVo.setCreateDate(DateUtil.date(publish.getCreated()).toString("yyyy-MM-dd HH:mm:ss"));
                momentVo.setTags(StringUtils.split(userInfo.getTags(), ","));
                return momentVo;
            }).collect(Collectors.toList());

            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 根据id,查询动态详情
     *
     * @param userId
     * @return
     */
    public MomentVo findMovementById(String userId) {
        //查出该条动态详情
        Publish publish = publishApi.findById(userId);

        UserInfo userInfo = userInfoApi.findById(publish.getUserId());

        //构建vo
        MomentVo vo = new MomentVo();
        // 复制动态内容
        BeanUtils.copyProperties(publish, vo);
        // 复制作者信息
        BeanUtils.copyProperties(userInfo, vo);

        // 把动态id转成字符串
        vo.setId(publish.getId().toHexString());
        // 图片, toArray转成数组
        vo.setImageContent(publish.getMedias().toArray(new String[0]));
        // 把日期转成字符串
        vo.setCreateDate(DateUtil.date(publish.getCreated()).toString("yyyy-MM-dd HH:mm:ss"));
        //设置标签
        vo.setTags(StringUtils.split(userInfo.getTags(), ","));
        return vo;
    }

    /**
     * 动态的评论列表
     *
     * @param messageID
     * @return
     */
    public PageResult<CommentVo> findMomentComment(Long page, Long pageSize, String messageID) {

        PageResult pageResult = commentApi.findPage(page, pageSize, messageID);

        List<Comment> commentList = pageResult.getItems();
        if (!CollectionUtils.isEmpty(commentList)) {
            //2. 获取所有评论者的ids集合
            List<Long> userIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());
            //3. 调用userInfoApi批量评论者的信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(userIds);
            //3.1 把查询到评论者信息转成map<key=userId,value=userInfo>
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //4. 转成vo
            List<CommentVo> voList = commentList.stream().map(comment -> {
                CommentVo vo = new CommentVo();
                BeanUtils.copyProperties(comment, vo);
                // 设置评论id
                vo.setId(comment.getId().toHexString());
                vo.setCreateDate(DateUtil.date(comment.getCreated()).toString("yyyy-MM-dd HH:mm"));
                // 通过评论者的id，获取评论者信息
                UserInfo userInfo = userInfoMap.get(comment.getUserId());
                BeanUtils.copyProperties(userInfo, vo);
                return vo;
            }).collect(Collectors.toList());
            pageResult.setItems(voList);
        }
        //5. 设置到pageResult再返回
        return pageResult;

    }
}
