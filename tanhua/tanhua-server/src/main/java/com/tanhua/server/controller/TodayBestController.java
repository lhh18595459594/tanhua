package com.tanhua.server.controller;

import com.tanhua.domain.vo.*;
import com.tanhua.server.service.ImService;
import com.tanhua.server.service.LocationService;
import com.tanhua.server.service.TodayBestServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 【今日佳人】 控制层
 */

@RestController
@RequestMapping("/tanhua")
public class TodayBestController {


    @Autowired
    private TodayBestServcie todayBestServcie;

    @Autowired
    private ImService imService;

    @Autowired
    private LocationService locationService;

    /**
     * 查询今日佳人
     *
     * @return
     */
    @GetMapping("/todayBest")
    public ResponseEntity todayBest() {

        //查询佳人，缘分值最高的
        TodayBestVo todayBestVo = todayBestServcie.queryTodayBest();

        return ResponseEntity.ok(todayBestVo);
    }


    /**
     * 交友——推荐用户列表
     *
     * @param queryParam
     * @return
     */
    @GetMapping("/recommendation")
    public ResponseEntity recommendationList(RecommendUserQueryParam queryParam) {
        //调用业务，查询
        PageResult<RecommendUserVo> pageResult = todayBestServcie.recommendationList(queryParam);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 查看佳人信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/{id}/personalInfo")
    public ResponseEntity queryUserDetail(@PathVariable("id") Long userId) {

        TodayBestVo todayBestVo = todayBestServcie.queryUserDetail(userId);

        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 查询陌生人的问题
     *
     * @param userId
     * @return
     */
    @GetMapping("/strangerQuestions")
    public ResponseEntity strangerQuestions(Long userId) {
        String questions = todayBestServcie.strangerQuestions(userId);

        return ResponseEntity.ok(questions);
    }

    /**
     * 回复陌生人问题
     *
     * @param paramMap
     * @return
     */
    @PostMapping("/strangerQuestions")
    public ResponseEntity replyStrangerQuestions(@RequestBody Map<String, Object> paramMap) {
        imService.replyStrangerQuestions(paramMap);
        return ResponseEntity.ok(null);
    }


    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity searchNearBy(@RequestParam(required = false) String gender,
                                       @RequestParam(defaultValue = "2000") String distance) {

        List<NearUserVo> nearUserVoList = locationService.searchNearBy(gender, distance);

        return ResponseEntity.ok(nearUserVoList);
    }

}
