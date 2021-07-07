package com.tanhua.manage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisSummaryVo {

    private Integer cumulativeUsers; //累计用户数

    private Integer activePassMonth; //过去30天活跃用户数

    private Integer activePassWeek;  //过去7天活跃用户

    private Integer newUsersToday;     //今日新增用户数量

    private BigDecimal newUsersTodayRate; // 今日新增用户涨跌率，单位百分数，正数为涨，负数为跌

    private Integer loginTimesToday;  //今日登录次数

    private BigDecimal loginTimesTodayRate;  //今日登录次数涨跌率，单位百分数，正数为涨，负数为跌

    private Integer activeUsersToday;  //今日活跃用户数量

    private BigDecimal activeUsersTodayRate; //今日活跃用户涨跌率，单位百分数，正数为涨，负数为跌
}