package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论、点赞业务类
 */
@Service
public class CommentService {
    @Reference
    private CommentApi commentApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 动态信息 点赞
     *
     * @param publishId
     * @return
     */
    public long like(String publishId) {
        //1.获取登录用户的id
        Long loginUserId = UserHolder.getUserId();

        //2.构建【动态信息点赞、评论对象】 Comment
        //设置点赞属性
        Comment comment = new Comment();

        comment.setCommentType(1);        //设置评论类型，1-点赞
        comment.setUserId(loginUserId);  //设置点赞的人的Id
        comment.setTargetType(1);        //设置评论内容类型，1——对动态操作

        comment.setTargetId(new ObjectId(publishId));  //设置动态信息的自身id

        //3.调用api保存评论，获取返回的点赞数
        Long likeCount = commentApi.save(comment);


        //4.存入redis，标记当前用户对这个动态信息点过赞了
        String key = "publish_like_" + loginUserId + "_" + publishId;
        redisTemplate.opsForValue().set(key, "1");

        //5.返回点赞数
        return likeCount;
    }

    /**
     * 动态信息取消点赞
     *
     * @param publishId
     * @return
     */
    public Integer dislike(String publishId) {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();

        //2.构建comment对象
        Comment comment = new Comment();
        //2.1设置发布动态的id
        comment.setTargetId(new ObjectId(publishId));
        //2.2设置评论类型  1——点赞
        comment.setCommentType(1);
        //2.3设置评论人的id
        comment.setUserId(loginUserId);

        //3.调用api 取消点赞 接收返回的点赞数
        Integer likeCount = commentApi.remove(comment);

        //4.redis中删除点赞过的标记
        String key = "publish_like_" + loginUserId + "_" + publishId;
        redisTemplate.delete(key);

        //5.返回
        return likeCount;
    }

    /**
     * 查询评论列表
     *
     * @param page
     * @param pageSize
     * @param pulishId
     * @return
     */
    public PageResult<CommentVo> findPage(Long page, Long pageSize, String pulishId) {
        //1. 先调用api，通过动态的id 分页查询 评论的分页数据
        PageResult pageResult = commentApi.findPage(page, pageSize, pulishId);

        //2.获取分页的所有数据结果集
        List<Comment> commentList = pageResult.getItems();

        //如果commentList不为空
        if (!CollectionUtils.isEmpty(commentList)) {
            //3.遍历结果集,获取所有评论者的id集合
            List<Long> userIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());

            //4.调用api。查询出所有的评论者信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(userIds);

            //5. 通过使用流的遍历，将所有的评论者信息userInfoList的【id】和【个人信息】依次取出，存入Map集合中
            //key：UserInfo::getId  value：userInfo
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

            //6.转成vo
            List<CommentVo> commentVoList = commentList.stream().map(comment -> {
                //7.构建vo
                CommentVo commentVo = new CommentVo();

                //8.获取每一个评论者的信息
                UserInfo userInfo = userInfoMap.get(comment.getUserId());

                //9.复制评论者的信息
                BeanUtils.copyProperties(userInfo, commentVo);

                //10.登录用户是否对这个评论点过赞
                commentVo.setHasLiked(0);
                String key = "comment_like_" + UserHolder.getUserId() + "_" + commentVo.getId();
                if (redisTemplate.hasKey(key)) {
                    //11.如果存在，则代表已经点过赞
                    commentVo.setHasLiked(1);
                }
                //11.返回
                return commentVo;
            }).collect(Collectors.toList());
        }

        //12.将转换好的数据，设置到pageResult中，并返回
        pageResult.setItems(commentList);

        return pageResult;

    }

    /**
     * 对动态发表评论
     * @param paramMap
     */
    public void add(Map<String, String> paramMap) {
        //1.获取动态id和评论内容
        String publishId = paramMap.get("movementId");
        String content = paramMap.get("comment");

        //2.获取登录用户的Id
        Long loginUserId = UserHolder.getUserId();

        //3.构建comment对象
        Comment comment=new Comment();

        //设置该条评论的id
        comment.setTargetId(new ObjectId(publishId));
        //设置该条评论的内容
        comment.setContent(content);
        //设置评论者的id
        comment.setTargetUserId(loginUserId);
        //设置评论类型 ，2-评论
        comment.setCommentType(2);
        //设置评论内容类型，1-对动态操作
        comment.setTargetType(1);

        // 调用api添加评论
        commentApi.save(comment);
    }


    /**
     * 对评论点赞
     * @param commentId
     * @return
     */
    public long likeComment(String commentId) {
        //1.获取登录用户的id
        Long loginUserId = UserHolder.getUserId();

        //2.构建【动态信息点赞、评论对象】 Comment
        //设置点赞属性
        Comment comment = new Comment();

        comment.setCommentType(1);        //设置评论类型，1-点赞
        comment.setUserId(loginUserId);  //设置点赞的人的Id
        comment.setTargetType(3);        //设置评论内容类型，3——对评论操作

        comment.setTargetId(new ObjectId(commentId));  //设置动态信息的自身id

        //3.调用api保存评论，获取返回的点赞数
        Long likeCount = commentApi.save(comment);


        //4.存入redis，标记当前用户对这个动态信息点过赞了
        String key = "publish_like_" + loginUserId + "_" + commentId;
        redisTemplate.opsForValue().set(key, "1");

        //5.返回点赞数
        return likeCount;
    }


    /**
     * 对评论：取消点赞
     * @param commentId
     * @return
     */
    public long dislikeComment(String commentId) {
        // 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        // 构建对评论点赞对象Comment
        Comment comment = new Comment();
        comment.setCommentType(1); //1-点赞，
        comment.setUserId(loginUserId);
        comment.setTargetId(new ObjectId(commentId));
        // 调用api保存评论, 获取返回的点赞数
        long likeCount = commentApi.remove(comment);
        // 存入redis，标记当前用户对这个动态点过赞了
        String key = "comment_like_" + loginUserId+"_" + commentId;
        // 使用什么样的redis数据类型来替换key=value方式
        redisTemplate.delete(key);
        // 返回的点赞数
        return likeCount;
    }
}
