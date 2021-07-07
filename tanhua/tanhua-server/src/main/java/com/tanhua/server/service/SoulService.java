package com.tanhua.server.service;

import com.tanhua.domain.db.Linhunceshi.*;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.SoulApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 灵魂测试【问卷列表】
 */
@Service
@Slf4j
public class SoulService {

    @Reference
    private SoulApi soulApi;


    /**
     * 测灵魂-问卷列表
     *
     * @return
     */
    public List<QuestionnaireVo> findAll() {
        //1.获取用户id
        Long loginUserId = UserHolder.getUserId();

        //2.调用api，查出卷问列表的 【编号、等级、星级、封面】
        List<Questionnaire> questionnaireList = soulApi.findAll();

        //2.1调用api，根据用户id，【查询锁表】.
        List<QuestionUserLock> questionUserLockList = soulApi.findUserLockById(loginUserId);

        //2.2 调用api，查询【所有的试题】
        List<SoulQuestion> questionList = soulApi.findAllQuestion();


        //2.4 调用api,查询所有的 【题目选项内容】
        List<SoulQuestionOption> optionList = soulApi.findAllOption();

        //3.构建问卷列表vo
        List<QuestionnaireVo> questionnaireVoList = new ArrayList<>();

        //4.遍历questionUserLockList锁表，将锁表里的用户Id和锁表状态设置进vo中
        for (QuestionUserLock questionUserLock : questionUserLockList) {
            QuestionnaireVo vo = new QuestionnaireVo();
            vo.setId(questionUserLock.getQuestionnaireId().toString());
            vo.setIsLock(questionUserLock.getIsLock());
            if (questionUserLock.getReportId() != null) {
                vo.setReportId(questionUserLock.getReportId().toString());
            }
            questionnaireVoList.add(vo);
        }


        //5.遍历vo  和 卷问列表的 【编号、等级、星级、封面】
        for (Questionnaire questionnaire : questionnaireList) {
            for (QuestionnaireVo vo : questionnaireVoList) {
                //设置保存条件
                // 只有【vo中的问卷Id】=【问卷列表的问卷Id】时，才保存至vo中
                if (questionnaire.getId().toString().equals(vo.getId().toString())) {
                    vo.setLevel(questionnaire.getLevel().toString());
                    vo.setName(questionnaire.getName());
                    vo.setCover(questionnaire.getCover());
                    vo.setStar(questionnaire.getStar());
                }
            }
        }

        //6.遍历【所有的问卷列表vo】
        for (QuestionnaireVo questionnaireVo : questionnaireVoList) {
            //7.遍历【所有的题目】，转成vo
            List<SoulQuestionVo> soulQuestionVoList = new ArrayList<>();
            for (SoulQuestion soulQuestion : questionList) {
                SoulQuestionVo vo = new SoulQuestionVo();

                //8.设置【当问卷列表类中问卷的id= 题目中对应的问卷id时才进入循环添加】 条件
                if (questionnaireVo.getId().toString().equals(soulQuestion.getQuestionnaireId().toString())) {
                    //设置题目id
                    vo.setId(soulQuestion.getId().toString());
                    //设置问卷题目内容
                    vo.setQuestion(soulQuestion.getStem());
                    //保存
                    soulQuestionVoList.add(vo);
                }
                //9.遍历【所有的选项】，转成vo
                List<SoulQuestionOptionVo> OptionVoList = new ArrayList<>();
                for (SoulQuestionOption option : optionList) {
                    //【当题目的id= 选项对应的题目的id】 条件
                    if (soulQuestion.getId().toString().equals(option.getQuestionId().toString())) {
                        SoulQuestionOptionVo vo1 = new SoulQuestionOptionVo();
                        vo1.setOption(option.getContent());
                        vo1.setId(option.getId().toString());
                        //保存
                        OptionVoList.add(vo1);

                    }
                    vo.setOptions(OptionVoList);
                }
                questionnaireVo.setQuestions(soulQuestionVoList);
            }

        }
        System.out.println(questionnaireVoList);
        return questionnaireVoList;
    }


