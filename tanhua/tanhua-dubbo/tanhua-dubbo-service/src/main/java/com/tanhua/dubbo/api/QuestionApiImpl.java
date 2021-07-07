package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 服务提供者,需要发布服务
 */

@Service
public class QuestionApiImpl implements QuestionApi {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 通过id查询用户的陌生人问题表
     *
     * @param id
     * @return
     */
    public Question findByUserId(Long id) {

        QueryWrapper<Question> queryWrapper = new QueryWrapper();

        queryWrapper.eq("user_id", id);

        return questionMapper.selectOne(queryWrapper);

    }


    /**
     * 添加陌生人问题
     * @param question
     */
    public void save(Question question) {

        //1.通过用户id查询陌生人问题
        Question questionInDb = findByUserId(question.getUserId());


        //2.问题是否存在，不存在则添加
        if (null == questionInDb) {
            questionMapper.insert(question);
        }else {

            //3.存在则更新
            //需要先获取数据库中question表的Id,才能根据Id更新
            question.setId(questionInDb.getId());
            questionMapper.updateById(question);
        }


    }
}
