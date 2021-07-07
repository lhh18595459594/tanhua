package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;

public interface FollowUserApi {

    /**
     * 关注视频用户
     * @param followUser
     */
    void save(FollowUser followUser);

    /**
     * 取消关注视频用户
     * @param followUser
     */
    void remove(FollowUser followUser);
}
