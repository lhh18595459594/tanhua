package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 【动态信息点赞、评论】  实体类
 */
@Data
@Document(collection = "quanzi_comment")
public class Comment implements Serializable {

    private ObjectId id;

    private ObjectId targetId;    //发布id   如果：targetType=1  , targetUserId=动态的作者id，targetId=动态的id，
                                                 //targetType=2 ,  targetUserId=视频的发布者id，targetId=视频的id，
    private Long targetUserId; // 发布者的id


    private Integer commentType;   //评论类型，1-点赞，2-评论，3-喜欢
    private Integer targetType;       //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
    private String content;        //评论内容
    private Long userId;           //评论人
    private Integer likeCount = 0; //点赞数
    private Long created; //发表时间

    //动态选择更新的字段
    public String getCol() {
        return this.commentType == 1 ? "likeCount" : commentType==2? "commentCount"
            : "loveCount";
    }
}