package com.tanhua.domain.db;

import lombok.Data;

/**
 * 与数据库查询交互的 【通知设置】 实体类
 */
@Data
public class Settings extends BasePojo {

    private Long id;
    private Long userId;

    private Boolean likeNotification;    //推送喜欢通知

    private Boolean pinglunNotification; //推送评论通知

    private Boolean gonggaoNotification;  //推送公告通知
}
