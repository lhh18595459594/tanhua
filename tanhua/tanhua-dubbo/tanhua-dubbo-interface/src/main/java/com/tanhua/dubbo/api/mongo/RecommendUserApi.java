package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

public interface RecommendUserApi {

    /**
     * 查询佳人，缘分值最高的
     * @param loginUserId
     * @return
     */
    RecommendUser todayBest(Long loginUserId);

    /**
     * 通过登录用户id, 分页查询推荐用户列表
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long loginUserId, Long page, Long pageSize);


    /**
     * 查看佳人信息
     *
     * @param userId
     * @return
     */
    Double queryForScore(Long userId, Long loginUserId);
}
