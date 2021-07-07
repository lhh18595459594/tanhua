package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.ReceivingVoice;
import com.tanhua.domain.mongo.Voice;

import java.util.List;

public interface PeachblossomApi {

    /**
     * 上传语音功能
     * @param voice
     */
    void add(Voice voice);

    /**
     * 根据用户id查找该用户发的语音
     * @param loginUserId
     * @return
     */
    Voice findById(Long loginUserId);

    /**
     * 查询所有的发送了语音的用户id
     * @return
     */
    List<Long> findAll();

    /**
     * 添加用户的接收语音的记录
     *
     * @param receivingVoice
     * @return
     */
    void receivingVoice(ReceivingVoice receivingVoice);

    /**
     * 根据登录用户id，查询数据库中是否存在已经收听过的记录
     * @param loginUserId
     * @return
     */
    ReceivingVoice findByIdtoReceivingVoice(Long loginUserId);
}
