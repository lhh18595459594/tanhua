package com.tanhua.server.controller;


import com.tanhua.domain.db.Linhunceshi.Answers;
import com.tanhua.domain.db.Linhunceshi.QuestionUserLock;
import com.tanhua.domain.vo.QuestionnaireVo;
import com.tanhua.domain.vo.TestResultVo;
import com.tanhua.server.service.SoulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 灵魂测试 控制层
 */
@RestController
@RequestMapping("/testSoul")
public class SoulController {

    @Autowired
    private SoulService soulService;


    /**
     * 测灵魂-问卷列表
     *
     * @return
     */
    @GetMapping
    public ResponseEntity findAll() {

        List<QuestionnaireVo> questionnaireVoList = soulService.findAll();
        return ResponseEntity.ok(questionnaireVoList);

    }


    /**
     * 测灵魂-提交问卷
     *
     * @param answers
     * @return
     */
    @PostMapping
    public ResponseEntity SubmitQuestionnaire(@RequestBody Map<String, List<Answers>> answers) {

        List<Answers> answersList = answers.get("answers");

        QuestionUserLock questionUserLock = soulService.SubmitQuestionnaire(answersList);
        String reportId = questionUserLock.getReportId().toString();
        return ResponseEntity.ok(reportId);
    }


    @GetMapping("/report/{id}")
    public ResponseEntity ViewResults(@PathVariable String id) {

        Long reportId = Long.valueOf(id);

        TestResultVo vo = soulService.ViewResults(reportId);
        return ResponseEntity.ok(vo);
    }
}
