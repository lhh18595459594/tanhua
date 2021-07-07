package com.tanhua.server.service;

import cn.hutool.core.date.DateUtil;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.ReceivingVoice;
import com.tanhua.domain.mongo.Voice;
import com.tanhua.domain.vo.ReceivingVoiceVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.PeachblossomApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 探花传音业务层
 */
@Service
@Slf4j
public class PeachblossomService {

    @Reference
    private PeachblossomApi peachblossomApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 上传语音功能
     *
     * @param soundFile
     */
    public void add(MultipartFile soundFile) {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();

        //2.调用api，查出该登录用户之前有没有发送语音
        Voice voice = peachblossomApi.findById(loginUserId);

        Voice newVoice = new Voice();
        if (voice != null) {
             /*
             这里必须先查询出旧的地址，不能放在更新地址完成之后。不然更新地址以后获取的是新地址
              */
            //先获取旧的语音地址，并删除。
            String oldVoiceUrl = voice.getVoiceUrl();

            System.out.println("oldVoiceUrl===============================" + oldVoiceUrl);

            //调用阿里云，上传语音,不设置动态id，让它覆盖
            voice = Aliyunsend(soundFile, loginUserId, voice);
            //调用方法,更新语音
            peachblossomApi.add(voice);
            log.info("上传语音成功");
            //上传新的语音成功以后，删除阿里云上旧的语音地址
            ossTemplate.deleteFile(oldVoiceUrl);
        } else {
            //调用阿里云，上传语音,设置动态id，让它覆盖
            newVoice = Aliyunsend(soundFile, loginUserId, newVoice);
            //设置动态id
            newVoice.setId(new ObjectId());
            //调用方法,保存语音
            peachblossomApi.add(newVoice);
            log.info("上传语音成功");
        }
    }

    /**
     * 接收语音
     *
     * @return
     */
    public ReceivingVoiceVo accept() {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();

        //2.获取所有的保存的语音的用户id
        List<Long> userIds = peachblossomApi.findAll();

        //3.通过随机的方式，随机接收一个用户id的语音。
        Random random = new Random();
        Long VoiceUserId = Long.valueOf(random.nextInt(userIds.size() + 1));

        //4.调用api，查出发送语音用户的信息
        UserInfo userInfo = userInfoApi.findById(VoiceUserId);
        //5.调用api，查出语音用户发送过的语音记录
        Voice userInfoVoice = peachblossomApi.findById(userInfo.getId());
        //6.取出语音地址
        String voiceUrl = userInfoVoice.getVoiceUrl();

        //7.根据登录用户id，查询数据库中是否存在（存在则代表之前听过）
        ReceivingVoice receivingVoice = peachblossomApi.findByIdtoReceivingVoice(loginUserId);

        //8.构建新的实体类
        ReceivingVoice receivingVoice1 = new ReceivingVoice();

        //9.构建vo
        ReceivingVoiceVo vo = new ReceivingVoiceVo();

        Integer remainingTimes = null;

        //10.判断登录用户的收听记录是否为空
        if (receivingVoice != null) {
            //不为空则代表数据库存在
            //取出收看次数
            remainingTimes = receivingVoice.getRemainingTimes();
            //调用封装方法，是否到了24点，到了则重置收听次数
            receivingVoice = count(receivingVoice, remainingTimes);

            receivingVoice.setUserId(loginUserId.intValue());
            BeanUtils.copyProperties(userInfo, receivingVoice);
            receivingVoice.setSoundUrl(voiceUrl);
            //11.添加记录
            peachblossomApi.receivingVoice(receivingVoice);

            //复制内容给vo
            BeanUtils.copyProperties(receivingVoice, vo);
            //用户id类型不对，转换
            vo.setId(loginUserId.intValue());
        } else {
            //数据库中不存在该用户的收听记录
            //调用方法封装，但是要重新设置动态id，因为数据库中没有记录
            receivingVoice1 = count(receivingVoice1, remainingTimes);
            receivingVoice1.setUserId(loginUserId.intValue());
            BeanUtils.copyProperties(userInfo, receivingVoice1);
            receivingVoice1.setSoundUrl(voiceUrl);
            receivingVoice1.setId(new ObjectId());

            //12.添加记录
            peachblossomApi.receivingVoice(receivingVoice1);
            //复制内容给vo
            BeanUtils.copyProperties(receivingVoice1, vo);
            //用户id类型不对，转换
            vo.setId(loginUserId.intValue());
        }
        //13.返回
        return vo;

    }


    /**
     * 封装调用阿里云，上传语音
     *
     * @param soundFile   语音文件
     * @param loginUserId 登录用户id
     * @param voice       与数据库交互的实体类
     * @return
     */
    public Voice Aliyunsend(MultipartFile soundFile, Long loginUserId, Voice voice) {

        try {
            //2.调用阿里云，上传语音文件
            String voiceUrl = ossTemplate.upload(soundFile.getOriginalFilename(), soundFile.getInputStream());
            voice.setUserId(loginUserId);
            voice.setVoiceUrl(voiceUrl);
            voice.setCreated(System.currentTimeMillis());
            voice.setUpdate(System.currentTimeMillis());
        } catch (IOException e) {
            log.info("上传语音失败");
        }
        return voice;
    }


    /**
     * 封装方法，重置收听次数，并设置ReceivingVoice属性
     *
     * @param receivingVoice 与数据库交互的实体类
     * @param remainingTimes 剩余次数
     * @return
     */
    public ReceivingVoice count(ReceivingVoice receivingVoice, Integer remainingTimes) {

        try {
                //当次数=0时
                //1.获取当前时间
                long nowTime = System.currentTimeMillis();
                //2.获取当天时间的0点时间
                SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
                long todayTime = 0;
                todayTime = sd.parse(sd.format(new Date())).getTime();
                //3.获取第二天的0点的时间
                long tomorrowTime = todayTime + 86400000;
                //4.判断当前时间是否已经过了24点
                if ((tomorrowTime - todayTime) <= nowTime) {
                    //5.代表已经过了,刷新.
                    remainingTimes = 10;
                }else {
                    remainingTimes--;
                }
                //11.设置接收次数
                receivingVoice.setRemainingTimes(remainingTimes);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return receivingVoice;
    }

}
