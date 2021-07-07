package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {
    private Long counts; // 总记录数
    private Long pagesize;// 每页大小
    private Long pages;// 总页数
    private Long page;// 页码

    private List<T> items = Collections.emptyList();  //列表，包含所有数据


    public static PageResult pageResult(Long total, Long page, Long pagesize, List items) {
        PageResult pageResult = new PageResult();
        pageResult.setCounts(total);
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        pageResult.setItems(items);

        Long pages = total / pagesize +(total % pagesize > 0 ? 1 : 0);

        pageResult.setPages(pages);

        return pageResult;
    }
}
