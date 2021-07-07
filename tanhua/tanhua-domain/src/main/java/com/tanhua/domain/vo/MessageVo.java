package com.tanhua.domain.vo;

import lombok.Data;

/**
 * 谁点赞我  实体类
 */
@Data
public class MessageVo {
    private String id;
    private String avatar;
    private String nickname;
    private String createDate;
}