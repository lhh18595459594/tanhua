package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class FollowUserApiImpl implements FollowUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 关注视频用户
     *
     * @param followUser
     */
    public void save(FollowUser followUser) {

        mongoTemplate.insert(followUser);
    }


    /**
     * 取消关注视频用户
     *
     * @param followUser
     */
    public void remove(FollowUser followUser) {

        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(followUser.getUserId())
                .and("followUserId").is(followUser.getFollowUserId()));

        mongoTemplate.remove(query, FollowUser.class);
    }
}
