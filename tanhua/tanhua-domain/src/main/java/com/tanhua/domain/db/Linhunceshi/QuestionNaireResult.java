package com.tanhua.domain.db.Linhunceshi;

import com.tanhua.domain.db.BasePojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * 灵魂测试【结果表】
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionNaireResult extends BasePojo {

    private Long id;

    //用户答的对应的问卷id
    private Long questionnaireId;

    //成绩
    private Long scope;


    //封面图片地址
    private String cover;

    //对应的结论
    private String content;


}
