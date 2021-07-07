package com.tanhua.server.controller;


import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.User;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.domain.vo.UserInfoVoAge;
import com.tanhua.server.service.SettingsService;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 设置类 控制层
 */
@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * 读取用户的通用设置
     * 请求连接：
     */
    @GetMapping("/settings")
    public ResponseEntity querySettings() {

        SettingsVo settingsVo = settingsService.querySettings();
        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 修改【通用设置】 里的【通知设置】
     *
     * @param settingsVo
     * @return
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity updateNotification(@RequestBody SettingsVo settingsVo) {

        settingsService.updateNotification(settingsVo);
        return ResponseEntity.ok(null);
    }


    /**
     * 分页查询黑名单列表
     * 请求参数：
     * Query参数：
     * page：当前页
     * pagesize：每页查询条数
     */
    @GetMapping("/blacklist")
    public ResponseEntity findBlackList(@RequestParam(value = "page", defaultValue = "1") Long page,
                                        @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize) {
        //防止无意义的查询
        page = page <= 0 ? 1 : page;

        //防止无意义的查询与过大的查询
        if (pageSize <= 0 || pageSize >= 50) {
            pageSize = 10L;
        }
        PageResult<UserInfoVoAge> pageResult = settingsService.blackList(page, pageSize);

        return ResponseEntity.ok(pageResult);

    }


    /**
     * 移除黑名单
     *
     * @param blackUserId
     * @return
     */
    @DeleteMapping("/blacklist/{blackUserId}")
    public ResponseEntity removeBlackList(@PathVariable Long blackUserId) {
        settingsService.removeBlackList(blackUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 保存陌生人问题
     *
     * @param paramMap
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity updateQuestion(@RequestBody Map<String, String> paramMap) {
        settingsService.updateQuestion(paramMap);
        return ResponseEntity.ok(null);
    }

    /**
     * 修改手机号码：发送验证码
     *
     * @return
     */
    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity sendValidateCode() {
        settingsService.sendValidateCode();
        return ResponseEntity.ok(null);
    }


    /**
     * 修改手机号码：校验验证码
     *
     * @param param
     * @return
     */
    @PostMapping("/phone/checkVerificationCode")
    public ResponseEntity checkVerificationCode(@RequestBody Map<String, String> param) {

        boolean flag = settingsService.checkValidateCode(param.get("verificationCode"));

        Map<String, Boolean> result = new HashMap<>();
        result.put("verificationCode", flag);

        return ResponseEntity.ok(result);
    }


    /**
     * 修改手机号码
     * @param
     * @return
     */
    @PostMapping("/phone")
    public ResponseEntity changeMobile(@RequestBody Map<String,String> param,@RequestHeader("Authorization") String token) {


        settingsService.changeMobile(param.get("phone"),token);

        return ResponseEntity.ok(null);


    }
}



