package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.service.LogService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RocketMQMessageListener(topic = "tanhua_log", consumerGroup = "tanhua_log_consumer")
public class LogMessageListener implements RocketMQListener<String> {


    @Autowired
    private LogService logService;


    /**
     * 消费消息
     *
     * @param message
     */
    public void onMessage(String message) {
        //1.返回解析json为Map,将Json字符串转换为对象
        JSONObject msgMap = JSON.parseObject(message);

        //2.构建log对象
        Log log = new Log();
        log.setLogTime(msgMap.getString("log_time"));
        log.setType(msgMap.getString("type"));
        log.setUserId(msgMap.getLong("userId"));
        log.setEquipment(msgMap.getString("equipment"));
        log.setPlace(msgMap.getString("place"));
        log.setCreated(new Date());

        //3.调用service添加Log记录
        logService.add(log);
    }


}
