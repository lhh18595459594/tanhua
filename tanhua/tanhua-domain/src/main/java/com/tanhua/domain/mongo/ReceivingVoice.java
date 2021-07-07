package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "ReceivingVoice")
public class ReceivingVoice implements Serializable {

    @Id
    private ObjectId id; //主键id

    private Integer userId; //用户id

    private String avatar; //头像

    private String nickname; //昵称

    private String gender; //性别

    private Integer age; //年龄

    private String soundUrl;//语音地址

    private Integer remainingTimes; //剩余次数

}
