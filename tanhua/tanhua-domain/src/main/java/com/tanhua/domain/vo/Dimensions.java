package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * 维度  实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dimensions {

    //维度项（外向，判断，抽象，理性）
    private String key;

    //维度值(80%,70%,90%,60%)
    private String value;
}
