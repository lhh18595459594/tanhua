package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;


public interface VideoApi {

    /**
     * 保存小视频
     */
    void save(Video video);

    /**
     * 小视频分页列表查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long page, Long pageSize);

    /**
     * 根据id查询小视频分页列表查询
     * @param page
     * @param pageSize
     * @param uid
     * @return
     */
    PageResult findPageAll(Long page, Long pageSize, Long uid);
}
