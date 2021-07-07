package com.tanhua.domain.db.Linhunceshi;


import com.tanhua.domain.db.BasePojo;
import lombok.Data;

/**
 * 灵魂测试【报告表】
 */
@Data
public class QuestionNaireReport extends BasePojo {

    //报告表的id
    private Long id;

    //用户id
    private Long userId;

    //问卷编号
    private Long questionnaireId;

    //报告id
    private Long reportId;


    //结果封面图
    private String cover;

    //结果的文字
    private String content;

    //外向得分
    private String extroversion;

    //判断得分
    private String judgement;

    //抽象得分
    private String abstraction;

    //理性得分
    private String retionality;

}
