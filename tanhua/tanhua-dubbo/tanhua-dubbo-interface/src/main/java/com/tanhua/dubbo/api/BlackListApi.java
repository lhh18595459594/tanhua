package com.tanhua.dubbo.api;

import com.tanhua.domain.vo.PageResult;

import java.util.List;

public interface BlackListApi {


    /**
     * 黑名单分页查询
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult fingPage(Long loginUserId, Long page, Long pageSize);

    /**
     * 移除黑名单
     * @param loginUserId
     * @param blackUserId
     */
    void delete(Long loginUserId, Long blackUserId);
}
