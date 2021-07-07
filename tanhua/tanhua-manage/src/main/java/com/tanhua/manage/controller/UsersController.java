package com.tanhua.manage.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.*;
import com.tanhua.manage.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage")
public class UsersController {

    @Autowired
    private UsersService usersService;


    /**
     * 用户管理页面分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/users")
    public ResponseEntity findPage(@RequestParam(value = "page", defaultValue = "1") Long page,
                                   @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize) {

        //1.防止无意义查询
        page = page < 1 ? 1 : page;

        PageResult<UserInfoVo> pageResult = usersService.findPage(page, pageSize);

        return ResponseEntity.ok(pageResult);

    }


    /**
     * 用户详情
     *
     * @param userId
     * @return
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity findUserDetail(@PathVariable Long userId) {

        UserInfo userInfo = usersService.findUserDetail(userId);
        return ResponseEntity.ok(userInfo);
    }


    /**
     * 获取当前用户的所有视频分页列表
     *
     * @param page
     * @param pageSize
     * @param uid
     * @return
     */
    @GetMapping("/videos")
    public ResponseEntity findAllVideos(@RequestParam(value = "page", defaultValue = "1") Long page,
                                        @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize,
                                        @RequestParam(required = false) Long uid) {

        //1.防止无意义查询
        page = page < 1 ? 1 : page;

        PageResult<VideoVo> pageResult = usersService.findAllVideos(page, pageSize, uid);

        return ResponseEntity.ok(pageResult);
    }


    /**
     * 获取当前用户的所有动态分页列表
     *
     * @param page
     * @param pageSize
     * @param uid
     * @param state
     * @return
     */
    @GetMapping("/messages")
    public ResponseEntity findAllMovements(@RequestParam(value = "page", defaultValue = "1") Long page,
                                           @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize,
                                           @RequestParam(required = false) Long uid,
                                           @RequestParam(required = false) Integer state) {


        PageResult<MomentVo> pageResult = usersService.findAllMovements(page, pageSize, uid, state);

        return ResponseEntity.ok(pageResult);
    }


    /**
     * 根据id,查询动态详情
     *
     * @param userId
     * @return
     */
    @GetMapping("/messages/{userId}")
    public ResponseEntity findMovementById(@PathVariable String userId) {

        MomentVo vo = usersService.findMovementById(userId);
        return ResponseEntity.ok(vo);
    }


    /**
     * 动态的评论列表
     * @param messageID
     * @return
     */
    @GetMapping("/messages/comments")
    public ResponseEntity findAllComments(@RequestParam(value = "page", defaultValue = "1") Long page,
                                          @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize,
                                          String sortProp, String sortOrder, String messageID) {

        PageResult<CommentVo> pageResult = usersService.findMomentComment(page, pageSize, messageID);
        return ResponseEntity.ok(pageResult);
    }

}
