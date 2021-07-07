package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoApiImpl implements UserInfoApi {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 完善个人用户信息
     */
    public void add(UserInfo userInfo) {
        //调用mybatis-plus的insert方法，把完善的信息传进数据库
        userInfoMapper.insert(userInfo);
    }

    /**
     * 更新用户信息
     *
     * @param userInfo
     */
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     */
    public UserInfo findById(Long id) {

        return userInfoMapper.selectById(id);
    }

    /**
     * 通过批量id查询用户详情
     *
     * @param blackUserIds
     * @return
     */
    public List<UserInfo> findByBatchIds(List<Long> blackUserIds) {
        return userInfoMapper.selectBatchIds(blackUserIds);
    }


    /**
     * 用户管理页面分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findPage(Long page, Long pageSize) {

        IPage<UserInfo> aaa = new Page<>(page, pageSize);
        userInfoMapper.selectPage(aaa, null);

        return PageResult.pageResult(aaa.getTotal(), aaa.getCurrent(), aaa.getSize(), aaa.getRecords());
    }


}
