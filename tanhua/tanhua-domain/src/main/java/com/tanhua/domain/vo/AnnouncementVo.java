package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementVo {
    private String id;        //编号
    private String title;     //标题
    private String description;  //内容
    private String createDate;
}
