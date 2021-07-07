package com.tanhua.domain.db;

import lombok.Data;

/**
 * 与数据库查询交互的 【黑名单】 实体类
 */
@Data
public class BlackList extends BasePojo{
    private Long id;
    private Long userId;
    private Long blackUserId;
}
