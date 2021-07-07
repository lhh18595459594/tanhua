package com.tanhua.manage.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.interceptor.AdminHolder;
import com.tanhua.manage.service.AdminService;
import com.tanhua.manage.vo.AdminVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 后台登陆时 图片验证码 生成
     */
    @GetMapping("/verification")
    public void showValidateCodePic(String uuid, HttpServletRequest req, HttpServletResponse res) {
        // 设置响应头
        res.setDateHeader("Expires", 0);
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        res.setHeader("Pragma", "no-cache");
        // =============== 以上的设置作用：让浏览器不要缓存这个响应的结果 =========================

        res.setContentType("image/jpeg"); // 告诉浏览，响应的内容体是一个图片
        // 创建一张图片 在图片中生成验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(299, 97);
        // 获取生成的验证码
        String code = lineCaptcha.getCode();
        log.debug("uuid={},code={}", uuid, code);
        // 保存验证码，保存到redis里
        adminService.saveCode(uuid, code);
        try {
            // 把图片响应给请求者
            lineCaptcha.write(res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户登陆校验
     *
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Map<String, String> paramMap) {
        Map<String, String> result = adminService.login(paramMap);
        return ResponseEntity.ok(result);
    }


    /**
     * 获取当前登录用户的详情：
     * 在请求头中包含一个 Authorization 数据(token元素)
     * Bearer token
     * 返回AdminVo
     */
    @PostMapping("/profile")
    public ResponseEntity profile() {
        Admin loginUser = AdminHolder.getAdmin();

        AdminVo adminVo = new AdminVo();

        BeanUtils.copyProperties(loginUser, adminVo);

        return ResponseEntity.ok(adminVo);
    }


    /**
     * 退出登录
     * @param token
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity logout(@RequestHeader("Authorization") String token) {
        adminService.logout(token);

        return ResponseEntity.ok(null);
    }
}