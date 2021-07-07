package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Visitor;

import java.util.List;

public interface VisitorsApi {

    /**
     * 查询所有的访客记录
     * @param loginUserId
     * @param lastTime
     * @return
     */
    List<Visitor> queryVisitors(Long loginUserId, Long lastTime);
}
