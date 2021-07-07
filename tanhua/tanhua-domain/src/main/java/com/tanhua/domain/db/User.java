package com.tanhua.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class User extends BasePojo{
    private Long id;
    private String mobile; //手机号

    //把User对象转成json字符串时，忽略这个字段,再返回数据时，密码就不用暴露出来
    @JSONField(serialize = false)
    private String password;
}