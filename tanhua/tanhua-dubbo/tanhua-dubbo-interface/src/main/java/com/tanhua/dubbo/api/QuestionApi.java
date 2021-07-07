package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Question;

public interface QuestionApi {

    /**
     * 通过id查询用户的陌生人问题表
     * @param id
     * @return
     */
    Question findByUserId(Long id);

    /**
     * 保存陌生人问题
     * @param question
     */
    void save(Question question);
}
