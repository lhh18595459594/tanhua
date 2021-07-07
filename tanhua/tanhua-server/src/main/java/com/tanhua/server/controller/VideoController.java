package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.server.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/smallVideos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 发布小视频
     *
     * @param videoThumbnail 视频封面文件
     * @param videoFile      视频文件
     * @return
     */
    @PostMapping("/smallVideos")
    public ResponseEntity post(MultipartFile videoThumbnail, MultipartFile videoFile) {


        videoService.post(videoThumbnail, videoFile);

        return ResponseEntity.ok(null);
    }


    /**
     * 小视频分页列表查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/smallVideos")
    public ResponseEntity findPage(@RequestParam(defaultValue = "1") Long page,
                                   @RequestParam(defaultValue = "10") Long pageSize) {

        //防止无意义查询
        page = page < 1 ? 1 : page;

        PageResult<VideoVo> pageResult = videoService.findPage(page, pageSize);

        return ResponseEntity.ok(pageResult);
    }


    /**
     * 关注视频用户
     *
     * @param followUserId 发布视频的作者id
     * @return
     */
    @PostMapping("/{followUserId}/userFocus")
    public ResponseEntity followUser(@PathVariable Long followUserId) {

        videoService.followUser(followUserId);
        return ResponseEntity.ok(null);
    }


    /**
     * 取消关注视频用户
     *
     * @param followUserId 发布视频的作者Id
     * @return
     */
    @PostMapping("/{followUserId}/userUnFocus")
    public ResponseEntity UnfollowUser(@PathVariable Long followUserId) {

        videoService.UnfollowUser(followUserId);
        return ResponseEntity.ok(null);
    }

}
