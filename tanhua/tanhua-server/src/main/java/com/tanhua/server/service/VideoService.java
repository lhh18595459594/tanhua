package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FollowUserApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发布视频业务层
 */
@Service
@Slf4j
public class VideoService {

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private VideoApi videoApi;

    @Reference
    private FollowUserApi followUserApi;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private OssTemplate ossTemplate;


    /**
     * 发布小视频
     *
     * @param videoThumbnail 视频封面文件
     * @param videoFile      视频文件
     * @return
     */
    public void post(MultipartFile videoThumbnail, MultipartFile videoFile) {

        try {
            //1.视频封面需要上传到oss
            String pirUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());

            //2.视频文件要上传FastDFS
            //2.1  获取视频的文件名
            String videoFileName = videoFile.getOriginalFilename();

            //2.2 名字后缀不要.
            String suffix = videoFileName.substring(videoFileName.lastIndexOf(".") + 1);

            //2.3开始上传视频文件.
            StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), suffix, null);

            // 小视频 的完整路径，真实开发下要区分开服务地址与文件的相对路径
            // 真实开发下应该存这个相对路径storePath.getFullPath();
            String videoUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();

            //构建video对象
            Video video = new Video();

            //设置视频文件地址
            video.setVideoUrl(videoUrl);

            //设置视频封面文件地址
            video.setPicUrl(pirUrl);

            //设置发布者的id
            video.setUserId(UserHolder.getUserId());

            //设置发布时间
            video.setCreated(System.currentTimeMillis());

            //设置视频配的文字说明
            video.setText("黑马出品");
            // 调用api添加video数据
            videoApi.save(video);
        } catch (IOException e) {
            log.error("上传小视频封面图片失败", e);
            throw new TanHuaException("上传小视频失败");
        }
    }


    /**
     * 小视频分页列表查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<VideoVo> findPage(Long page, Long pageSize) {
        //1.调用api分页查询
        PageResult pageResult = videoApi.findPage(page, pageSize);

        //2.获取所有的小视频的信息
        List<Video> videoList = pageResult.getItems();

        //3.判断videoList是否不为空
        if (!CollectionUtils.isEmpty(videoList)) {
            //4.遍历videoList，取出每一个视频发布者的id，并将他们存进list集合中
            List<Long> userIds = videoList.stream().map(Video::getUserId).collect(Collectors.toList());

            //5.通过这些id，进行批量查询出所有的作者信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(userIds);

            //6.将所有的作者信息，遍历并转成Map集合，
            //key = 作者id ， value = 作者个人信息
            Map<Long, UserInfo> UserInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

            //7.转成VO
            List<VideoVo> voList = videoList.stream().map(video -> {
                //8.构建VO
                VideoVo videoVo = new VideoVo();
                BeanUtils.copyProperties(video, videoVo);
                videoVo.setId(video.getId().toHexString());
                videoVo.setSignature(video.getText());
                videoVo.setCover(video.getPicUrl());

                //将每个作者信息复制给videoVo
                UserInfo userInfo = UserInfoMap.get(videoVo.getUserId());
                BeanUtils.copyProperties(userInfo, videoVo);

                //是否关注
                videoVo.setHasFocus(0);
                String key = "follow_user_" + UserHolder.getUserId() + "_" + video.getUserId();
                if (redisTemplate.hasKey(key)) {
                    //如果redis中存在这个key，就代表已经关注了
                    videoVo.setHasFocus(1);
                }

                //是否点赞
                videoVo.setHasLiked(0);
                return videoVo;
            }).collect(Collectors.toList());


            pageResult.setItems(voList);

        }


        return pageResult;
    }

    /**
     * 关注视频用户
     *
     * @param followUserId
     */
    public void followUser(Long followUserId) {
        //1.构建followUser对象
        FollowUser followUser = new FollowUser();

        //设置登陆用户关注的人的id
        followUser.setFollowUserId(followUserId);

        //设置登陆用户id
        followUser.setUserId(UserHolder.getUserId());

        //设置关注时间
        followUser.setCreated(System.currentTimeMillis());

        //调用save方法，保存关注记录
        followUserApi.save(followUser);

        //记录redis, 当前登陆用户关注了这个作者
        String key = "follow_user_" + UserHolder.getUserId() + "_" + followUserId;

        redisTemplate.opsForValue().set(key, 1);
    }

    /**
     * 取消关注视频用户
     *
     * @param followUserId 发布视频的作者Id
     * @return
     */
    public void UnfollowUser(Long followUserId) {
        //1.构建followUser对象,补全查询需要的条件
        FollowUser followUser = new FollowUser();

        followUser.setUserId(UserHolder.getUserId());
        followUser.setFollowUserId(followUserId);
        followUser.setCreated(System.currentTimeMillis());

        //2.调用api取消关注
        followUserApi.remove(followUser);

        //3.删除redis中的关注标记key
        String key = "follow_user_" + UserHolder.getUserId() + "_" + followUserId;
        redisTemplate.delete(key);

    }
}
