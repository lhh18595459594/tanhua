package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录控制层
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;


    /**
     * 通过手机号码查询
     * @param phone
     * @return
     */
    @GetMapping("/findByMobile")
    public ResponseEntity findByMobile(String phone){
        User user = userService.findByMobile(phone);
        return ResponseEntity.ok(user);
    }

    /**
     * 登录第一步---手机号登录
     * 发送验证码
     * 使用RequestBody Map<String,String>接收参数。因此前端提交的请求头中声明了application/json, 而是从body中来的
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Map<String,String> paramMap){
        // 获取传过来的手机号码
        String phone = paramMap.get("phone");
        // 调用业务发送验证码
        userService.sendValidateCode(phone);
        // 响应
        return ResponseEntity.ok(null);
    }

    /**
     * 登陆第二步： 验证码校验
     * @return
     */
    @PostMapping("/loginVerification")
    public ResponseEntity loginVerification(@RequestBody Map<String,String> paramMap){
        // 手机号码
        String phone = paramMap.get("phone");
        // 验证码
        String verificationCode = paramMap.get("verificationCode");
        // 调用业务 校验，生成token
        Map<String,Object> result = userService.loginVerification(phone, verificationCode);
        return ResponseEntity.ok(result);
    }
}
