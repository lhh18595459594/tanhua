package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.JsonAdapter;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回复陌生人问题
 */

@Service
public class ImService {

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private FriendApi friendApi;

    @Reference
    private CommentApi commentApi;


    /**
     * 回复陌生人问题
     *
     * @param paramMap
     */
    public void replyStrangerQuestions(Map<String, Object> paramMap) {
        //陌生人的Id
        Long userId = (Long) paramMap.get("userId");

        //回复的消息
        String content = (String) paramMap.get("reply");

        //查询发送消息的用户信息，消息内容中需要用到nickname
        UserInfo userInfo = userInfoApi.findById(userId);

        //陌生人问题
        Question StrangerQuestions = questionApi.findByUserId(userId);
        String question = "你喜欢我吗";
        if (null != StrangerQuestions) {
            question = StrangerQuestions.getTxt();
        }

        //构建消息内容
        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("userId", userInfo.getId().toString());
        messageMap.put("nickname", userInfo.getNickname());
        messageMap.put("strangerQuestion", StrangerQuestions);
        messageMap.put("reply", content);


        //调用环信发送消息
        String msg = JSON.toJSONString(messageMap);

        huanXinTemplate.sendMsg(userId.toString() + " ", msg);
    }

    /**
     * 添加好友
     *
     * @param friendId
     */
    public void addContacts(Long friendId) {

        //1.获取登录用户的Id
        Long loginUserId = UserHolder.getUserId();

        friendApi.add(loginUserId, friendId);

        // 环信上也加为好友，【注意】：两个id必须在环信上有帐号
        huanXinTemplate.makeFriends(loginUserId, friendId);
    }

    /**
     * 查询好友列表
     *
     * @param page     当前页
     * @param pageSize 每页显示多少条
     * @param keyword  关键字
     * @return
     */
    public PageResult<ContactVo> queryContactsList(Long page, Long pageSize, String keyword) {
        //0.获取登录用户id
        Long loginUserId = UserHolder.getUserId();

        //1.调用api查询所有联系人
        PageResult pageResult = friendApi.findPage(loginUserId, page, pageSize, keyword);

        //2.取出所有的联系人信息
        List<Friend> friendList = pageResult.getItems();

        //3.遍历friendList,并将所以的联系人id取出来装入新的list集合中
        List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());


        //4.调用api,根据好友id.批量查询好友信息
        List<UserInfo> friendInfo = userInfoApi.findByBatchIds(friendIds);

        //5.将所有的好友信息，取出每个人的id和个人信息，并以键值对的形式存入map中
        Map<Long, UserInfo> friendInfoMap = friendInfo.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

        /*
          6.通过流遍历所有联系人信息friendList，在里面构建vo对象，并复制属性。
         */
        List<ContactVo> contactVoList = friendList.stream().map(userinfo -> {

            //7.构建contactVo
            ContactVo contactVo = new ContactVo();

            //8.依次取出好友信息
            UserInfo friendUserInfo = friendInfoMap.get(userinfo.getFriendId());

            //9.复制好友信息
            BeanUtils.copyProperties(friendUserInfo, contactVo);

            //10.因为前端需要的userId是String类型，而这里的是Long类型，需要转换一下
            contactVo.setUserId(userinfo.getFriendId().toString());

            return contactVo;
        }).collect(Collectors.toList());


        //10.设置到pageResult中
        pageResult.setItems(contactVoList);

        return pageResult;
    }

    /**
     * 对登陆用户
     *
     * @param commentType 点赞(1)、喜欢(3)、评论(2)
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<MessageVo> messageCommentList(int commentType, Long page, Long pageSize) {
        //1. 按登陆用户id 通过commentType分页查询 评论信息
        Long loginUserId = UserHolder.getUserId();

        PageResult pageResult = commentApi.findPageByUserId(loginUserId, commentType, page, pageSize);

        //2.取出查出来的结果
        List<Comment> commentList = pageResult.getItems();

        if (!CollectionUtils.isEmpty(commentList)) {
            //3. 获取所有评论者的用户ids
            List<Long> UserIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());

            //4. 批量查询出所有用户的信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(UserIds);

            //5.把用户信息转成map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

            //6.遍历所有的信息
            List<MessageVo> voList = commentList.stream().map(comment -> {

                //7.构建vo
                MessageVo messageVo = new MessageVo();
                //8.取出评论者的信息
                UserInfo userInfo = userInfoMap.get(comment.getUserId());

                //9.复制内容
                BeanUtils.copyProperties(userInfo, messageVo);

                //10.因为UserInfo和MessageVo的id，一个是Long类型，一个是String类型,需要转换
                messageVo.setId(userInfo.getId().toString());

                //11.设置时间
                messageVo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
                return messageVo;
            }).collect(Collectors.toList());

            pageResult.setItems(voList);
        }

        return pageResult;
    }


}
