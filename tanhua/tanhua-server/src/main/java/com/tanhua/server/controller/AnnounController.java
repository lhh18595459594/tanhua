package com.tanhua.server.controller;

import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.AnnounService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告管理 控制层
 */
@RestController
@RequestMapping("/messages")
public class AnnounController {

    @Autowired
    private AnnounService announService;

    /**
     * 查看公告列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/announcements")
    public ResponseEntity announcements(@RequestParam(defaultValue = "1") Long page,
                                        @RequestParam(defaultValue = "5") Long pageSize) {


        PageResult<AnnouncementVo> pageResult = announService.announcements(page, pageSize);

        return ResponseEntity.ok(pageResult);
    }
}
