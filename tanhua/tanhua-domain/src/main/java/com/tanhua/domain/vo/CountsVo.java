package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * “我的模块”  互相喜欢、喜欢、粉丝 的实体类
 */
@Data
public class CountsVo implements Serializable {
    private Long eachLoveCount; //互相喜欢
    private Long loveCount; //喜欢
    private Long fanCount; //粉丝
}