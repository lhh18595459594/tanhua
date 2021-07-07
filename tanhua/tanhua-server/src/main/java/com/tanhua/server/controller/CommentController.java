package com.tanhua.server.controller;

import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制层
 */

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 查询评论列表
     *
     * @param page
     * @param pageSize
     * @param movementId
     * @return
     */
    @GetMapping
    public ResponseEntity findPage(@RequestParam(value = "page", defaultValue = "1") Long page,
                                   @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize,
                                   String movementId) {
        //防止无意义查询
        page = page < 1 ? 1 : page;

        //防止无意义的查询与过大的查询
        if (pageSize <= 0 || pageSize >= 50) {
            pageSize = 10L;
        }


        PageResult<CommentVo> pageResult = commentService.findPage(page, pageSize, movementId);

        return ResponseEntity.ok(pageResult);

    }


    /**
     * 对动态发表评论
     * @param paramMap
     * @return
     */
    @PostMapping
    public ResponseEntity add(@RequestBody Map<String, String> paramMap) {

        commentService.add(paramMap);

        return ResponseEntity.ok(null);
    }

    /**
     * 点赞评论
     * @param commentId
     * @return
     */
    @GetMapping("/{commentId}/like")
    public ResponseEntity like(@PathVariable String commentId){
        long likeCount = commentService.likeComment(commentId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 对评论 取消点赞
     */
    @GetMapping("/{commentId}/dislike")
    public ResponseEntity dislike(@PathVariable String commentId){
        long likeCount = commentService.dislikeComment(commentId);
        return ResponseEntity.ok(likeCount);
    }
}
