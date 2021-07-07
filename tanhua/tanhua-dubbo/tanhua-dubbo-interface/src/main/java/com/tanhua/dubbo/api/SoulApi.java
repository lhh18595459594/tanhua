package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Linhunceshi.*;
import com.tanhua.domain.vo.SoulQuestionOptionVo;
import com.tanhua.domain.vo.SoulQuestionVo;

import java.util.List;

public interface SoulApi {


    /**
     * 查出问卷列表的 【编号、名称、等级、星级、封面】
     * @return
     */
    List<Questionnaire> findAll();

    /**
     * 查出每套问卷的【编号、试题】
     * @return
     */
    List<SoulQuestionVo> findQuestions();

    /**
     * 查出每个试题的【选项编号、选项内容】
     * @return
     */
    List<SoulQuestionOptionVo> findOptions();

    /**
     * 根据用户id，查询对应的锁表
     * @param loginUserId
     * @return
     */
    List<QuestionUserLock> findUserLockById(Long loginUserId);

    /**
     * 查询所有的试题
     * @return
     */
    List<SoulQuestion> findAllQuestion();

    /**
     * 查询所有的 【题目选项内容】
     * @return
     */
    List<SoulQuestionOption> findAllOption();

    /**
     * 查询选项编号id对应的分数，计算总分数
     * @param optionId
     * @return
     */
    SoulQuestionOption findScoreById(Long optionId);

    /**
     * 根据题目编号，查询出用户的问卷id
     * @param
     * @param questionId
     * @return
     */
    SoulQuestion findQuestionNaireIdByQuestionId(Long questionId);

    /**
     * 根据用户id，问卷id， 查出锁卷状态。
     *
     * @param loginUserId
     * @param questionnaireId
     * @return
     */
    QuestionUserLock SelectQuesitonUserLock(Long loginUserId, Long questionnaireId);

    /**
     * 更新，将对应的下一个问卷的锁打开
     * @param loginUserId
     * @param questionnaireId
     * @return
     */
    QuestionUserLock UpdateQuesitonUserLock(Long loginUserId, Long questionnaireId);

    /**
     * 根据问卷id，查询结果表中的数据List集合
     * @param questionnaireId1
     * @param
     * @return
     */
    List<QuestionNaireResult> findResult(Long questionnaireId1);

    /**
     * 添加报告表的数据
     * @param report
     */
    void addReport(QuestionNaireReport report);


    /**
     * 根据 报告ID，登录用户id，查出记录
     * @param reportId1
     * @param loginUserId
     * @return
     */
    List<QuestionNaireReport> findList(Long reportId1, Long loginUserId);

    /**
     * 根据问卷id，用户id，查出单条记录
     * @param loginUserId
     * @param questionnaireId
     * @return
     */
    QuestionNaireReport findOne(Long loginUserId, Long questionnaireId);

    /**
     * 当前的问卷状态设置reportId。
     * @param userLock
     * @param
     */
    void SetReportId(QuestionUserLock userLock);
}
