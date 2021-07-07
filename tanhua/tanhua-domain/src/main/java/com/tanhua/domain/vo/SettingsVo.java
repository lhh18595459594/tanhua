package com.tanhua.domain.vo;

import lombok.Data;

/**
 *'我的'模块中的  通用设置 实体类
 * 专门用来与前端数据交互的类。
 */
@Data
public class SettingsVo {

    private Integer id;

    private String strangerQuestion;    //设置陌生人问题

    private String phone;                //修改手机号

    private Boolean likeNotification;    //推送喜欢通知

    private Boolean pinglunNotification; //推送评论通知

    private Boolean gonggaoNotification; //推送公告通知
}
