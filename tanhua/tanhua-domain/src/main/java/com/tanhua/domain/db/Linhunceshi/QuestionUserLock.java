package com.tanhua.domain.db.Linhunceshi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tanhua.domain.db.BasePojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户对应问卷的锁表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionUserLock extends BasePojo {

  //该表的id
   private Long id;

   //用户id
   private Long userId;

   //问卷编号
   private Long questionnaireId;

   //是否锁住（0解锁，1锁住）
    private Integer isLock;

   //最新报告id,数据库中的是Long类型，前端需要传String类型，记得转换
   private Long reportId;


}
