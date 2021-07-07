package com.tanhua.manage.service;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import com.tanhua.manage.vo.DataPointVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 统计分析业务类
 */
@Service
public class AnalysisService {

    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;

    @Autowired
    private LogMapper logMapper;

    /**
     * 概要统计信息
     *
     * @return
     */
    public AnalysisSummaryVo summary() {

        // 过去30天, 把当前时间减去30天。java.util.Calendar 日历操作
        String last30Day = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, -30).toDateStr();

        //过去7天
        String last7Day = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, -7).toDateStr();

        //今天
        String today = DateUtil.today();

        //昨天
        String yesterday = DateUtil.yesterday().toDateStr();


        //1.累计用户数
        Integer cumulativeUsers = analysisByDayMapper.countTotalUser();
        //2.过去30天活跃用户数
        Integer activePassMonth = logMapper.countActiveUserAfterDate(last30Day);
        //3.过去7天活跃用户
        Integer activePassWeek = logMapper.countActiveUserAfterDate(last7Day);


        //今天的数据（通过查询tb_analysis_by_day得到）
        AnalysisByDay todayData = analysisByDayMapper.findByDate(today);
        //昨天的数据
        AnalysisByDay yesterdayData = analysisByDayMapper.findByDate(yesterday);

        //4.今日新增用户数量
        Integer newUsersToday = todayData.getNumRegistered();
        //昨天新增用户数量
        Integer yesterdayUsers = yesterdayData.getNumRegistered();


        // 5.今日新增用户涨跌率，单位百分数，正数为涨，负数为跌
        // (今天-昨天)/昨天
        BigDecimal newUsersTodayRate = ComputeUtil.computeRate(newUsersToday.longValue(), yesterdayUsers.longValue());

        //6.今日登录次数
        Integer loginTimesToday = todayData.getNumLogin();
        //昨日登录次数
        Integer loginTimesyesterToday = yesterdayData.getNumLogin();

        //7.今日登录次数涨跌率，单位百分数，正数为涨，负数为跌
        // (今天-昨天)/昨天
        BigDecimal loginTimesTodayRate = ComputeUtil.computeRate(loginTimesToday.longValue(), loginTimesyesterToday.longValue());

        //8.今日活跃用户数量
        Integer activeUsersToday = todayData.getNumActive();
        //.昨日活跃用户数量
        Integer activeUsersyesterday = yesterdayData.getNumActive();

        //9.今日活跃用户涨跌率，单位百分数，正数为涨，负数为跌
        // (今天-昨天)/昨天
        BigDecimal activeUsersTodayRate = ComputeUtil.computeRate(activeUsersToday.longValue(), activeUsersyesterday.longValue());


        //10.把数据设置到vo中
        AnalysisSummaryVo vo = new AnalysisSummaryVo();
        vo.setCumulativeUsers(cumulativeUsers);
        vo.setActivePassMonth(activePassMonth);
        vo.setActivePassWeek(activePassWeek);
        vo.setNewUsersToday(newUsersToday);
        vo.setNewUsersTodayRate(newUsersTodayRate);
        vo.setLoginTimesToday(loginTimesToday);
        vo.setLoginTimesTodayRate(loginTimesTodayRate);
        vo.setActiveUsersToday(activeUsersToday);
        vo.setActiveUsersTodayRate(activeUsersTodayRate);
        return vo;
    }

    /**
     * 新增、活跃用户、次日留存率
     *
     * @param sd   开始时间
     * @param ed   结束时间
     * @param type 101 新增 102 活跃用户 103 次日留存率
     * @return
     */
    public AnalysisUsersVo getUsersCount(Long sd, Long ed, Integer type) {

        //1.传入前端输入的开始日期、结束日期
        //今年的开始日期
        String thisYearStartDate = DateUtil.date(sd).toDateStr();
        //今年的结束日期
        String thisYearEndDate = DateUtil.date(ed).toDateStr();

        //去年的开始日期
        String lastYearStartDate = DateUtil.date(sd).offset(DateField.YEAR, -1).toDateStr();
        //去年的结束日期
        String lastYearEndDate = DateUtil.date(ed).offset(DateField.YEAR, -1).toDateStr();


        //2.根据数字来辨别查询的列
        String column = "num_registered";
        switch (type) {
            //查询新增用户
            case 101:
                column = "num_registered";
                break;
            //查询活跃用户
            case 102:
                column = "num_active";
                break;
            //查询次日留存率
            case 103:
                column = "num_retention1d";
                break;
            default:
                break;
        }

        //查询今年的数据,传入今年的 (开始时间、结束时间、查询的列名)
        List<DataPointVo> thisYear = analysisByDayMapper.findBetweenDate(thisYearStartDate, thisYearEndDate, column);

        //查询去年的数据,传入去年的 (开始时间、结束时间、查询的列名)
        List<DataPointVo> lastYear = analysisByDayMapper.findBetweenDate(lastYearStartDate, lastYearEndDate, column);

        AnalysisUsersVo vo = new AnalysisUsersVo();
        vo.setThisYear(thisYear);
        vo.setLastYear(lastYear);

        return vo;
    }


    /**
     * 每日数据自动统计
     */
    public void analysisLog() {
        //1.获取统计的今天日期
        String today = DateUtil.today();

        Date todayDate = DateUtil.parse(today);  //今日date对象

        //2.获取昨天的日期
        String yesterday = DateUtil.yesterday().toDateStr();


        //4.根据日期，查询tb_analysis_by_day是否有今日的统计记录
        //如果是第一次统计，创建一条今日的统计数据
        QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper();

        queryWrapper.eq("record_date", today);
        AnalysisByDay analysis = analysisByDayMapper.selectOne(queryWrapper);

        if (analysis == null) {
            //不存在，今日第一次统计。保存一天数据到数据库
            analysis = new AnalysisByDay();
            analysis.setRecordDate(todayDate);
            analysis.setCreated(new Date());
            analysis.setUpdated(new Date());
            //因为日期每天都是固定的，无需反复插入更新，每天只需要插入一次即可
            analysisByDayMapper.insert(analysis);
        }

        //5.统计Log表中，今日的注册人数
        Integer registerNum = logMapper.queryNumsByType(today, "0102");

        //6.统计Log表中，今日的登录人数
        Integer loginNum = logMapper.queryNumsByType(today, "01");

        //7.统计Log表中，今日的活跃人数
        Integer activeNum = logMapper.queryNumsByDate(today);

        //8.统计Log表中，次日留存用户
        Integer numRetention1d = logMapper.queryRetention1d(today,yesterday);

        analysis.setNumRegistered(registerNum);
        analysis.setNumLogin(loginNum);
        analysis.setNumActive(activeNum);
        analysis.setNumRetention1d(numRetention1d);

        //插入相关数据
        analysisByDayMapper.updateById(analysis);


    }


}
