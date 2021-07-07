package com.tanhua.domain.db.Linhunceshi;

import com.tanhua.domain.db.BasePojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoulQuestion extends BasePojo {

    //题目id
    private Long id;
    //问卷题目内容
    private String stem;

    //问卷id
    private Long questionnaireId;
}
