package com.tanhua.manage.domain;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin{



    private Long id;  //id

    private String username; //用户名

    private String password; //密码

    private String avatar;   //头像

    @TableField(exist = false)
    private String token;
}
