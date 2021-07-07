package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tanhua.domain.db.Linhunceshi.*;
import com.tanhua.domain.vo.SoulQuestionOptionVo;
import com.tanhua.domain.vo.SoulQuestionVo;
import com.tanhua.dubbo.mapper.*;
import org.apache.dubbo.config.annotation.Service;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SoulApiImpl implements SoulApi {

    @Autowired
    private SoulMapper soulMapper;

    @Autowired
    private SoulQuestionLockMapper soulQuestionLockMapper;

    @Autowired
    private SoulQuestionMapper soulQuestionMapper;

    @Autowired
    private SoulQuestionOptionMapper soulQuestionOptionMapper;

    @Autowired
    private SoulQuestionNaireResultMapper soulQuestionNaireResultMapper;

    @Autowired
    private SoulQuestionNaireReportMapper soulQuestionNaireReportMapper;

    /**
     * 查出问卷列表的 【编号、等级、星级、封面】
     *
     * @return
     */
    public List<Questionnaire> findAll() {
        QueryWrapper queryWrapper = new QueryWrapper();
        return soulMapper.selectList(queryWrapper);

    }

    /**
     * 查出每套问卷的【编号、试题】
     *
     * @return
     */
    public List<SoulQuestionVo> findQuestions() {
        QueryWrapper queryWrapper = new QueryWrapper();

        return soulMapper.selectList(queryWrapper);
    }

    /**
     * 查出每个试题的【选项编号、选项内容】
     *
     * @return
     */
    public List<SoulQuestionOptionVo> findOptions() {
        QueryWrapper queryWrapper = new QueryWrapper();

        return soulMapper.selectList(queryWrapper);

    }

    /**
     * 根据用户id，查询对应的锁表
     *
     * @param loginUserId
     * @return
     */
    public List<QuestionUserLock> findUserLockById(Long loginUserId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", loginUserId);

        return soulQuestionLockMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有试题
     *
     * @return
     */
    public List<SoulQuestion> findAllQuestion() {
        QueryWrapper queryWrapper = new QueryWrapper();
        return soulQuestionMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有的 【题目选项内容】
     *
     * @return
     */
    public List<SoulQuestionOption> findAllOption() {
        QueryWrapper queryWrapper = new QueryWrapper();
        return soulQuestionOptionMapper.selectList(queryWrapper);

    }

    /**
     * 查询选项编号id对应的分数，计算总分数
     *
     * @param optionId
     * @return
     */
    public SoulQuestionOption findScoreById(Long optionId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", optionId);
        //因为在循环中，一次只查一条记录。
        return soulQuestionOptionMapper.selectOne(queryWrapper);

    }

    /**
     * 根据题目编号，查询出用户的问卷id
     *
     * @param
     * @param questionId
     * @return
     */
    public SoulQuestion findQuestionNaireIdByQuestionId(Long questionId) {
        QueryWrapper queryWrapper = new QueryWrapper();

        queryWrapper.eq("id", questionId);

        return soulQuestionMapper.selectOne(queryWrapper);
    }

    /**
     * 根据用户id，问卷id， 查出锁卷状态。
     *
     * @param loginUserId
     * @param questionnaireId
     * @return
     */
    public QuestionUserLock SelectQuesitonUserLock(Long loginUserId, Long questionnaireId) {
        //先查出该用户正在做的问卷状态
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", loginUserId);

        queryWrapper.eq("questionnaire_id", questionnaireId);

        QuestionUserLock userLock = soulQuestionLockMapper.selectOne(queryWrapper);


        return userLock;
    }

    /**
     * 更新，将对应的下一个问卷的锁打开
     *
     * @param loginUserId
     * @param questionnaireId
     * @return
     */
    public QuestionUserLock UpdateQuesitonUserLock(Long loginUserId, Long questionnaireId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", loginUserId);
        queryWrapper.eq("questionnaire_id", questionnaireId);

        QuestionUserLock userLock1 = soulQuestionLockMapper.selectOne(queryWrapper);

        userLock1.setIsLock(0);
        soulQuestionLockMapper.updateById(userLock1);
        System.out.println("userLock1========================================="+userLock1);
        return userLock1;
    }


    /**
     * 根据问卷id，查询结果表中的数据List集合
     *
     * @param questionnaireId1
     * @param
     * @return
     */
    public List<QuestionNaireResult> findResult(Long questionnaireId1) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("questionnaire_id", questionnaireId1);

        return soulQuestionNaireResultMapper.selectList(queryWrapper);

    }

    /**
     * 添加报告表的数据
     *
     * @param report
     */
    public void addReport(QuestionNaireReport report) {

        soulQuestionNaireReportMapper.insert(report);
    }

    /**
     * 根据 报告ID，登录用户id，查出记录
     *
     * @param reportId1
     * @param loginUserId
     * @return
     */
    public List<QuestionNaireReport> findList(Long reportId1, Long loginUserId) {
        QueryWrapper queryWrapper = new QueryWrapper();

        queryWrapper.eq("report_id", reportId1);
        queryWrapper.eq("user_id", loginUserId);

        return soulQuestionNaireReportMapper.selectList(queryWrapper);

    }


    /**
     * 根据问卷id，用户id，查出单条记录
     *
     * @param loginUserId
     * @param questionnaireId
     * @return
     */
    public QuestionNaireReport findOne(Long loginUserId, Long questionnaireId) {
        QueryWrapper queryWrapper = new QueryWrapper();

        queryWrapper.eq("questionnaire_id", questionnaireId);
        queryWrapper.eq("user_id", loginUserId);

        return soulQuestionNaireReportMapper.selectOne(queryWrapper);

    }

    /**
     * 当前的问卷状态设置reportId。
     * @param userLock
     * @param
     */
    public void SetReportId(QuestionUserLock userLock) {
        soulQuestionLockMapper.updateById(userLock);

    }
}
