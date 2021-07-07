package com.tanhua.manage.listener;

import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.mongo.PublishApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "tanhua-publish", consumerGroup = "tanhua_publish_group")
public class PublishMessageLinstener implements RocketMQListener<String> {

    @Reference
    private PublishApi publishApi;

    @Autowired
    private HuaWeiUGCTemplate huaWeiUGCTemplate;

    public void onMessage(String publishId) {
        log.info("开始进行动态审核,id={}", publishId);

        //1.通过动态id查询动态信息
        Publish publish = publishApi.findById(publishId);

        //还未审核
        log.info("要审核的动态状态：state={}", publish.getState());
        if (publish.getState() == 0) {
            //2.获取动态的文本、图片
            String textContent = publish.getTextContent();
            List<String> pirUrls = publish.getMedias();

            //3.调用华为云审核文本
            boolean textContentCheck = huaWeiUGCTemplate.textContentCheck(textContent);

            log.info("动态审核文本结果：id={},{}", publishId, textContentCheck);

            boolean imageCheck = true;  //默认为审核通过
            //4.文本通过后再审核图片
            if (textContentCheck) {

                log.info("动态审核图片：id={},图片有多少张{}", publishId, pirUrls != null ? pirUrls.size() : 0);
                if (!CollectionUtils.isEmpty(pirUrls)) {
                    String[] picUrlsArr = pirUrls.toArray(new String[]{});
                    huaWeiUGCTemplate.imageContentCheck(picUrlsArr);
                }

                log.info("动态审核图片结果：id={},{}", publishId, imageCheck);
            }
            int state = 2; //需要人工复审
            if (textContentCheck && imageCheck) {
                //5.如果审核都通过，更改审核状态。
                state = 1;
            }

            //6.如果不通过，也要更新状态，人工复审
            publishApi.updateState(publishId, state);

            log.info("更新状态成功 id={},state={}",publishId,state);
        }
    }
}
