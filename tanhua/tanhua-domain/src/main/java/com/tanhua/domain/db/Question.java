package com.tanhua.domain.db;

import lombok.Data;

/**
 *
 * 与数据库查询交互的 【设置陌生人问题】  的实体类
 */
@Data
public class Question extends BasePojo{
    private Long id;
    private Long userId;

    //问题内容
    private String txt;
}
