package com.tanhua.server.service;


import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.UserLocationVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocationService {


    @Reference
    private UserLocationApi userLocationApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 上传地理位置
     *
     * @param paramMap
     */
    public void reportLocation(Map<String, Object> paramMap) {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();

        //从map中取出前端传过来的信息
        //取出纬度
        Double latitude = (Double) paramMap.get("latitude");

        //取出经度
        Double longitude = (Double) paramMap.get("longitude");

        //取出位置描述
        String addrStr = (String) paramMap.get("addrStr");

        userLocationApi.addLocation(loginUserId, latitude, longitude, addrStr);
    }

    /**
     * 搜附近
     * @param gender
     * @param distance 圆的半径
     * @return
     */
    public List<NearUserVo> searchNearBy(String gender, String distance) {
        //1.获取当前用户id
        Long loginUserId = UserHolder.getUserId();


        //2、调用API根据用户id，距离查询当前用户附近的人 List<UserLocationVo>
        List<UserLocationVo> userLocationList = userLocationApi.searchNearBy(loginUserId, Long.valueOf(distance));

        List<NearUserVo> voList = new ArrayList<>();
        //3、循环附近的人所有数据
        if (!CollectionUtils.isEmpty(userLocationList)) {
            // 范围的人的id取出来
            List<Long> nearByUserIds = userLocationList.stream().map(UserLocationVo::getUserId).collect(Collectors.toList());
            // 批量的查询这些用户信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchIds(nearByUserIds);
            //4.转成vo
            // filter是来过滤，符合条件的数据留下, !gender.equals(u.getGender()) 性别不一样的就过滤
            // 基础数据类型(除String外) 才可以==， 其它的都使用equals
            voList = userInfoList.stream().filter(u -> gender.equals(u.getGender()))
                    .map(u -> new NearUserVo(u.getId(), u.getAvatar(), u.getNickname()))
                    .collect(Collectors.toList());
        }
        return voList;
    }

}
