package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 【访客，谁看过我】 实体类
 */
@Data
public class VisitorVo implements Serializable {
    private Long id;          //访客的id
    private String avatar;     //头像
    private String nickname;   //昵称
    private String gender;     //性别
    private Integer age;       //年龄
    private String[] tags;     //标签
    private Integer fateValue;   //缘分值
}