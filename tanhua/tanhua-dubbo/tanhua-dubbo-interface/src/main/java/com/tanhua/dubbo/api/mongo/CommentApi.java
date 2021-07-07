package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

public interface CommentApi {

    /**
     * 保存点赞、评论
     * @param comment
     * @return
     */
    long save(Comment comment);

    /**
     * 取消点赞
     * @param comment
     * @return
     */
    Integer remove(Comment comment);

    /**
     * 查询评论列表
     * @param page
     * @param pageSize
     * @param pulishId
     * @return
     */
    PageResult findPage(Long page, Long pageSize, String pulishId);

    /**
     * 按登陆用户id 通过commentType分页查询 评论信息
     * @param loginUserId
     * @param commentType
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageByUserId(Long loginUserId, int commentType, Long page, Long pageSize);
}
