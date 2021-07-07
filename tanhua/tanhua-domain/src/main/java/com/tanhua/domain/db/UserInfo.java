package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class UserInfo extends BasePojo{

    //tb_user_info表中的id与tb_user表中的id是一一对应的，所以不能自增
    //添加记录时，必须指定值
    @TableId(type= IdType.INPUT)
    private Long id; //用户id

    private String nickname; //昵称
    private String avatar; //用户头像
    private String birthday; //生日
    private String gender; //性别
    private Integer age; //年龄
    private String city; //城市
    private String income; //收入
    private String education; //学历
    private String profession; //行业
    private Integer marriage; //婚姻状态
    private String tags; //用户标签：多个用逗号分隔
    private String coverPic; // 封面图片
}
