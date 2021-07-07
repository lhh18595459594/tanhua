package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 灵魂测试【问卷列表】
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireVo implements Serializable {

    //问卷编号
    private String id;

    //问卷名称
    private String name;

    //封面
    private String cover;

    //问卷等级
    private String level;

    //问卷星级
    private Integer star;

    //试题
    private List<SoulQuestionVo> questions;

    private Integer isLock;//是否锁住:0未锁住 1锁住(锁住的不可以点开)

    private String reportId;//最新报告编号(如果有的话，提交过的才会有)
}
