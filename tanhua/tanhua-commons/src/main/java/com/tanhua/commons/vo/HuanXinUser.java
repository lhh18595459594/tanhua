package com.tanhua.commons.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 环信即时通讯 专用
 */
@Data
@AllArgsConstructor
public class HuanXinUser implements Serializable {

    private String username;
    private String password;
    private String nickname;
}