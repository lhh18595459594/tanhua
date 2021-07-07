package com.tanhua.manage.utils;

import cn.hutool.core.date.DateUtil;

import java.math.BigDecimal;
import java.util.Date;

public class ComputeUtil {
    /**
     * 计算环比
     *
     * @param current 本期计数
     * @param last    上一期计数
     * @return 环比
     */
    public static BigDecimal computeRate(Long current, Long last) {
        BigDecimal result;
        if (last == 0) {
            // 当上一期计数为零时，此时环比增长为倍数增长
            result = new BigDecimal((current - last) * 100);
        } else {
            // (今天计数-昨天计数)/昨天计数
            result = BigDecimal.valueOf((current - last) * 100).divide(BigDecimal.valueOf(last), 2, BigDecimal.ROUND_HALF_DOWN);
        }
        return result;
    }

    public static String offsetDay(Date date, int offSet) {
        return DateUtil.offsetDay(date,offSet).toDateStr();
    }
}