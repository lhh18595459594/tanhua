package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

// 【注意】发布服务要使用dubbo的包
@Service
public class UserApiImpl implements UserApi{

    @Autowired
    private UserMapper userMapper;

    /**
     * 保存用户
     *
     * @param user
     * @return 返回新增用户的id
     */
    public Long save(User user) {
        user.setUpdated(new Date());
        user.setCreated(new Date());
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 通过手机号码查询
     *
     * @param mobile
     * @return
     */
    public User findByMobile(String mobile) {
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("mobile", mobile);
        return userMapper.selectOne(queryWrapper);
    }


    /**
     * 修改手机号码
     * @param loginUserId
     * @param phone
     */
    public void updateMobile(Long loginUserId, String phone) {
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();

        //创建User对象，并手动设置id和修改后的phone
        User user=new User();
        user.setId(loginUserId);
        user.setMobile(phone);


        //调用update进行修改
        userMapper.updateById(user);
    }
}
