package com.tanhua.domain.db.Linhunceshi;

import com.tanhua.domain.db.BasePojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 灵魂测试最开始的界面（问卷表）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Questionnaire extends BasePojo {

    private Long id;

    //问卷等级:1初级 2中级 3 高级
    private Integer level;

    //问卷名称
    private String name;

    //问卷封面图
    private String cover;

    //题目星级:2,3,5星级
    private  Integer star;


}
