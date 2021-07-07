package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultVo {


    //结果的文字描述
    private String conclusion;

    //结果封面
    private String cover;

    //维度
    private List<Dimensions> dimensions;

    //与你相似
    private List<SimilarYou> similarYou;
}