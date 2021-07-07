package com.tanhua.domain.vo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 探花传音， 与前端交互的实体类
 */
@Data
public class ReceivingVoiceVo implements Serializable {

    private Integer id;  //用户id
    private String avatar; //头像
    private String nickname; //昵称
    private String gender; //性别
    private Integer age; //年龄
    private String soundUrl;//语音地址
    private Integer remainingTimes; //剩余次数

}
