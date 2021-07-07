package com.tanhua.server.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * "我的"功能模块
 */
@RestController
@RequestMapping("/users")
public class UserInfoController {


    @Autowired
    private UserService userService;

    /**
     * 查看登陆用户信息
     *
     * @param userID
     * @param huanxinID
     * @return
     */
    @GetMapping
    public ResponseEntity getUserInfo(Long userID, Long huanxinID) {
        UserInfoVo userInfoVo = userService.getLoginUserInfo();
        return ResponseEntity.ok(userInfoVo);
    }

    /**
     * 更新用户信息
     *
     * @param userInfoVo
     * @return
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfoVo userInfoVo) {
        userService.updateUserInfo(userInfoVo);
        return ResponseEntity.ok(null);
    }


    /**
     * 更新用户头像
     *
     * @param headPhoto
     * @return
     */
    @PostMapping("/header")
    public ResponseEntity updateHeadPhoto(MultipartFile headPhoto) {
        userService.updateHeadPhoto(headPhoto);
        return ResponseEntity.ok(null);
    }


    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     * 我的页面中的统计
     *
     * @return
     */
    @GetMapping("/counts")
    public ResponseEntity counts() {
        CountsVo countsVo = userService.counts();

        return ResponseEntity.ok(countsVo);
    }


    /**
     * 相互喜欢、我喜欢、粉丝列表分页查询
     *
     * @param type     1 互相关注
     *                 2 我关注
     *                 3 粉丝
     *                 4 谁看过我
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/friends/{type}")
    public ResponseEntity queryUserLikeList(@PathVariable int type,
                                            @RequestParam(value = "page", defaultValue = "1") Long page,
                                            @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize,
                                            String nickname) {



        PageResult<FriendVo> pageResult = userService.queryUserLikeList(type,page,pageSize,nickname);

        return ResponseEntity.ok(pageResult);

    }


    /**
     * 粉丝中的喜欢
     *
     * @param fansId
     * @return
     */
    @GetMapping("/fans/{fansId}")
    public ResponseEntity fansLike(@PathVariable Long fansId) {

        userService.fansLike(fansId);
        return ResponseEntity.ok(null);
    }


}
