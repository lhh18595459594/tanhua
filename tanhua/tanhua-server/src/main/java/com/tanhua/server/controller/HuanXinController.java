package com.tanhua.server.controller;

import com.tanhua.commons.vo.HuanXinUser;
import com.tanhua.domain.db.User;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/huanxin")
@Slf4j
public class HuanXinController {

    /**
     * 用户登录成功后，获取用户对应的环信帐号信息
     *
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity getHuanxinUser() {

        User loginUser = UserHolder.getUser();

        HuanXinUser huanXinUser = new HuanXinUser(loginUser.getId().toString(), "123456", loginUser.getId().toString());

        log.info("huanxinUser:" + huanXinUser);

        return ResponseEntity.ok(huanXinUser);
    }
}
