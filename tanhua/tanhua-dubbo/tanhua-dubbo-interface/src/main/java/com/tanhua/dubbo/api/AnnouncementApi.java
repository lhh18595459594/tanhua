package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

public interface AnnouncementApi {

    /**
     * 查询公告列表
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long page, Long pageSize);

    /**
     * 根据id批量查询公告信息
     * @param announcementIds
     * @return
     */
    List<Announcement> findByBatchIds(List<String> announcementIds);
}
