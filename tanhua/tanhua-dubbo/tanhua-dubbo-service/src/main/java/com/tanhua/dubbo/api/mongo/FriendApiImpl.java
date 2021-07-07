package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.Voice;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加好友 业务层
 */
@Service
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 添加好友
     *
     * @param loginUserId
     * @param friendId
     */
    public void add(Long loginUserId, Long friendId) {

        Query query = new Query();

        //0.设置查询条件
        query.addCriteria(Criteria.where("userid").is(loginUserId)
                .and("friendId").is(friendId));

        //1、查询好友关系是否存在
        boolean flag = mongoTemplate.exists(query, Friend.class);

        //2.不存在，则添加好友关系
        if (!flag) {
            Friend friend = new Friend();
            friend.setUserId(loginUserId);
            friend.setFriendId(friendId);
            friend.setCreated(System.currentTimeMillis());
            //save的操作： 对象如果包含已存在的_id，则进行更新的操作。_id不存在则会插入新记录
            //insert的操作：只会插入，如果_id相同，则报错
            mongoTemplate.insert(friend);
        }


    }

    /**
     * 查询好友列表
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findPage(Long loginUserId, Long page, Long pageSize, String keyword) {

        Query query = new Query();

        //1.设置查询条件 . 数据库中的userId=这里的loginUserId
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        //2.查询总记录数
        long total = mongoTemplate.count(query, Friend.class);

        List<Friend> friendList = new ArrayList<>();

        if (total > 0) {
            //3.设置分页条件
            query.skip((page - 1) * pageSize).limit(pageSize.intValue());

            //4.设置添加好友，降序
            query.with(Sort.by(Sort.Order.desc("created")));

            //5.查询出所以的好友集合
            friendList = mongoTemplate.find(query, Friend.class);
        }

        //6.设置PageResult
        PageResult<Friend> pageResult = new PageResult<>();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setCounts(total);

        Long pages = total / pageSize;

        pages += total % pageSize > 0 ? 1 : 0;

        pageResult.setPages(pages);
        pageResult.setItems(friendList);

        return pageResult;
    }

}
