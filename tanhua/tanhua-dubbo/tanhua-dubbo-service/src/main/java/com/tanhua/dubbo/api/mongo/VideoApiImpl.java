package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;


@Service
public class VideoApiImpl implements VideoApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 保存小视频
     */
    public void save(Video video) {
        mongoTemplate.insert(video);
    }

    /**
     * 小视频分页列表查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult findPage(Long page, Long pageSize) {

        Query query = new Query();

        //查看总视频记录数
        long total = mongoTemplate.count(query, Video.class);

        if (total > 0) {
            Query queryPage = new Query();
            //添加分页条件
            queryPage.skip((page - 1) * pageSize).limit(pageSize.intValue());

            //按时间降序条件
            queryPage.with(Sort.by(Sort.Order.desc("created")));

            List<Video> videoList = mongoTemplate.find(query, Video.class);

        }
        //设置分页结果到pageResult
        PageResult<Video> pageResult = new PageResult<>();
        //设置当前页码
        pageResult.setPage(page);
        //设置每页显示条数
        pageResult.setPagesize(pageSize);
        //设置总记录数
        pageResult.setCounts(total);
        //设置总页数
        Long pages = total / pageSize;
        pages += total % pageSize > 0 ? 1 : 0;

        pageResult.setPages(pages);

        return pageResult;
    }

    @Override
    public PageResult findPageAll(Long page, Long pageSize, Long uid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(uid));

        //查看总视频记录数
        long total = mongoTemplate.count(query, Video.class);

        if (total > 0) {

            //添加分页条件
            query.skip((page - 1) * pageSize).limit(pageSize.intValue());

            //按时间降序条件
            query.with(Sort.by(Sort.Order.desc("created")));

            List<Video> videoList = mongoTemplate.find(query, Video.class);

        }
        //设置分页结果到pageResult
        PageResult<Video> pageResult = new PageResult<>();
        //设置当前页码
        pageResult.setPage(page);
        //设置每页显示条数
        pageResult.setPagesize(pageSize);
        //设置总记录数
        pageResult.setCounts(total);
        //设置总页数
        Long pages = total / pageSize;
        pages += total % pageSize > 0 ? 1 : 0;

        pageResult.setPages(pages);

        return pageResult;
    }
}
