package com.tanhua.server.controller;

import com.tanhua.domain.vo.ReceivingVoiceVo;
import com.tanhua.server.service.PeachblossomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * 桃花传音  控制层
 */
@RestController
@RequestMapping("/peachblossom")
public class PeachBlossomController {

    @Autowired
    private PeachblossomService peachblossomService;


    /**
     * 探花传音，发送语音
     *
     * @param soundFile
     * @return
     */
    @PostMapping
    public ResponseEntity sendVoiceMessage(MultipartFile soundFile) {
        peachblossomService.add(soundFile);
        return ResponseEntity.ok(null);
    }


    /**
     * 接收语音
     * @return
     */
    @GetMapping
    public ResponseEntity ReceivingVoiceMessage() {

        ReceivingVoiceVo vo = peachblossomService.accept();
        return ResponseEntity.ok(vo);
    }
}
