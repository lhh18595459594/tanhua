package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 抽取BasePojo
 */
@Data
public class BasePojo implements Serializable {

    //插入数据时，自动填充
    @TableField(fill = FieldFill.INSERT)
    private Date created;

    //插入或更新数据时，自动填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;
}
