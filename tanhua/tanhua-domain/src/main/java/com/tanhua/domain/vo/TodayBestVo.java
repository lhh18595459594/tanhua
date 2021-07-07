package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 与前端数据交互的【今日佳人】实体类。
 */
@Data
public class TodayBestVo implements Serializable {

    private Long id;         //佳人id
    private String avatar;   //头像
    private String nickname; //昵称
    private String gender; //性别 man woman
    private Integer age;      //年龄
    private String[] tags;   //标签
    private Long fateValue; //缘分值
}
