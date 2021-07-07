package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 试题实体类 vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoulQuestionVo {

    //题目id
    private String id;

    //问卷题目内容
    private String question;

    //问卷选项
    List<SoulQuestionOptionVo> options;
}
