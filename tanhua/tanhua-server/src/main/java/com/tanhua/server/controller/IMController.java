package com.tanhua.server.controller;

import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.ImService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 联系人  控制层
 */
@RestController
@RequestMapping("/messages")
public class IMController {

    @Autowired
    private ImService imService;

    /**
     * 添加好友
     *
     * @param paramMap
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity addContacts(@RequestBody Map<String, Long> paramMap) {

        //获取对方的id。
        Long friendId = paramMap.get("userId");

        imService.addContacts(friendId);
        return ResponseEntity.ok(null);
    }


    /**
     * 查询好友列表
     *
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    @GetMapping("/contacts")
    public ResponseEntity queryContactsList(@RequestParam(defaultValue = "1") Long page,
                                            @RequestParam(defaultValue = "10") Long pageSize,
                                            @RequestParam(required = false) String keyword) {

        //防止无意义查询
        page = page < 1 ? 1 : page;


        PageResult<ContactVo> pageResult = imService.queryContactsList(page, pageSize, keyword);

        return ResponseEntity.ok(pageResult);
    }


    /**
     * 谁对我点赞 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/likes")
    public ResponseEntity likes(@RequestParam(value = "page", defaultValue = "1") Long page,
                                @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(1, page,pageSize); // 1: 点赞
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁对我评论 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/comments")
    public ResponseEntity comments(@RequestParam(value = "page", defaultValue = "1") Long page,
                                   @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(2, page,pageSize); // 2: 评论
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁对我喜欢 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/loves")
    public ResponseEntity loves(@RequestParam(value = "page", defaultValue = "1") Long page,
                                @RequestParam(value = "pagesize", defaultValue = "10") Long pageSize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(3, page,pageSize); // 3: 喜欢
        return ResponseEntity.ok(pageResult);
    }

}
