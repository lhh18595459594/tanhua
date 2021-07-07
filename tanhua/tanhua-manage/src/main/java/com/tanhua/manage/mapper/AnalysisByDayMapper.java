package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.vo.DataPointVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Mapper
public interface AnalysisByDayMapper extends BaseMapper<AnalysisByDay> {

    /**
     * 通过日期查询当天的所有统计数据
     *
     * @param today
     * @return
     */
    @Select("select * from tb_analysis_by_day where record_date=#{today}")
    AnalysisByDay findByDate(String today);


    /**
     * 统计总用户数(总注册数)
     * （查询tb_analysis_by_day表中所有的num_registered之和)
     *
     * @return
     */
    @Select("select sum(num_registered) from tb_analysis_by_day")
    Integer countTotalUser();


    /**
     * 通过日期范围查询统计数据
     * #{} ${} 两者的区别
     *  ? 点位符，prepared预编译，sql发给数据库
     *  ${} sql拼接 防止 sql注入
     * @param startDate
     * @param endDate
     * @param column
     * @return
     */
    @Select("select date_format(record_date,'%Y-%m-%d') title,${column} amount from tb_analysis_by_day where record_date between #{startDate} and #{endDate} ")
    /**
     * mybatis中的参数不能有相同的类型，所以需要加别名
     */
    List<DataPointVo> findBetweenDate(@Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("column") String column);
}