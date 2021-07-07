package com.tanhua.manage.controller;

import com.tanhua.manage.service.AnalysisService;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 首页数据统计controller
 */
@RestController
@RequestMapping("/dashboard")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 概要统计信息(累计用户、过去7天活跃用户、过去30天活跃用户、新增用户、登录次数、活跃用户)
     *
     * @return
     */
    @GetMapping("/summary")
    public ResponseEntity getSummary() {

        // 调用业务查询
        AnalysisSummaryVo vo = analysisService.summary();

        return ResponseEntity.ok(vo);
    }


    /**
     * 新增、活跃用户、次日留存率
     * @param sd 开始时间
     * @param ed  结束时间
     * @param type 101 新增 102 活跃用户 103 次日留存率
     * @return
     */
    @GetMapping("/users")
    public ResponseEntity getUsersCount(@RequestParam(name = "sd") Long sd,
                                        @RequestParam(name = "ed") Long ed,
                                        @RequestParam(name = "type") Integer type) {

        AnalysisUsersVo vo = analysisService.getUsersCount(sd,ed,type);

        return ResponseEntity.ok(vo);
    }
}
