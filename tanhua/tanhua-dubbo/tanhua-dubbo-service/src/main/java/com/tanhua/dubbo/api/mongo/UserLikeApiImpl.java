package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FriendApi friendApi;


    /**
     * 查询用户相互喜欢的好友个数，统计好友数
     *
     * @param loginUserId
     * @return
     */
    public Long countLikeEachOther(Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        long count = mongoTemplate.count(query, Friend.class);

        return count;
    }

    /**
     * 统计【我喜欢的 】用户，统计个数
     *
     * @param loginUserId
     * @return
     */
    public Long countOneSideLike(Long loginUserId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        return mongoTemplate.count(query, UserLike.class);

    }


    /**
     * 统计【我的粉丝】个数
     *
     * @param loginUserId
     * @return
     */
    public Long countFens(Long loginUserId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId));

        return mongoTemplate.count(query, UserLike.class);

    }

    /**
     * 分页查询相互喜欢的
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     */
    public PageResult findPageLikeEachOther(Long loginUserId, Long page, Long pagesize) {

        test();
        // 构建查询条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        // 查总数
        long total = mongoTemplate.count(query, Friend.class);
        List<RecommendUser> recommendUserList = new ArrayList<>();
        // 总数>0 查询结果
        if(total > 0) {
            // 分页
            query.skip((page-1) * pagesize).limit(pagesize.intValue());
            // 按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            // 查询结果集
            List<Friend> friendList = mongoTemplate.find(query, Friend.class);
            // 取出对方的用户id
            List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
            // 查询登陆用户与对方的缘分, 查询推荐表
            Query recommendUserQuery = new Query(Criteria.where("toUserId")
                    .is(loginUserId).and("userId").in(friendIds));
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);


        }


        return PageResult.pageResult(total,page,pagesize,recommendUserList);
    }


    public void test(){
        ReceivingVoice voice=new ReceivingVoice();
        voice.setId(new ObjectId());
        voice.setUserId(2);
        voice.setAvatar("null");
        voice.setNickname("我叼你妈的");
        voice.setSoundUrl("http.baidu.com");
        voice.setRemainingTimes(10);

        mongoTemplate.save(voice);
    }

    /**
     * 分页查询我喜欢的
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     */
    public PageResult findPageOneSideLike(Long loginUserId, Long page, Long pagesize) {
        // 构建查询条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        // 查总数
        long total = mongoTemplate.count(query, UserLike.class);
        List<RecommendUser> recommendUserList = new ArrayList<>();
        // 总数>0 查询结果
        if(total > 0) {
            // 分页
            query.skip((page-1) * pagesize).limit(pagesize.intValue());
            // 按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            // 查询结果集
            List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
            // 取出对方的用户id
            List<Long> likeUserIds = userLikeList.stream().map(UserLike::getLikeUserId).collect(Collectors.toList());
            // 查询登陆用户与对方的缘分, 查询推荐表
            Query recommendUserQuery = new Query(Criteria.where("toUserId")
                    .is(loginUserId).and("userId").in(likeUserIds));
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        return PageResult.pageResult(total,page,pagesize,recommendUserList);
    }

    /**
     * 分页查询喜欢我的粉丝
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findPageFens(Long loginUserId, Long page, Long pagesize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId));

        //查询总记录数
        long total = mongoTemplate.count(query, UserLike.class);

        List<RecommendUser> recommendUserList = new ArrayList<>();

        // 总数>0 查询结果
        if (total > 0) {
            // 分页
            query.skip((page - 1) * pagesize).limit(pagesize.intValue());
            // 按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            // 查询结果集
            List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
            // 取出对方的用户id
            List<Long> userIds = userLikeList.stream().map(UserLike::getUserId).collect(Collectors.toList());
            // 查询登陆用户与对方的缘分, 查询推荐表
            Query recommendUserQuery = new Query(Criteria.where("toUserId")
                    .is(loginUserId).and("userId").in(userIds));
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        return PageResult.pageResult(total, page, pagesize, recommendUserList);
    }

    /**
     * 分页查询谁看过我 列表
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findPageMyVisitors(Long loginUserId, Long page, Long pagesize) {
        // 构建查询条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        // 查总数
        long total = mongoTemplate.count(query, Visitor.class);
        List<RecommendUser> recommendUserList = new ArrayList<>();
        // 总数>0 查询结果
        if (total > 0) {
            // 分页
            query.skip((page - 1) * pagesize).limit(pagesize.intValue());
            // 按时间降序
            query.with(Sort.by(Sort.Order.desc("date")));
            // 查询结果集
            List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
            // 取出对方的用户id
            List<Long> visitorIds = visitorList.stream().map(Visitor::getVisitorUserId).collect(Collectors.toList());
            // 查询登陆用户与对方的缘分, 查询推荐表
            Query recommendUserQuery = new Query(Criteria.where("toUserId")
                    .is(loginUserId).and("userId").in(visitorIds));
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        return PageResult.pageResult(total, page, pagesize, recommendUserList);
    }



    /**
     * 粉丝中的喜欢
     *
     * @param fansId
     */
    public Boolean fansLike(Long loginUserId, Long fansId) {
        // 先判断对方是否也喜欢我
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId).and("userId").is(fansId));

        //如果存在
        if (mongoTemplate.exists(query, UserLike.class)) {
            // 是 则可以交友,true
            // 删除粉丝的喜欢的记录
            mongoTemplate.remove(query, UserLike.class);

            // 添加好友记录
            friendApi.add(loginUserId, fansId);

            return true;
        } else {
            // 不是，单方喜欢，添加喜欢记录
            UserLike userLike = new UserLike();
            userLike.setLikeUserId(fansId);
            userLike.setUserId(loginUserId);
            userLike.setCreated(System.currentTimeMillis());
            mongoTemplate.insert(userLike);
        }

        return false;
    }

}
