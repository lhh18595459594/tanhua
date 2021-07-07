package com.tanhua.server.controller;

import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/movements")
public class MomentController {

    @Autowired
    private MomentService momentService;

    @Autowired
    private CommentService commentService;


    /**
     * 发布动态
     *
     * @param publishVo
     * @param imageContent
     * @return
     */
    @PostMapping
    public ResponseEntity postMonment(PublishVo publishVo, MultipartFile[] imageContent) {
        //调用业务发布
        momentService.postMonment(publishVo, imageContent);

        return ResponseEntity.ok(null);
    }

    /**
     * 查询好友动态
     *
     * @param page     当前页码
     * @param pagesize 每页显示多少条
     * @return
     */
    @GetMapping
    public ResponseEntity queryFriendPublishList(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pagesize) {
        //防止无意义查询
        page = page < 1 ? 1 : page;

        PageResult<MomentVo> pageResult = momentService.queryFriendPublishList(page, pagesize);

        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询推荐动态
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/recommend")
    public ResponseEntity queryRecommendPublishList(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "10") Long pageSize) {
        page = page < 1 ? 1 : page;
        PageResult<MomentVo> pageResult = momentService.queryRecommendPublishList(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 查看"我的"动态
     *
     * @param page
     * @param pageSize
     * @param userId：后台管理系统中使用
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity queryMyPublishList(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long pageSize,
                                             @RequestParam(required = false) Long userId) {

        //防止无意义查询
        page = page < 1 ? 1 : page;


        //防止无意义的查询与过大的查询
        if (pageSize <= 0 || pageSize >= 50) {
            pageSize = 10L;
        }

        PageResult<MomentVo> pageResult = momentService.queryMyPublishList(userId, page, pageSize);

        return ResponseEntity.ok(pageResult);
    }

    /**
     * 动态信息点赞
     * @param PublishId
     * @return
     */
    @GetMapping("/{PublishId}/like")
    public ResponseEntity like(@PathVariable String PublishId) {

        //返回点赞数
        Long likeCount = commentService.like(PublishId);

        return ResponseEntity.ok(likeCount);
    }


    /**
     * 动态取消点赞
     *
     * @return
     */
    @GetMapping("/{publishId}/dislike")
    public ResponseEntity dislike(@PathVariable String publishId) {
        Integer likeCount = commentService.dislike(publishId);
        // 返回的是基础数据类型，前端不需要通过key(名称)的方式来获取
        return ResponseEntity.ok(likeCount);
    }


    /**
     * 查询单条动态
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}")
    public ResponseEntity findById(@PathVariable String publishId) {
        MomentVo momentVo = momentService.findById(publishId);
        return ResponseEntity.ok(momentVo);
    }


    /**
     * 谁看过我
     * @return
     */
    @GetMapping("/visitors")
    public ResponseEntity queryVisitors() {

        List<VisitorVo> visitorVoList = momentService.queryVisitors();
        return ResponseEntity.ok(visitorVoList);
    }

}
