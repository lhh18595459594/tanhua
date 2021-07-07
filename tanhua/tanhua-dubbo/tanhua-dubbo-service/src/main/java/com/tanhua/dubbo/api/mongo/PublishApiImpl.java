package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.RecommendQuanzi;
import com.tanhua.domain.mongo.TimeLine;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.utils.IdService;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 动态业务 服务提供者
 */
@Service
public class PublishApiImpl implements PublishApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    /**
     * 发布动态
     *
     * @param publish
     */
    public String add(Publish publish) {
        publish.setCreated(System.currentTimeMillis());
        //设置id
        publish.setId(new ObjectId());

        //设置pid，给推荐系统用
        Long pid = idService.nextId("quanzi_publish");
        publish.setPid(pid);

        //1.添加动态
        mongoTemplate.insert(publish);

        //2.查询好友列表
        Long loginUserId = publish.getUserId();
        Query query = new Query();
        //查询条件，userId=loginUserId
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        List<Friend> friendsList = mongoTemplate.find(query, Friend.class);

        //自己的时间线表添加记录，实现我的相册功能，查询自己发布的动态
        String collectionName = "quanzi_time_line_" + loginUserId;
        TimeLine timeLine = new TimeLine();
        timeLine.setCreated(System.currentTimeMillis());
        timeLine.setPublishId(publish.getId());
        //发布动态的作者id
        timeLine.setUserId(loginUserId);
        mongoTemplate.insert(timeLine, collectionName);


        //3.给好友的时间线添加记录
        if (CollectionUtils.isNotEmpty(friendsList)) {

            //待优化，使用RocketMQ
            for (Friend friend : friendsList) {
                //好友的时间线表名
                collectionName = "quanzi_time_line_" + friend.getFriendId();
                timeLine = new TimeLine();
                timeLine.setCreated(System.currentTimeMillis());
                timeLine.setPublishId(publish.getId());
                //发布动态的作者id
                timeLine.setUserId(loginUserId);

                mongoTemplate.insert(timeLine, collectionName);
            }
        }
        return publish.getId().toHexString();
    }


    /**
     * 分页查询好友动态Publish
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findFriendPublishByTimeline(Long loginUserId, Long page, Long pageSize) {
        //先查询登录用户的时间线表
        String collectionName = "quanzi_time_line_" + loginUserId;

        // 构建查询的条件
        Query timeLineQuery = new Query();

        //2.查出登录用户有多少个好友
        long total = mongoTemplate.count(timeLineQuery, collectionName);

        List<Publish> publishList = new ArrayList<>();

        //3. 总数>0，查询结果集, 时间线表的结果，
        if (total > 0) {
            timeLineQuery.limit(pageSize.intValue()).skip((page - 1) * pageSize);
            //按创建的时间降序
            timeLineQuery.with(Sort.by(Sort.Order.desc("created")));

            //时间线表数据（publishId）
            List<TimeLine> timeLineList = mongoTemplate.find(timeLineQuery, TimeLine.class, collectionName);

            //获取所有的发布动态id
            List<ObjectId> publishIds = timeLineList.stream().map(TimeLine::getPublishId).collect(Collectors.toList());

            // 通过ids查询动态信息 按发布的时间降序
            Query publishQuery = new Query();
            publishQuery.addCriteria(Criteria.where("_id").in(publishIds));

            // 按发布的时间降序
            publishQuery.with(Sort.by(Sort.Order.desc("created")));
            publishList = mongoTemplate.find(publishQuery, Publish.class);
        }

        //返回PageResult
        return PageResult.pageResult(total, page, pageSize, publishList);
    }


    /**
     * 分页查询推荐动态信息
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findRecommendPublish(Long loginUserId, Long page, Long pageSize) {
        // 先查询推荐表
        // 构建查询的条件
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        // 查总数
        long total = mongoTemplate.count(query, RecommendQuanzi.class);

        List<Publish> publishList = new ArrayList<>();
        // 总数>0，查询结果集, 时间线表的结果，
        if (total > 0) {
            //按发布的时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            // 分页
            query.skip((page - 1) * pageSize).limit(pageSize.intValue());
            // p2: 返回值的类型
            List<RecommendQuanzi> recommendQuanziList = mongoTemplate.find(query, RecommendQuanzi.class);
            if (!CollectionUtils.isEmpty(recommendQuanziList)) {
                // 获取动态的ids集合
                List<ObjectId> publishIds = recommendQuanziList.stream().map(RecommendQuanzi::getPublishId).collect(Collectors.toList());
                // 通过ids查询动态信息 按发布的时间降序
                Query publishQuery = new Query();
                publishQuery.addCriteria(Criteria.where("_id").in(publishIds));
                // 按发布的时间降序
                publishQuery.with(Sort.by(Sort.Order.desc("created")));
                publishList = mongoTemplate.find(publishQuery, Publish.class);
            }
        }

        // 构建pageResult再返回
        PageResult pageResult = new PageResult();
        pageResult.setCounts(total);
        pageResult.setPage(page);
        pageResult.setPages(pageSize);

        long pages = total / pageSize;
        pages += total % pageSize > 0 ? 1 : 0;

        pageResult.setPages(pages);
        pageResult.setItems(publishList);
        return pageResult;
    }


    /**
     * 查询我的动态
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryMyPublishList(Long loginUserId, Long page, Long pageSize) {
        //先查询登录用户的时间线表
        String collectionName = "quanzi_time_line_" + loginUserId;

        // 构建查询的条件
        Query query = new Query();
        //我的相册，是登录用户发布的动态
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        //2.查出登录用户有多少个好友
        long total = mongoTemplate.count(query, collectionName);
        List<Publish> publishList = new ArrayList<>();

        //3. 总数>0，查询结果集, 时间线表的结果，
        if (total > 0) {
            query.limit(pageSize.intValue()).skip((page - 1) * pageSize);
            //按创建的时间降序
            query.with(Sort.by(Sort.Order.desc("created")));

            //时间线表数据（publishId）
            List<TimeLine> timeLineList = mongoTemplate.find(query, TimeLine.class, collectionName);

            //获取所有的发布动态id
            List<ObjectId> publishIds = timeLineList.stream().map(TimeLine::getPublishId).collect(Collectors.toList());

            // 通过ids查询动态信息 按发布的时间降序
            Query publishQuery = new Query();
            publishQuery.addCriteria(Criteria.where("id").in(publishIds));

            // 按发布的时间降序
            publishQuery.with(Sort.by(Sort.Order.desc("created")));
            publishList = mongoTemplate.find(publishQuery, Publish.class);
        }

        //返回PageResult
        PageResult pageResult = new PageResult();
        pageResult.setCounts(total);
        pageResult.setPage(page);
        pageResult.setPages(pageSize);

        long pages = total / pageSize;
        pages += total % pageSize > 0 ? 1 : 0;

        pageResult.setPages(pages);
        pageResult.setItems(publishList);
        return pageResult;
    }


    /**
     * 通过id查询单条动态信息
     *
     * @param publishId
     * @return
     */
    public Publish findById(String publishId) {

        return mongoTemplate.findById(new ObjectId(publishId), Publish.class);

    }

    /**
     * 获取当前用户的所有动态分页列表
     *
     * @param page
     * @param pageSize
     * @param uid
     * @param state
     * @return
     */
    public PageResult findAll(Long page, Long pageSize, Long uid, Integer state) {

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(uid));

        //总记录数
        long total = mongoTemplate.count(query, Publish.class);

        List<Publish> publishList = new ArrayList<>();

        if (total > 0) {
            //分页条件
            query.skip((page - 1) * pageSize).limit(pageSize.intValue());

            //时间降序条件
            query.with(Sort.by(Sort.Order.desc("created")));

            publishList = mongoTemplate.find(query, Publish.class);

        }


        return PageResult.pageResult(total, page, pageSize, publishList);
    }


    /**
     * 更新动态的审核状态
     *
     * @param publishId
     * @param state
     */
    public void updateState(String publishId, int state) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(publishId)));
        Update update = new Update();

        update.set("state", state);

        //更新
        mongoTemplate.updateFirst(query, update, Publish.class);
    }


}
