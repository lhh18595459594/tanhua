package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentApiImpl implements CommentApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存点赞
     *
     * @param comment
     * @return
     */
    public long save(Comment comment) {
        //1. 通过动态id来获取作者id
        // 1.1 构建动态表的查询条件
        ObjectId publishId = comment.getTargetId();
        Query publishQuery = new Query(Criteria.where("_id").is(publishId));

        // 1.2 查询动态表,获取作者id
        Publish publish = mongoTemplate.findOne(publishQuery, Publish.class);
        Long authorId = publish.getUserId();
        comment.setTargetUserId(authorId);

        //2. 补充创建时间
        comment.setCreated(System.currentTimeMillis());

        //3. 往评论表添加点赞数据
        mongoTemplate.insert(comment);

        //4. 更新动态的点赞数
        //  4.1 构建动态的条件, 上面已经构建过了，可重用
        //  4.2 使用Update.inc
        Update update = new Update();
        update.inc(comment.getCol(), 1); // 点赞数自增1
        mongoTemplate.updateFirst(publishQuery, update, Publish.class);

        //5. 查询最新的点赞数
        Publish after = mongoTemplate.findOne(publishQuery, Publish.class);

        // 3-喜欢
        if (comment.getCommentType() == 3) {
            // 返回喜欢数量
            return after.getLoveCount();
        }
        //6. 返回
        return after.getLikeCount();
    }

    /**
     * 取消点赞
     *
     * @param comment
     * @return
     */
    public Integer remove(Comment comment) {
        //1.获取动态信息的id
        ObjectId publishId = comment.getTargetId();

        //2. 删除评论表中的记录，3个条件，targetId, commentType, userId
        Query query = new Query();
        query.addCriteria(Criteria.where("targetId").is(publishId)
                .and("commentType").is(comment.getCommentType())
                .and("userId").is(comment.getUserId()));

        //3.调用方法删除表中的点赞记录
        mongoTemplate.remove(query, Comment.class);

        //4.更新动态表"quanzi_publish"的点赞数减1
        Query publishQuery = new Query();

        publishQuery.addCriteria(Criteria.where("_id").is(publishId));
        Update update = new Update();
        update.inc(comment.getCol(), -1);  // 点赞数自减1

        mongoTemplate.updateFirst(publishQuery, update, Publish.class);

        //5.再次查询动态表"quanzi_publish",得到最新的点赞数
        Publish after = mongoTemplate.findOne(publishQuery, Publish.class);

        if (comment.getCommentType() == 3) {
            // 返回喜欢数量
            return after.getLoveCount();
        }
        return after.getLikeCount();
    }

    /**
     * 查询评论列表
     *
     * @param page
     * @param pageSize
     * @param pulishId
     * @return
     */
    public PageResult findPage(Long page, Long pageSize, String pulishId) {

        //1.设置好查询条件, 表中的"targetId"=这里的pulishId
        //                  评论类型 = 2；
        Query query = new Query();
        query.addCriteria(Criteria.where("targetId").is(pulishId)
                .and("commentType").is(2));

        //2.查询出总记录数
        long total = mongoTemplate.count(query, Comment.class);

        List<Comment> commentList = new ArrayList<>();

        if (total > 0) {
            //3.设置分页条件
            query.skip((page - 1) * pageSize).limit(pageSize.intValue());

            //4.按时间降序条件
            query.with(Sort.by(Sort.Order.desc("created")));

            //5.查询
            commentList = mongoTemplate.find(query, Comment.class);

        }
        //6.设置pageResult的信息
        PageResult pageResult = new PageResult();
        pageResult.setCounts(total);
        pageResult.setPage(page);
        pageResult.setPages(pageSize);

        long Pages = total / pageSize;
        Pages += total % pageSize > 0 ? 1 : 0;

        pageResult.setPages(Pages);
        pageResult.setItems(commentList);
        return pageResult;
    }

    /**
     * 按登陆用户id 通过commentType分页查询 评论信息
     *
     * @param loginUserId
     * @param commentType
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findPageByUserId(Long loginUserId, int commentType, Long page, Long pageSize) {

        Query query = new Query();

        //1.编写查询条件  targetUserId——发布者的id   commentType——评论类型
        query.addCriteria(Criteria.where("targetUserId").is(loginUserId)
                .and("commentType").is(commentType));

        //2.查询总记录数
        long total = mongoTemplate.count(query, Comment.class);

        List<Comment> commentList = new ArrayList<>();


        if (total > 0) {
            query.skip((page - 1) * pageSize).limit(pageSize.intValue());
            query.with(Sort.by(Sort.Order.desc("created")));

            // 查询结果集
            commentList = mongoTemplate.find(query, Comment.class);
        }

        PageResult<Comment> pageResult = new PageResult<>();
        pageResult.setItems(commentList);
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setCounts(total);

        Long pages = total / pageSize;

        pages += total % pageSize > 0 ? 1 : 0;
        pageResult.setPages(pages);


        return pageResult;
    }


}
