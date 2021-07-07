package com.tanhua.manage.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.Log;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogMapper extends BaseMapper<Log> {

    /**
     * 过去?天活跃用户数
     *
     * @param date
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time>#{date}")
    Integer countActiveUserAfterDate(String date);


    /**
     * 根据传过来的type值，决定该方法是【统计今日注册人数】还是【统计今日登录人数】
     *
     * @param today
     * @param type
     * @return
     */
    //因为有两个同类型String的参数。所以必须取别名
    @Select("select count(distinct user_id) from tb_log where log_time=#{today} and type=#{type}")
    Integer queryNumsByType(@Param("today") String today, @Param("type") String type);


    /**
     * 统计【今日活跃数】
     *
     * @param today
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time=#{today}")
    Integer queryNumsByDate(String today);


    /**
     * 统计【次日留存用户数】
     *
     * @param today
     * @param yesterday
     * @return
     */
    @Select("select count(distinct user_id) " +
            "from tb_log " +
            "where log_time=#{today} and " +
            "user_id in (select user_id FROM tb_log WHERE log_time=#{yesterday} and type='0102');")
    Integer queryRetention1d(@Param("today") String today, @Param("yesterday") String yesterday);
}
