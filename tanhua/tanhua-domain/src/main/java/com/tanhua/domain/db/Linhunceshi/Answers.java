package com.tanhua.domain.db.Linhunceshi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answers {

    //题目编号
    private Long questionId;

    //选项编号
    private Long optionId;
}
