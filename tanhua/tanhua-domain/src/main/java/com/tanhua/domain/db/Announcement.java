package com.tanhua.domain.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 【公告管理】实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BasePojo{
    private String id;
    private String title;
    private String description;
}
