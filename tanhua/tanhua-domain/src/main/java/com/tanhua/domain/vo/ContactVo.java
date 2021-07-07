package com.tanhua.domain.vo;
import lombok.Data;

/**
 * 联系人列表 实体类
 */
@Data
public class ContactVo {
    private Long id;            //用户id
    private String userId;     //联系人id
    private String avatar;     //头像
    private String nickname;   //昵称
    private String gender;     //性别
    private Integer age;       //年龄
    private String city;       //城市
}