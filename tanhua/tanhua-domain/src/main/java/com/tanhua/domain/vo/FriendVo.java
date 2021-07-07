package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 互相喜欢、我喜欢、粉丝、谁看过我的翻页列表  实体类
 */
@Data
public class FriendVo implements Serializable {

    private Long id;
    private String avatar;     //头像
    private String nickname;   //昵称
    private String gender;     //性别
    private Integer age;       //年龄
    private String city;       //城市
    private String education;  //学历
    private Integer marriage; //婚姻状态（0未婚，1已婚）
    private Integer matchRate; //匹配度
    private Boolean alreadyLove; //是否喜欢过
}