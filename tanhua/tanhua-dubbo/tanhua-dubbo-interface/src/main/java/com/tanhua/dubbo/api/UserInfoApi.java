package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVoAge;

import java.util.List;

public interface UserInfoApi{
    /**
     * 用户注册后完善个人信息
     * @param userInfo
     */
    void add(UserInfo userInfo);

    /**
     * 完善用户信息  选取头像
     * @param userInfo
     */
    void update(UserInfo userInfo);

    /**
     * 查看登陆用户信息
     * @param id
     * @return
     */
    UserInfo findById(Long id);

    /**
     * 通过批量id查询用户详情
     * @param blackUserIds
     * @return
     */
    List<UserInfo> findByBatchIds(List<Long> blackUserIds);


    /**
     * 用户管理页面分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long page, Long pageSize);


}