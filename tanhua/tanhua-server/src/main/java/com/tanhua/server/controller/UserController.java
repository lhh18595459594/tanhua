package com.tanhua.server.controller;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 完善信息层
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 完善个人信息
     *
     * @param userInfoVo
     * @return
     */
    //这里用UserInfoVo实体类来接收前端传过来的对应的参数.
    @PostMapping("/loginReginfo")
    public ResponseEntity  loginReginfo(@RequestBody UserInfoVo userInfoVo ) {

        userService.loginReginfo(userInfoVo);
        return ResponseEntity.ok(null);
    }


    /**
     * 完善用户信息  选取头像
     * @param headPhoto
     *
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity updateUserAvatar(MultipartFile headPhoto){

        userService.updateUserAvatar(headPhoto);
        return ResponseEntity.ok(null);

    }
}
