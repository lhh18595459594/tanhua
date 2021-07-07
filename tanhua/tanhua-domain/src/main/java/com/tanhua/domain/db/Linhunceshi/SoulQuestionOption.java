package com.tanhua.domain.db.Linhunceshi;

import com.tanhua.domain.db.BasePojo;
import lombok.Data;

/**
 * 灵魂测试 【题目选项表】
 */
@Data
public class SoulQuestionOption extends BasePojo {

    //选项id
    private Long id;

    //每个选项的内容
    private String content;

    //选项对应的题目的id
    private Long questionId;

    //
    private String medias;

    //分数
    private String score;
}
