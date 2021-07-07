package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 与你相似  实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarYou {

    //用户编号
    private Integer id;

    //头像
    private String avatar;
}
