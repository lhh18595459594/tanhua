package com.tanhua.manage.job;

import cn.hutool.core.date.DateUtil;
import com.tanhua.manage.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 编写定时执行方法
 * 案例：每隔5秒钟，打印系统时间
 * 在引导类上配置注解@EnableScheduling
 * 1、此类需要交给spring容器管理
 * 2、配置一个方法（没有参数，没有返回值） 定时需要执行的业务逻辑
 * 3、在此方法上，通过注解配置时间表达式
 * 时间表达式：配置时间规则
 */
@Component
@Slf4j
public class AnalysisJob {

    @Autowired
    private AnalysisService analysisService;

    /**
     * cron 定时(时间错过，只会等一次，重启后只有时间点到了才执行)
     * <p>
     * initialDelay 延迟执行，间隔多长后再执行(再次启动后就会执行)
     */
    //每隔30秒打印当前系统时间
    //@Scheduled(cron = "* 0/30 * * * ?")

    //延迟3秒,每间隔10分钟执行一次(单位都是毫秒)
    @Scheduled(initialDelay = 3000, fixedDelay = 600000)
    public void analysis() {
        log.info("后台统计任务开始：" + DateUtil.formatTime(new Date()));

        long StartTime = System.currentTimeMillis();
        //调用方法
        analysisService.analysisLog();
        long EndTime = System.currentTimeMillis();
        //耗时
        long cost = StartTime - EndTime;
        log.info("耗时：" + cost);

        System.out.println("完成数据统计");
    }
}
