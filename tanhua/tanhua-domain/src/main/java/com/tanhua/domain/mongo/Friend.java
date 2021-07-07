package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 好友关系  tanuan_users 
 *  checkgroup   checkitem  t_checkgroup_checkitem
 */
@Data
@Document(collection = "user_friend")
public class Friend implements Serializable {
    private ObjectId id;
    private Long userId; //用户id
    private Long friendId; //好友id
    private Long created; //时间
}
