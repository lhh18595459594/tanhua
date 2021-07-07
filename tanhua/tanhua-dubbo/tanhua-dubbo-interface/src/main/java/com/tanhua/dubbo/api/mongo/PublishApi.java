package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;

/**
 * 动态业务处理
 */
public interface PublishApi {

    /**
     * 发布动态
     * @param publish
     */
    String add(Publish publish);

    /**
     * 通过用户id分页查询好友动态
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findFriendPublishByTimeline(Long loginUserId, Long page, Long pageSize);


    /**
     * 分页查询推荐动态信息
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findRecommendPublish(Long loginUserId, Long page, Long pageSize);

    /**
     * 用户动态查询
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult queryMyPublishList(Long loginUserId, Long page, Long pageSize);

    /**
     *通过id查询单条动态信息
     * @param publishId
     * @return
     */
    Publish findById(String publishId);

    /**
     * 获取当前用户的所有动态分页列表
     * @param page
     * @param pageSize
     * @param uid
     * @param state
     * @return
     */
    PageResult findAll(Long page, Long pageSize, Long uid, Integer state);

    /**
     * 更新动态的审核状态
     * @param publishId
     * @param state
     */
    void updateState(String publishId, int state);
}
