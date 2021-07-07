package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitor;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VisitorsApiImpl implements VisitorsApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询所有的访客记录
     *
     * @param loginUserId
     * @param lastTime
     * @return
     */
    public List<Visitor> queryVisitors(Long loginUserId, Long lastTime) {

        //1.查询visitors条件userId=登陆用户id
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        //2.有时间，date>上次的时间，如果没有时间，则不需要条件
        if (null != lastTime) {
            //转换类型
            Long date = lastTime;
            // date>=记录的时间 ,gte: 大于等于
            query.addCriteria(Criteria.where("date").gte(date));
        }
        // 最近的，时间降序
        query.with(Sort.by(Sort.Order.desc("date")));

        //- 取5条
        query.limit(5);

        //查询出所有的访客记录
        List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);

        // ============= 查询访客的缘分值 ===================
        //- 遍历访客的id, 查询访客的缘分值recommendUser（默认值70），补充到实体类中，再返回
        if (CollectionUtils.isNotEmpty(visitorList)) {

            List<Long> visitorUserIds = visitorList.stream().map(Visitor::getVisitorUserId).collect(Collectors.toList());
            //查询推荐表
            Query recommendUserQuery = new Query();
            recommendUserQuery.addCriteria(Criteria.where("userId").in(visitorUserIds));
            recommendUserQuery.addCriteria(Criteria.where("toUserId").is(loginUserId));

            //获取推荐表的所有人员信息
            List<RecommendUser> recommendUsers = mongoTemplate.find(recommendUserQuery, RecommendUser.class);

            //创建map。用来存储每个访客的缘分值
            Map<Long, Double> scoreMap = new HashMap<>();

            if (CollectionUtils.isNotEmpty(recommendUsers)) {
                scoreMap.putAll(recommendUsers.stream().collect(Collectors.toMap(RecommendUser::getUserId, RecommendUser::getScore)));
            }

            //补全缘分值
            visitorList.forEach(visitor -> {
                Double score = scoreMap.get(visitor.getVisitorUserId());
                if (null == score) {
                    //如果没有缘分值，则给一个默认的
                    score = 70d;
                }
                visitor.setScore(score);
            });
        }
        return visitorList;
    }
}
