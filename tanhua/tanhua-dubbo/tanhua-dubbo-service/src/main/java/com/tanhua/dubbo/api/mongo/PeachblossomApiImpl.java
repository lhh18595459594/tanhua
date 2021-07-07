package com.tanhua.dubbo.api.mongo;

import com.alibaba.druid.sql.visitor.functions.Now;
import com.tanhua.domain.mongo.ReceivingVoice;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.mongo.Voice;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PeachblossomApiImpl implements PeachblossomApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 上传语音功能
     *
     * @param voice
     */
    public void add(Voice voice) {
        mongoTemplate.save(voice);
    }

    /**
     * 根据用户id查找该用户发的语音
     *
     * @param loginUserId
     * @return
     */
    public Voice findById(Long loginUserId) {
        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(loginUserId));

        Voice voice = mongoTemplate.findOne(query, Voice.class);

        return voice;
    }

    /**
     * 查询所有发送了语音的用户id
     *
     * @return
     */
    public List<Long> findAll() {

        List<Voice> voiceList = mongoTemplate.findAll(Voice.class);


        List<Long> userIds = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(voiceList)) {
            //遍历取出所有的用户id
            userIds = voiceList.stream().map(Voice::getUserId).collect(Collectors.toList());
        }
        return userIds;
    }


    /**
     * 添加用户的接收语音的记录
     *
     * @param receivingVoice
     * @return
     */
    public void receivingVoice(ReceivingVoice receivingVoice) {
        mongoTemplate.save(receivingVoice);
    }

    /**
     * 根据登录用户id，查询数据库中是否存在已经收听过的记录
     *
     * @param loginUserId
     * @return
     */
    public ReceivingVoice findByIdtoReceivingVoice(Long loginUserId) {
        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(loginUserId));

        ReceivingVoice receivingVoice = mongoTemplate.findOne(query, ReceivingVoice.class);
        return receivingVoice;
    }
}
