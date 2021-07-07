package com.tanhua.dubbo.api;

import com.tanhua.domain.db.User;

public interface UserApi {
    /**
     * 保存用户
     * @param user
     * @return
     */
    Long save(User user);

    /**
     * 通过手机号码查询
     * @param mobile
     * @return
     */
    User findByMobile(String mobile);

    /**
     * 修改手机号
     * @param loginUserId
     * @param phone
     */
    void updateMobile(Long loginUserId, String phone);
}