    /**
     * 测灵魂-提交问卷
     *
     * @param
     * @return
     */
    public QuestionUserLock SubmitQuestionnaire(List<Answers> answersList) {

        //1.获取用户id
        Long loginUserId = UserHolder.getUserId();

        //2.调用api，根据用户id，查询锁表状态.
        List<QuestionUserLock> questionUserLockList = soulApi.findUserLockById(loginUserId);

        //3.遍历Answers，拿到前端传过来的 用户提交的 【题目编号】 和【选项编号】
        Integer score = 0;
        Long questionId = 0L;

        for (Answers answer : answersList) {
            //拿到【选项编号】
            Long optionId = answer.getOptionId();

            //调用api查询选项编号id对应的分数，计算总分数
            //因为在循环中，一次只查一条记录。
            SoulQuestionOption soulQuestionOption = soulApi.findScoreById(optionId);

            //查询出来的是String类型，需要转换成int类型
            score += Integer.parseInt(soulQuestionOption.getScore());

            //拿到所有【题目编号】
            questionId = answer.getQuestionId();

        }

        //4.调用api,只需要根据for循环出来的最后一个题目的编号，【查询出用户的问卷id】
        System.out.println("questionId================================================"+questionId);
        SoulQuestion soulQuestion = soulApi.findQuestionNaireIdByQuestionId(questionId);
        Long questionnaireId = soulQuestion.getQuestionnaireId();

        //调用api,根据用户id，问卷id， 查出锁卷状态
        QuestionUserLock userLock = soulApi.SelectQuesitonUserLock(loginUserId, questionnaireId);

        //获取对应的锁卷id
        Long id = userLock.getId();
        //给当前的问卷状态设置reportId。
        userLock.setReportId(1L);
        soulApi.SetReportId(userLock);

        if (questionnaireId < 3) {

            questionnaireId = questionnaireId + 1;
            System.out.println("questionnaireId============================="+questionnaireId);
            //调用api，更新，将对应的下一个问卷的锁打开
            QuestionUserLock userLock1 = soulApi.UpdateQuesitonUserLock(loginUserId, questionnaireId);
        }


        //调用api，根据问卷id，得到结果表中的数据，将它添加到报告表中
        Long questionnaireId1 = userLock.getQuestionnaireId();
        System.out.println("questionnaireId1========================="+questionnaireId1);
        List<QuestionNaireResult> resultList = soulApi.findResult(questionnaireId1);

        //遍历结果集合
        String content = null;
        String cover = null;

       //循环遍历结果表，得到【评分结果】和【封面】
        for (QuestionNaireResult result : resultList) {
            Long scope1 = result.getScope();
            if (score < scope1) {
                System.out.println("Scope1======================" + scope1);
                //获取评分结果
                content = result.getContent();
                //获取封面
                cover = result.getCover();
                break;
            }
        }

        //将结果封装到报告表中。
        QuestionNaireReport report = new QuestionNaireReport();
        report.setUserId(loginUserId);
        report.setQuestionnaireId(questionnaireId1);
        report.setReportId(userLock.getReportId());
        report.setCover(cover);
        report.setContent(content);
        report.setExtroversion("70");
        report.setJudgement("80");
        report.setAbstraction("90");
        report.setRetionality("100");
        soulApi.addReport(report);
        return userLock;
    }

    /**
     * 测灵魂-查看结果
     *
     * @param reportId
     * @return
     */
    public TestResultVo ViewResults(Long reportId) {
        Long loginUserId = UserHolder.getUserId();

        //根据reportId【报告ID】，登录用户id，查出记录
        List<QuestionNaireReport> reportList = soulApi.findList(reportId, loginUserId);

        Long questionnaireId = null;

        //遍历结果集，得到问卷id
        for (QuestionNaireReport report : reportList) {
            questionnaireId = report.getQuestionnaireId();
        }

        //根据问卷id，用户id，查出单条记录
        QuestionNaireReport questionNaireReport = soulApi.findOne(loginUserId, questionnaireId);


        //构建DimensionsList
        List<Dimensions> dimensionsList = new ArrayList<>();
        dimensionsList.add(new Dimensions("外向", questionNaireReport.getExtroversion()));
        dimensionsList.add(new Dimensions("判断", questionNaireReport.getJudgement()));
        dimensionsList.add(new Dimensions("抽象", questionNaireReport.getAbstraction()));
        dimensionsList.add(new Dimensions("理性", questionNaireReport.getRetionality()));

        //构建SimilarYouList
        List<SimilarYou> similarYouList = new ArrayList<>();
        similarYouList.add(new SimilarYou(1, null));
        similarYouList.add(new SimilarYou(2, null));
        similarYouList.add(new SimilarYou(3, null));
        similarYouList.add(new SimilarYou(4, null));

        //构建vo
        TestResultVo vo = new TestResultVo();
        //设置测试评论
        vo.setConclusion(questionNaireReport.getContent());
        //设置封面地址
        vo.setCover(questionNaireReport.getCover());
        //设置纬度List
        vo.setDimensions(dimensionsList);

        vo.setSimilarYou(similarYouList);

        return vo;

    }


}
