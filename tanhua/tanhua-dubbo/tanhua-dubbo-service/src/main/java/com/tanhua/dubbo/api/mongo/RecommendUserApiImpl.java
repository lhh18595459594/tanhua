package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

@Service
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 查询缘分值最高的佳人
     *
     * @param loginUserId
     * @return
     */
    public RecommendUser todayBest(Long loginUserId) {

        // 条件：推荐给登陆用户, toUserId=loginUserId
        Query query = new Query();
        query.addCriteria(Criteria.where("toUserId").is(loginUserId));

        // 按分数降序
        query.with(Sort.by(Sort.Order.desc("score")));

        // 查的是recommend_user表 取1个
        RecommendUser todayBest = mongoTemplate.findOne(query, RecommendUser.class);

        return todayBest;
    }

    /**
     * 通过登录用户id, 分页查询推荐用户列表
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findPage(Long loginUserId, Long page, Long pageSize) {

        Query query = new Query();
        //查询条件，让数据库里的toUserId=loginUserId
        query.addCriteria(Criteria.where("toUserId").is(loginUserId));

        //总记录数,根据条件查询一共有多少条记录。
        long total = mongoTemplate.count(query, RecommendUser.class);


        List<RecommendUser> list = Collections.emptyList();

        //只有大于0的时候才需要查询
        if (total > 0) {

            //查询条件，查询分页结果
            query.limit(pageSize.intValue()).skip((page - 1) * pageSize);

            //查询条件，按分数降序排序
            query.with(Sort.by(Sort.Order.desc("score")));

            //执行查询
            list = mongoTemplate.find(query, RecommendUser.class);
        }

        PageResult pageResult = new PageResult();

        //1.设置总记录数
        pageResult.setCounts(total);

        //2.设置当前页
        pageResult.setPage(page);

        //3.设置每页显示条数
        pageResult.setPagesize(pageSize);

        //4.设置总共有多少页，多出来的+1页。
        Long pages = total / pageSize;
        pages += total % pageSize > 0 ? 1 : 0;
        pageResult.setPages(pages);

        //5.设置items列表(包含推荐用户的所有数据)
        pageResult.setItems(list);


        return pageResult;
    }


    /**
     * 查看佳人信息
     *
     * @param userId
     * @return
     */
    public Double queryForScore(Long userId, Long loginUserId) {

        Query query = new Query();

        //1.补充查询条件 userId=userId, toUserId=loginUserId
        query.addCriteria(Criteria.where("userId").is(userId)
                .and("toUserId").is(loginUserId));

        //2.查询条件，降序查询
        query.with(Sort.by(Sort.Order.desc("date")));

        //3.查询单条数据
        RecommendUser user = mongoTemplate.findOne(query, RecommendUser.class);

        if (null != user){
            //4.如果查询结果佳人不为空，则获取她的缘分值
            return user.getScore();
        }

        //如果为空，则给一个默认值
        return 95d;
    }


}
