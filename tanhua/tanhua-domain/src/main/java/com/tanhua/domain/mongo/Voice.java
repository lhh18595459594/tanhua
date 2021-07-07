package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;


@Data
@Document(collection = "voice")
public class Voice implements Serializable {
    @Id
    private ObjectId id;   //主键id

    @Indexed
    private Long userId;   //用户id

    private String voiceUrl;  //语音路径

    private Long created;   //创建时间

    private Long update;     //更新时间

}
