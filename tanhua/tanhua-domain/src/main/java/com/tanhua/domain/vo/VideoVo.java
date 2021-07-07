package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 【小视频】 与前端交互的 实体类
 */
@Data
public class VideoVo implements Serializable {

    private String id;
    private Long userId;   //视频发布者的Id
    private String avatar; //头像
    private String nickname; //昵称
    private String cover; //封面
    private String videoUrl; //视频URL
    private String signature; //签名
    private Integer likeCount; //点赞数量
    private Integer hasLiked; //是否已赞（1是，0否）
    private Integer hasFocus; //是是否关注 （1是，0否）
    private Integer commentCount; //评论数量
    private Integer createDate; //创建时间
}