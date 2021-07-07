package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

public interface FriendApi {

    /**
     * 添加好友
     * @param loginUserId
     * @param friendId
     */
    void add(Long loginUserId, Long friendId);

    /**
     * 查询好友列表
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long loginUserId, Long page, Long pageSize,String keyword);
}
